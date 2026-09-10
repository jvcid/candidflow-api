package br.com.candidflow.controller;

import br.com.candidflow.dto.request.ApplicationCreateRequestDTO;
import br.com.candidflow.dto.request.ApplicationStatusUpdateDTO;
import br.com.candidflow.dto.request.ApplicationUpdateRequestDTO;
import br.com.candidflow.dto.response.ApplicationResponseDTO;
import br.com.candidflow.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationResponseDTO> create(
            @Valid @RequestBody ApplicationCreateRequestDTO request
    ) {
        ApplicationResponseDTO response = applicationService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ApplicationResponseDTO>> findAll(
            @PageableDefault(size = 20, sort = "appliedAt") Pageable pageable
    ) {
        return ResponseEntity.ok(applicationService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.findById(id));
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<Page<ApplicationResponseDTO>> findByCandidate(
            @PathVariable Long candidateId,
            @PageableDefault(size = 20, sort = "appliedAt") Pageable pageable
    ) {
        return ResponseEntity.ok(applicationService.findByCandidate(candidateId, pageable));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<Page<ApplicationResponseDTO>> findByJob(
            @PathVariable Long jobId,
            @PageableDefault(size = 20, sort = "appliedAt") Pageable pageable
    ) {
        return ResponseEntity.ok(applicationService.findByJob(jobId, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationUpdateRequestDTO request
    ) {
        return ResponseEntity.ok(applicationService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationStatusUpdateDTO request
    ) {
        return ResponseEntity.ok(applicationService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        applicationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
