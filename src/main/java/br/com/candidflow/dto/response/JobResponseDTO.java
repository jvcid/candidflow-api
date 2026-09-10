package br.com.candidflow.dto.response;

import br.com.candidflow.enums.JobStatus;
import br.com.candidflow.enums.WorkModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponseDTO {
    private Long id;
    private String title;
    private String company;
    private String description;
    private String requirements;
    private String location;
    private WorkModel workModel;
    private JobStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
