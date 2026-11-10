
package br.com.ari.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados editáveis da usuária.", example = "{\"nmUsuario\":\"Ana Souza\",\"nrTelefone\":\"11999999999\",\"dsEmail\":\"ana@example.com\"}")
public record UsuarioUpdateDTO(

        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String nmUsuario,

        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
        String nrTelefone,

        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        String dsEmail

) {}
