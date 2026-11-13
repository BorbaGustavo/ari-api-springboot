
package br.com.ari.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

/**
 * Model de apoio para representar as condições de acolhimento
 * oferecidas por cada estabelecimento.
 */
public class CondicaoApoio {

    private Long cdCondicaoApoio;

    private Long cdEstabelecimento;

    private String tpCategoria;

    private String nmCondicao;

    private String dsCondicao;

    private String flAtivo;

    private LocalDateTime dtCadastro;
}
