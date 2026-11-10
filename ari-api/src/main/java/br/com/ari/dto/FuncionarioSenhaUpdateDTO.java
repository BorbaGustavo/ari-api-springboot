
package br.com.ari.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Nova senha do funcionário.", example = "{\"dsNovaSenha\":\"nova-senha-segura\"}")
public record FuncionarioSenhaUpdateDTO(

        @NotBlank(message = "Nova senha é obrigatória.")
        @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres.")
        String dsNovaSenha

) {}
