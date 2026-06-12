package com.platform.controller;

import com.platform.dto.ContestRequest;
import com.platform.entity.Contest;
import com.platform.entity.ContestParticipant;
import com.platform.security.UserPrincipal;
import com.platform.service.ContestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contests")
public class ContestController {

    @Autowired
    private ContestService contestService;

    @GetMapping
    public ResponseEntity<List<Contest>> getAllContests() {
        return ResponseEntity.ok(contestService.getAllContests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contest> getContest(@PathVariable Long id) {
        return ResponseEntity.ok(contestService.getContestById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Contest> createContest(@RequestBody ContestRequest request) {
        return ResponseEntity.ok(contestService.createContest(request));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinContest(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        try {
            ContestParticipant participant = contestService.joinContest(id, userPrincipal.getId());
            return ResponseEntity.ok(participant);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<List<ContestParticipant>> getLeaderboard(@PathVariable Long id) {
        return ResponseEntity.ok(contestService.getContestLeaderboard(id));
    }
}
