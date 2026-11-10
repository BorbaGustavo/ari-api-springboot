
package br.com.ari.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credenciais para login de usuária.")
public record LoginRequestDTO(

        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        @Schema(description = "E-mail cadastrado.", example = "ana@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String dsEmail,

        @NotBlank(message = "Senha é obrigatória.")
        @Schema(description = "Senha da usuária.", example = "senha-segura", requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
        String dsSenha

) {}
