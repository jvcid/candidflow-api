package br.com.candidflow.dto.request;

import br.com.candidflow.enums.ApplicationStatus;
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
public class ApplicationUpdateRequestDTO {

    @NotNull(message = "O status é obrigatório")
    private ApplicationStatus status;

    @Size(max = 5000, message = "A carta de apresentação deve ter no máximo 5000 caracteres")
    private String coverLetter;
}
