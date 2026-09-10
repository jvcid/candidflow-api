package br.com.candidflow.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationCreateRequestDTO {

    @NotNull(message = "O candidato é obrigatório")
    @Positive(message = "O identificador do candidato deve ser positivo")
    private Long candidateId;

    @NotNull(message = "A vaga é obrigatória")
    @Positive(message = "O identificador da vaga deve ser positivo")
    private Long jobId;

    @Size(max = 5000, message = "A carta de apresentação deve ter no máximo 5000 caracteres")
    private String coverLetter;
}
