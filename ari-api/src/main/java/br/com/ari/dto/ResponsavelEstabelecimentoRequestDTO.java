package br.com.ari.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados do primeiro administrador do estabelecimento.")
public record ResponsavelEstabelecimentoRequestDTO(

        @NotBlank(message = "Nome do responsável é obrigatório.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        @Schema(description = "Nome completo do responsável.", example = "Gabriela Souza", requiredMode = Schema.RequiredMode.REQUIRED)
        String nmResponsavel,

        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
        @Schema(description = "Telefone do responsável.", example = "11999999999")
        String nrTelefone,

        @NotBlank(message = "E-mail de login do responsável é obrigatório.")
        @Email(message = "E-mail de login do responsável inválido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        @Schema(description = "E-mail usado para o login administrativo.", example = "gabriela@ari.demo", requiredMode = Schema.RequiredMode.REQUIRED)
        String dsEmailLogin,

        @NotBlank(message = "Senha do responsável é obrigatória.")
        @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres.")
        @Schema(description = "Senha do primeiro administrador.", example = "senhasenha", requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
        String dsSenha
) {}
