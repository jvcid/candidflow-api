package br.com.candidflow.mapper;

import br.com.candidflow.dto.request.CandidateRequestDTO;
import br.com.candidflow.dto.response.CandidateResponseDTO;
import br.com.candidflow.entity.Candidate;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class CandidateMapper {

    public Candidate toEntity(CandidateRequestDTO request) {
        return Candidate.builder()
                .name(request.getName().trim())
                .email(normalizeEmail(request.getEmail()))
                .phone(normalizeOptional(request.getPhone()))
                .linkedinUrl(normalizeOptional(request.getLinkedinUrl()))
                .githubUrl(normalizeOptional(request.getGithubUrl()))
                .build();
    }

    public void updateEntity(Candidate candidate, CandidateRequestDTO request) {
        candidate.setName(request.getName().trim());
        candidate.setEmail(normalizeEmail(request.getEmail()));
        candidate.setPhone(normalizeOptional(request.getPhone()));
        candidate.setLinkedinUrl(normalizeOptional(request.getLinkedinUrl()));
        candidate.setGithubUrl(normalizeOptional(request.getGithubUrl()));
    }

    public CandidateResponseDTO toResponse(Candidate candidate) {
        return CandidateResponseDTO.builder()
                .id(candidate.getId())
                .name(candidate.getName())
                .email(candidate.getEmail())
                .phone(candidate.getPhone())
                .linkedinUrl(candidate.getLinkedinUrl())
                .githubUrl(candidate.getGithubUrl())
                .createdAt(candidate.getCreatedAt())
                .updatedAt(candidate.getUpdatedAt())
                .build();
    }

    public String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
