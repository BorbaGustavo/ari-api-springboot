
package br.com.ari.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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
        String dsSenha,

        @NotBlank(message = "Perfil é obrigatório.")
        @Pattern(
                regexp = "ADMIN|ATENDENTE",
                message = "Perfil deve ser ADMIN ou ATENDENTE."
        )
        String tpPerfil,

        @Pattern(
                regexp = "S|N",
                message = "Treinamento deve ser S ou N."
        )
        String flTreinado

) {}
