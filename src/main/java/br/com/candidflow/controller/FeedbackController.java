package br.com.candidflow.controller;

import br.com.candidflow.dto.request.FeedbackCreateRequestDTO;
import br.com.candidflow.dto.request.FeedbackUpdateRequestDTO;
import br.com.candidflow.dto.response.FeedbackResponseDTO;
import br.com.candidflow.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackResponseDTO> create(
            @Valid @RequestBody FeedbackCreateRequestDTO request
    ) {
        FeedbackResponseDTO response = feedbackService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<FeedbackResponseDTO>> findAll(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(feedbackService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(feedbackService.findById(id));
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<Page<FeedbackResponseDTO>> findByApplication(
            @PathVariable Long applicationId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(feedbackService.findByApplication(applicationId, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeedbackResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackUpdateRequestDTO request
    ) {
        return ResponseEntity.ok(feedbackService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        feedbackService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
