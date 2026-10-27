package br.com.candidflow.controller;

import br.com.candidflow.dto.request.CandidateRequestDTO;
import br.com.candidflow.dto.response.CandidateResponseDTO;
import br.com.candidflow.dto.response.CandidateAddressResponseDTO;
import br.com.candidflow.service.AddressService;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import br.com.candidflow.service.CandidateService;
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
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;
    private final AddressService addressService;

    @GetMapping("/{id}/address")
    public ResponseEntity<CandidateAddressResponseDTO> address(@PathVariable Long id) {
        var candidate = candidateService.findById(id);
        var address = addressService.findByCep(candidate.getCep());
        return ResponseEntity.ok(new CandidateAddressResponseDTO(candidate, List.of(address)));
    }

    @GetMapping("/{id}/address/search")
    public ResponseEntity<CandidateAddressResponseDTO> searchAddress(
            @PathVariable Long id,
            @RequestParam(defaultValue = "") String uf,
            @RequestParam(defaultValue = "") String city,
            @RequestParam(defaultValue = "") String street) {
        var candidate = candidateService.findById(id);
        return ResponseEntity.ok(new CandidateAddressResponseDTO(
                candidate, addressService.search(uf, city, street)));
    }


    @PostMapping
    public ResponseEntity<CandidateResponseDTO> create(@Valid @RequestBody CandidateRequestDTO request) {
        CandidateResponseDTO response = candidateService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<CandidateResponseDTO>> findAll(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(candidateService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidateResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CandidateRequestDTO request
    ) {
        return ResponseEntity.ok(candidateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        candidateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
