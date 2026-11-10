
package br.com.ari.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para cadastrar um funcionário.")
public record FuncionarioRequestDTO(

        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        @Schema(description = "Nome completo.", example = "Carlos Lima", requiredMode = Schema.RequiredMode.REQUIRED)
        String nmFuncionario,

        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
        @Schema(description = "Telefone.", example = "11988887777")
        String nrTelefone,

        @NotBlank(message = "E-mail de login é obrigatório.")
        @Email(message = "E-mail inválido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        @Schema(description = "E-mail usado no login.", example = "carlos@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String dsEmailLogin,

        @NotBlank(message = "Senha é obrigatória.")
        @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres.")
        @Schema(description = "Senha com no mínimo 8 caracteres.", example = "senha-segura", requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
        String dsSenha,

        @NotBlank(message = "Perfil é obrigatório.")
        @Pattern(
                regexp = "ADMIN|ATENDENTE",
                message = "Perfil deve ser ADMIN ou ATENDENTE."
        )
        @Schema(description = "Perfil de acesso.", example = "ATENDENTE", allowableValues = {"ADMIN", "ATENDENTE"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String tpPerfil,

        @Pattern(
                regexp = "S|N",
                message = "Treinamento deve ser S ou N."
        )
        @Schema(description = "Indica se o funcionário foi treinado.", example = "S", allowableValues = {"S", "N"})
        String flTreinado

) {}
