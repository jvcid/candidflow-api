package br.com.candidflow.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
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
public class CandidateRequestDTO {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
    private String name;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Informe um e-mail válido")
    @Size(max = 180, message = "O e-mail deve ter no máximo 180 caracteres")
    private String email;

    @Size(max = 30, message = "O telefone deve ter no máximo 30 caracteres")
    private String phone;

    @Pattern(regexp = "[0-9]{5}-?[0-9]{3}", message = "Informe um CEP com 8 digitos")
    private String cep;

    @Size(max = 500, message = "A URL do LinkedIn deve ter no máximo 500 caracteres")
    private String linkedinUrl;

    @Size(max = 500, message = "A URL do GitHub deve ter no máximo 500 caracteres")
    private String githubUrl;
}
