package com.platform.repository;

import com.platform.entity.ContestParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContestParticipantRepository extends JpaRepository<ContestParticipant, Long> {
    List<ContestParticipant> findByContestIdOrderByScoreDesc(Long contestId);
    Optional<ContestParticipant> findByContestIdAndUserId(Long contestId, Long userId);
    List<ContestParticipant> findByUserIdOrderByJoinedAtDesc(Long userId);
}
