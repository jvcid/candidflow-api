package br.com.candidflow.repository;

import br.com.candidflow.entity.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<JobApplication, Long> {

    @Override
    @EntityGraph(attributePaths = {"candidate", "job"})
    Optional<JobApplication> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"candidate", "job"})
    Page<JobApplication> findAll(Pageable pageable);

    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);
    boolean existsByCandidateId(Long candidateId);
    boolean existsByJobId(Long jobId);

    @EntityGraph(attributePaths = {"candidate", "job"})
    Page<JobApplication> findByCandidateId(Long candidateId, Pageable pageable);

    @EntityGraph(attributePaths = {"candidate", "job"})
    Page<JobApplication> findByJobId(Long jobId, Pageable pageable);
}
