package br.com.candidflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackUpdateRequestDTO {

    @NotBlank(message = "O conteúdo do feedback é obrigatório")
    @Size(max = 5000, message = "O feedback deve ter no máximo 5000 caracteres")
    private String content;
}
