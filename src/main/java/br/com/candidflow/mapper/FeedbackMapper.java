package br.com.candidflow.mapper;

import br.com.candidflow.dto.request.FeedbackCreateRequestDTO;
import br.com.candidflow.dto.response.FeedbackResponseDTO;
import br.com.candidflow.entity.Feedback;
import br.com.candidflow.entity.JobApplication;
import org.springframework.stereotype.Component;

@Component
public class FeedbackMapper {

    public Feedback toEntity(FeedbackCreateRequestDTO request, JobApplication application) {
        return Feedback.builder()
                .application(application)
                .content(request.getContent().trim())
                .build();
    }

    public FeedbackResponseDTO toResponse(Feedback feedback) {
        return FeedbackResponseDTO.builder()
                .id(feedback.getId())
                .applicationId(feedback.getApplication().getId())
                .content(feedback.getContent())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }
}
