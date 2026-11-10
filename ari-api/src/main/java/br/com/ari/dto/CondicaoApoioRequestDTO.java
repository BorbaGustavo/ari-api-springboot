/**
 * DTO utilizado pelo Front-end para enviar novas condições
 * de apoio oferecidas pelo estabelecimento.
 */

package br.com.ari.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CondicaoApoioRequestDTO(

        @NotBlank(message = "Nome da condição é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nmCondicao,

        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres.")
        String dsCondicao

) {}
