package br.com.candidflow.repository;

import br.com.candidflow.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Page<Feedback> findByApplicationId(Long applicationId, Pageable pageable);
}
