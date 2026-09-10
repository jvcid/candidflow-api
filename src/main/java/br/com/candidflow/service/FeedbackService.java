package br.com.candidflow.service;

import br.com.candidflow.dto.request.FeedbackCreateRequestDTO;
import br.com.candidflow.dto.request.FeedbackUpdateRequestDTO;
import br.com.candidflow.dto.response.FeedbackResponseDTO;
import br.com.candidflow.entity.Feedback;
import br.com.candidflow.entity.JobApplication;
import br.com.candidflow.exception.ResourceNotFoundException;
import br.com.candidflow.mapper.FeedbackMapper;
import br.com.candidflow.repository.ApplicationRepository;
import br.com.candidflow.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ApplicationRepository applicationRepository;
    private final FeedbackMapper feedbackMapper;

    @Transactional
    public FeedbackResponseDTO create(FeedbackCreateRequestDTO request) {
        JobApplication application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidatura", request.getApplicationId()));
        Feedback feedback = feedbackMapper.toEntity(request, application);
        return feedbackMapper.toResponse(feedbackRepository.saveAndFlush(feedback));
    }

    @Transactional(readOnly = true)
    public Page<FeedbackResponseDTO> findAll(Pageable pageable) {
        return feedbackRepository.findAll(pageable).map(feedbackMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public FeedbackResponseDTO findById(Long id) {
        return feedbackMapper.toResponse(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Page<FeedbackResponseDTO> findByApplication(Long applicationId, Pageable pageable) {
        if (!applicationRepository.existsById(applicationId)) {
            throw new ResourceNotFoundException("Candidatura", applicationId);
        }
        return feedbackRepository.findByApplicationId(applicationId, pageable)
                .map(feedbackMapper::toResponse);
    }

    @Transactional
    public FeedbackResponseDTO update(Long id, FeedbackUpdateRequestDTO request) {
        Feedback feedback = findEntityById(id);
        feedback.setContent(request.getContent().trim());
        return feedbackMapper.toResponse(feedbackRepository.saveAndFlush(feedback));
    }

    @Transactional
    public void delete(Long id) {
        feedbackRepository.delete(findEntityById(id));
    }

    private Feedback findEntityById(Long id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", id));
    }
}
