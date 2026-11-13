/**
 * DTO utilizado pelo Back-end para retornar ao Front-end
 * as informações das condições de apoio cadastradas.
 */

package br.com.ari.dto;

import java.time.LocalDateTime;

public record CondicaoApoioResponseDTO(

        Long cdCondicaoApoio,
        Long cdEstabelecimento,
        String tpCategoria,
        String nmCondicao,
        String dsCondicao,
        String flAtivo,
        LocalDateTime dtCadastro

) {}
