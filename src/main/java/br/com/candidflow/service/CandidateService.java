package br.com.candidflow.service;

import br.com.candidflow.dto.request.CandidateRequestDTO;
import br.com.candidflow.dto.response.CandidateResponseDTO;
import br.com.candidflow.entity.Candidate;
import br.com.candidflow.exception.BusinessRuleException;
import br.com.candidflow.exception.ResourceNotFoundException;
import br.com.candidflow.mapper.CandidateMapper;
import br.com.candidflow.repository.ApplicationRepository;
import br.com.candidflow.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final ApplicationRepository applicationRepository;
    private final CandidateMapper candidateMapper;

    @Transactional
    public CandidateResponseDTO create(CandidateRequestDTO request) {
        String email = candidateMapper.normalizeEmail(request.getEmail());
        if (candidateRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException("Já existe um candidato cadastrado com esse e-mail");
        }

        Candidate candidate = candidateMapper.toEntity(request);
        return candidateMapper.toResponse(candidateRepository.saveAndFlush(candidate));
    }

    @Transactional(readOnly = true)
    public Page<CandidateResponseDTO> findAll(Pageable pageable) {
        return candidateRepository.findAll(pageable).map(candidateMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CandidateResponseDTO findById(Long id) {
        return candidateMapper.toResponse(findEntityById(id));
    }

    @Transactional
    public CandidateResponseDTO update(Long id, CandidateRequestDTO request) {
        Candidate candidate = findEntityById(id);
        String email = candidateMapper.normalizeEmail(request.getEmail());
        if (candidateRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new BusinessRuleException("Já existe outro candidato cadastrado com esse e-mail");
        }

        candidateMapper.updateEntity(candidate, request);
        return candidateMapper.toResponse(candidateRepository.saveAndFlush(candidate));
    }

    @Transactional
    public void delete(Long id) {
        Candidate candidate = findEntityById(id);
        if (applicationRepository.existsByCandidateId(id)) {
            throw new BusinessRuleException("Não é possível excluir um candidato que possui candidaturas");
        }
        candidateRepository.delete(candidate);
    }

    private Candidate findEntityById(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidato", id));
    }
}
