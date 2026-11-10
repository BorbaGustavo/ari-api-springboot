/**
 * DTO utilizado pelo Front-end para enviar novas condições
 * de apoio oferecidas pelo estabelecimento.
 */

package br.com.ari.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de uma condição de apoio oferecida pelo estabelecimento.")
public record CondicaoApoioRequestDTO(

        @NotBlank(message = "Nome da condição é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        @Schema(description = "Nome da condição.", example = "Atendimento em Libras", requiredMode = Schema.RequiredMode.REQUIRED)
        String nmCondicao,

        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres.")
        @Schema(description = "Detalhes da condição.", example = "Atendimento com intérprete de Libras.")
        String dsCondicao

) {}
