
package br.com.ari.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credenciais de login do funcionário.")
public record FuncionarioLoginDTO(

        @NotBlank(message = "E-mail de login é obrigatório.")
        @Email(message = "E-mail inválido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        @Schema(description = "E-mail de login.", example = "admin@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String dsEmailLogin,

        @NotBlank(message = "Senha é obrigatória.")
        @Schema(description = "Senha do funcionário.", example = "senha-segura", requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
        String dsSenha

) {}
