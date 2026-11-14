
package br.com.ari.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastrar um funcionário.")
public record FuncionarioRequestDTO(

        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String nmFuncionario,

        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
        String nrTelefone,

        @NotBlank(message = "E-mail de login é obrigatório.")
        @Email(message = "E-mail inválido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        String dsEmailLogin,

        @NotBlank(message = "Senha é obrigatória.")
        @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres.")
        String dsSenha

) {}
