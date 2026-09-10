package br.com.candidflow.dto.request;

import br.com.candidflow.enums.JobStatus;
import br.com.candidflow.enums.WorkModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRequestDTO {

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
    private String title;

    @NotBlank(message = "A empresa é obrigatória")
    @Size(max = 150, message = "A empresa deve ter no máximo 150 caracteres")
    private String company;

    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 10000, message = "A descrição deve ter no máximo 10000 caracteres")
    private String description;

    @Size(max = 10000, message = "Os requisitos devem ter no máximo 10000 caracteres")
    private String requirements;

    @Size(max = 150, message = "A localização deve ter no máximo 150 caracteres")
    private String location;

    @NotNull(message = "O modelo de trabalho é obrigatório")
    private WorkModel workModel;

    private JobStatus status;
}
