
package br.com.ari.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados necessários para criar uma usuária.")
public record UsuarioRequestDTO(

        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        @Schema(description = "Nome completo.", example = "Ana Souza", requiredMode = Schema.RequiredMode.REQUIRED)
        String nmUsuario,

        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
        @Schema(description = "Telefone para contato.", example = "11999999999")
        String nrTelefone,

        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        @Schema(description = "E-mail da usuária.", example = "ana@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String dsEmail,

        @NotBlank(message = "Senha é obrigatória.")
        @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres.")
        @Schema(description = "Senha com no mínimo 8 caracteres.", example = "senha-segura", requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
        String dsSenha

) {}
