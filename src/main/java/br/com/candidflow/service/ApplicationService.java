package br.com.candidflow.service;

import br.com.candidflow.dto.request.ApplicationCreateRequestDTO;
import br.com.candidflow.dto.request.ApplicationStatusUpdateDTO;
import br.com.candidflow.dto.request.ApplicationUpdateRequestDTO;
import br.com.candidflow.dto.response.ApplicationResponseDTO;
import br.com.candidflow.entity.Candidate;
import br.com.candidflow.entity.Job;
import br.com.candidflow.entity.JobApplication;
import br.com.candidflow.enums.JobStatus;
import br.com.candidflow.exception.BusinessRuleException;
import br.com.candidflow.exception.ResourceNotFoundException;
import br.com.candidflow.mapper.ApplicationMapper;
import br.com.candidflow.repository.ApplicationRepository;
import br.com.candidflow.repository.CandidateRepository;
import br.com.candidflow.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;
    private final ApplicationMapper applicationMapper;

    @Transactional
    public ApplicationResponseDTO create(ApplicationCreateRequestDTO request) {
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidato", request.getCandidateId()));
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Vaga", request.getJobId()));

        if (job.getStatus() != JobStatus.OPEN) {
            throw new BusinessRuleException("Não é possível se candidatar a uma vaga fechada");
        }
        if (applicationRepository.existsByCandidateIdAndJobId(candidate.getId(), job.getId())) {
            throw new BusinessRuleException("O candidato já se candidatou a esta vaga");
        }

        JobApplication application = applicationMapper.toEntity(request, candidate, job);
        return applicationMapper.toResponse(applicationRepository.saveAndFlush(application));
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponseDTO> findAll(Pageable pageable) {
        return applicationRepository.findAll(pageable).map(applicationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ApplicationResponseDTO findById(Long id) {
        return applicationMapper.toResponse(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponseDTO> findByCandidate(Long candidateId, Pageable pageable) {
        if (!candidateRepository.existsById(candidateId)) {
            throw new ResourceNotFoundException("Candidato", candidateId);
        }
        return applicationRepository.findByCandidateId(candidateId, pageable)
                .map(applicationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponseDTO> findByJob(Long jobId, Pageable pageable) {
        if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Vaga", jobId);
        }
        return applicationRepository.findByJobId(jobId, pageable)
                .map(applicationMapper::toResponse);
    }

    @Transactional
    public ApplicationResponseDTO update(Long id, ApplicationUpdateRequestDTO request) {
        JobApplication application = findEntityById(id);
        applicationMapper.updateEntity(application, request);
        return applicationMapper.toResponse(applicationRepository.saveAndFlush(application));
    }

    @Transactional
    public ApplicationResponseDTO updateStatus(Long id, ApplicationStatusUpdateDTO request) {
        JobApplication application = findEntityById(id);
        application.setStatus(request.getStatus());
        return applicationMapper.toResponse(applicationRepository.saveAndFlush(application));
    }

    @Transactional
    public void delete(Long id) {
        applicationRepository.delete(findEntityById(id));
    }

    private JobApplication findEntityById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidatura", id));
    }
}
