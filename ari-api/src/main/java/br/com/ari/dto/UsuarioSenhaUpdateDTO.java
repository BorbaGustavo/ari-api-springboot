
package br.com.ari.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO utilizado pelo Front-end para solicitar
 * a alteração da senha da usuária autenticada.
 */
@Schema(description = "Nova senha da usuária.")
public record UsuarioSenhaUpdateDTO(

        @NotBlank(message = "Nova senha é obrigatória.")
        @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres.")
        @Schema(description = "Nova senha com no mínimo 8 caracteres.", example = "nova-senha-segura", requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
        String dsNovaSenha

) {}
