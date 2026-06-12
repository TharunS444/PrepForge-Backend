package com.platform.service;

import com.platform.entity.PasswordResetToken;
import com.platform.entity.User;
import com.platform.repository.PasswordResetTokenRepository;
import com.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void generatePasswordResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return; // Don't throw error to prevent email enumeration attacks
        }

        User user = userOpt.get();

        // Generate raw token
        String rawToken = UUID.randomUUID().toString();

        // Hash token before storing
        String hashedToken = hashToken(rawToken);

        // Find existing token or create a new one (Upsert pattern to prevent unique constraint violation on user_id)
        Optional<PasswordResetToken> existingTokenOpt = tokenRepository.findByUser(user);
        PasswordResetToken resetToken;
        if (existingTokenOpt.isPresent()) {
            resetToken = existingTokenOpt.get();
            resetToken.setHashedToken(hashedToken);
            resetToken.setExpiryDate(java.time.LocalDateTime.now().plusMinutes(15));
        } else {
            resetToken = new PasswordResetToken(hashedToken, user);
        }
        tokenRepository.save(resetToken);

        // Send email with raw token
        emailService.sendPasswordResetEmail(user.getEmail(), rawToken);
    }

    @Transactional
    public boolean resetPassword(String rawToken, String newPassword) {
        String hashedToken = hashToken(rawToken);

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByHashedToken(hashedToken);

        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            return false;
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Consume the token
        tokenRepository.delete(resetToken);

        return true;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }
}
