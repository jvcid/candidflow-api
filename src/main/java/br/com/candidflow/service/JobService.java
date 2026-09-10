package br.com.candidflow.service;

import br.com.candidflow.dto.request.JobRequestDTO;
import br.com.candidflow.dto.response.JobResponseDTO;
import br.com.candidflow.entity.Job;
import br.com.candidflow.exception.BusinessRuleException;
import br.com.candidflow.exception.ResourceNotFoundException;
import br.com.candidflow.mapper.JobMapper;
import br.com.candidflow.repository.ApplicationRepository;
import br.com.candidflow.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final JobMapper jobMapper;

    @Transactional
    public JobResponseDTO create(JobRequestDTO request) {
        Job job = jobMapper.toEntity(request);
        return jobMapper.toResponse(jobRepository.saveAndFlush(job));
    }

    @Transactional(readOnly = true)
    public Page<JobResponseDTO> findAll(Pageable pageable) {
        return jobRepository.findAll(pageable).map(jobMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public JobResponseDTO findById(Long id) {
        return jobMapper.toResponse(findEntityById(id));
    }

    @Transactional
    public JobResponseDTO update(Long id, JobRequestDTO request) {
        Job job = findEntityById(id);
        jobMapper.updateEntity(job, request);
        return jobMapper.toResponse(jobRepository.saveAndFlush(job));
    }

    @Transactional
    public void delete(Long id) {
        Job job = findEntityById(id);
        if (applicationRepository.existsByJobId(id)) {
            throw new BusinessRuleException("Não é possível excluir uma vaga que possui candidaturas");
        }
        jobRepository.delete(job);
    }

    private Job findEntityById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga", id));
    }
}
