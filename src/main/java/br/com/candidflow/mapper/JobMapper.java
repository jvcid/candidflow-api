package br.com.candidflow.mapper;

import br.com.candidflow.dto.request.JobRequestDTO;
import br.com.candidflow.dto.response.JobResponseDTO;
import br.com.candidflow.entity.Job;
import br.com.candidflow.enums.JobStatus;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {

    public Job toEntity(JobRequestDTO request) {
        return Job.builder()
                .title(request.getTitle().trim())
                .company(request.getCompany().trim())
                .description(request.getDescription().trim())
                .requirements(normalizeOptional(request.getRequirements()))
                .location(normalizeOptional(request.getLocation()))
                .workModel(request.getWorkModel())
                .status(request.getStatus() == null ? JobStatus.OPEN : request.getStatus())
                .build();
    }

    public void updateEntity(Job job, JobRequestDTO request) {
        job.setTitle(request.getTitle().trim());
        job.setCompany(request.getCompany().trim());
        job.setDescription(request.getDescription().trim());
        job.setRequirements(normalizeOptional(request.getRequirements()));
        job.setLocation(normalizeOptional(request.getLocation()));
        job.setWorkModel(request.getWorkModel());
        if (request.getStatus() != null) {
            job.setStatus(request.getStatus());
        }
    }

    public JobResponseDTO toResponse(Job job) {
        return JobResponseDTO.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .location(job.getLocation())
                .workModel(job.getWorkModel())
                .status(job.getStatus())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
