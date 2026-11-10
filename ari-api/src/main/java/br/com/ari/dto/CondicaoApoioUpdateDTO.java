/**
 * DTO utilizado pelo Front-end para atualizar as informações
 * de uma condição de apoio já cadastrada.
 */

package br.com.ari.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados editáveis de uma condição de apoio.", example = "{\"nmCondicao\":\"Atendimento em Libras\",\"dsCondicao\":\"Atendimento com intérprete de Libras.\"}")
public record CondicaoApoioUpdateDTO(

        @NotBlank(message = "Nome da condição é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nmCondicao,

        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres.")
        String dsCondicao

) {}
