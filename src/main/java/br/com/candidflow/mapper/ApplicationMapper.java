package br.com.candidflow.mapper;

import br.com.candidflow.dto.request.ApplicationCreateRequestDTO;
import br.com.candidflow.dto.request.ApplicationUpdateRequestDTO;
import br.com.candidflow.dto.response.ApplicationResponseDTO;
import br.com.candidflow.entity.Candidate;
import br.com.candidflow.entity.Job;
import br.com.candidflow.entity.JobApplication;
import br.com.candidflow.enums.ApplicationStatus;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {

    public JobApplication toEntity(ApplicationCreateRequestDTO request, Candidate candidate, Job job) {
        return JobApplication.builder()
                .candidate(candidate)
                .job(job)
                .coverLetter(normalizeOptional(request.getCoverLetter()))
                .status(ApplicationStatus.SUBMITTED)
                .build();
    }

    public void updateEntity(JobApplication application, ApplicationUpdateRequestDTO request) {
        application.setStatus(request.getStatus());
        application.setCoverLetter(normalizeOptional(request.getCoverLetter()));
    }

    public ApplicationResponseDTO toResponse(JobApplication application) {
        return ApplicationResponseDTO.builder()
                .id(application.getId())
                .candidateId(application.getCandidate().getId())
                .candidateName(application.getCandidate().getName())
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle())
                .coverLetter(application.getCoverLetter())
                .status(application.getStatus())
                .appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
