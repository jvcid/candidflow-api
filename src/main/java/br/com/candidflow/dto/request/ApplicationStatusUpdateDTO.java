package br.com.candidflow.dto.request;

import br.com.candidflow.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusUpdateDTO {

    @NotNull(message = "O status é obrigatório")
    private ApplicationStatus status;
}
