package com.codearena.repository;

import com.codearena.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByUserId(Long userId);
    List<Submission> findByProblemId(Long problemId);
    List<Submission> findByUserIdAndProblemId(Long userId, Long problemId);
}
