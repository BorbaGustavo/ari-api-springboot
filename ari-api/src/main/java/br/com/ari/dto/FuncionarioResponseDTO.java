
package br.com.ari.dto;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de retorno de um funcionario.")
public record FuncionarioResponseDTO(

        @Schema(description = "Identificador do funcionario.", example = "1")
        Long cdFuncionario,
        @Schema(description = "Identificador do estabelecimento vinculado.", example = "1")
        Long cdEstabelecimento,
        @Schema(description = "Nome completo do funcionario.", example = "Carlos Silva")
        String nmFuncionario,
        @Schema(description = "Telefone para contato.", example = "11999999999")
        String nrTelefone,
        @Schema(description = "E-mail utilizado para login.", example = "carlos@example.com")
        String dsEmailLogin,
        @Schema(description = "Perfil de acesso do funcionario.", example = "FUNCIONARIO", allowableValues = {"ADMIN", "FUNCIONARIO"})
        String tpPerfil,
        @Schema(description = "Indica se o funcionario esta treinado.", example = "S", allowableValues = {"S", "N"})
        String flTreinado,
        @Schema(description = "Situacao do funcionario.", example = "ATIVO", allowableValues = {"ATIVO", "INATIVO"})
        String stFuncionario,
        @Schema(description = "Data e hora do cadastro.", example = "2026-11-18T10:00:00")
        LocalDateTime dtCadastro

) {}
