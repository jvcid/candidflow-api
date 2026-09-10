package br.com.candidflow.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class FeedbackCreateRequestDTO {

    @NotNull(message = "A candidatura é obrigatória")
    @Positive(message = "O identificador da candidatura deve ser positivo")
    private Long applicationId;

    @NotBlank(message = "O conteúdo do feedback é obrigatório")
    @Size(max = 5000, message = "O feedback deve ter no máximo 5000 caracteres")
    private String content;
}
