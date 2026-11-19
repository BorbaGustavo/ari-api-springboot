/**
 * DTO utilizado pelo Back-end para retornar ao Front-end
 * as informações das condições de apoio cadastradas.
 */

package br.com.ari.dto;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de retorno de uma condicao de apoio cadastrada.")
public record CondicaoApoioResponseDTO(

        @Schema(description = "Identificador da condicao de apoio.", example = "1")
        Long cdCondicaoApoio,
        @Schema(description = "Identificador do estabelecimento.", example = "1")
        Long cdEstabelecimento,
        @Schema(description = "Categoria da condicao.", example = "CONDICAO", allowableValues = {"CONDICAO", "APOIO_ADICIONAL"})
        String tpCategoria,
        @Schema(description = "Nome da condicao.", example = "Atendimento em Libras")
        String nmCondicao,
        @Schema(description = "Detalhes da condicao.", example = "Atendimento com interprete de Libras.")
        String dsCondicao,
        @Schema(description = "Indica se a condicao esta ativa.", example = "S", allowableValues = {"S", "N"})
        String flAtivo,
        @Schema(description = "Data e hora do cadastro.", example = "2026-11-18T10:00:00")
        LocalDateTime dtCadastro

) {}
