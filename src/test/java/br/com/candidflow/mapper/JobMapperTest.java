package br.com.candidflow.mapper;

import br.com.candidflow.dto.request.JobRequestDTO;
import br.com.candidflow.entity.Job;
import br.com.candidflow.enums.JobStatus;
import br.com.candidflow.enums.WorkModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JobMapperTest {

    private final JobMapper mapper = new JobMapper();

    @Test
    void shouldCreateOpenJobByDefault() {
        JobRequestDTO request = JobRequestDTO.builder()
                .title("  Desenvolvedor Java  ")
                .company("  CandidFlow  ")
                .description("  Desenvolvimento de APIs REST  ")
                .workModel(WorkModel.REMOTE)
                .build();

        Job job = mapper.toEntity(request);

        assertEquals("Desenvolvedor Java", job.getTitle());
        assertEquals("CandidFlow", job.getCompany());
        assertEquals(JobStatus.OPEN, job.getStatus());
    }
}
