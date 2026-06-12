package com.platform.repository;

import com.platform.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByUserIdOrderByCompletedAtDesc(Long userId);
    List<Result> findByUserIdAndTestId(Long userId, Long testId);
}
