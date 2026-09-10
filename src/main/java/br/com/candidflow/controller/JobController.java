package br.com.candidflow.controller;

import br.com.candidflow.dto.request.JobRequestDTO;
import br.com.candidflow.dto.response.JobResponseDTO;
import br.com.candidflow.service.JobService;
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
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponseDTO> create(@Valid @RequestBody JobRequestDTO request) {
        JobResponseDTO response = jobService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<JobResponseDTO>> findAll(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(jobService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody JobRequestDTO request
    ) {
        return ResponseEntity.ok(jobService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        jobService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
