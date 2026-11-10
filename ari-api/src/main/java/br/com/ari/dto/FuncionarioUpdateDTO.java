
package br.com.ari.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados editáveis do funcionário.", example = "{\"nmFuncionario\":\"Carlos Lima\",\"nrTelefone\":\"11988887777\",\"dsEmailLogin\":\"carlos@example.com\",\"tpPerfil\":\"ATENDENTE\",\"flTreinado\":\"S\"}")
public record FuncionarioUpdateDTO(

        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String nmFuncionario,

        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
        String nrTelefone,

        @NotBlank(message = "E-mail de login é obrigatório.")
        @Email(message = "E-mail inválido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        String dsEmailLogin,

        @NotBlank(message = "Perfil é obrigatório.")
        @Pattern(
                regexp = "ADMIN|ATENDENTE",
                message = "Perfil deve ser ADMIN ou ATENDENTE."
        )
        String tpPerfil,

        @NotBlank(message = "Informe a situação do treinamento.")
        @Pattern(
                regexp = "S|N",
                message = "Treinamento deve ser S ou N."
        )
        String flTreinado

) {}
