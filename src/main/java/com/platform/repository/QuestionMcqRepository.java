package com.platform.repository;

import com.platform.entity.QuestionMcq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionMcqRepository extends JpaRepository<QuestionMcq, Long> {
    List<QuestionMcq> findByTestId(Long testId);
}
