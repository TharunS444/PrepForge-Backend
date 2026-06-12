package com.platform.repository;

import com.platform.entity.PasswordResetToken;
import com.platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByHashedToken(String hashedToken);
    Optional<PasswordResetToken> findByUser(User user);
    void deleteByUser(User user);
}
