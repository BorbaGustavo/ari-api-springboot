
package br.com.ari.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO utilizado pelo Front-end para solicitar
 * a alteração da senha da usuária autenticada.
 */
public record UsuarioSenhaUpdateDTO(

        @NotBlank(message = "Nova senha é obrigatória.")
        @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres.")
        String dsNovaSenha

) {}
