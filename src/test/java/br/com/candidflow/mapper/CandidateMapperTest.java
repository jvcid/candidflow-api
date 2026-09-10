package br.com.candidflow.mapper;

import br.com.candidflow.dto.request.CandidateRequestDTO;
import br.com.candidflow.entity.Candidate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CandidateMapperTest {

    private final CandidateMapper mapper = new CandidateMapper();

    @Test
    void shouldNormalizeCandidateInput() {
        CandidateRequestDTO request = CandidateRequestDTO.builder()
                .name("  João Victor  ")
                .email("  JOAO@EXAMPLE.COM  ")
                .phone("   ")
                .build();

        Candidate candidate = mapper.toEntity(request);

        assertEquals("João Victor", candidate.getName());
        assertEquals("joao@example.com", candidate.getEmail());
        assertNull(candidate.getPhone());
    }
}
