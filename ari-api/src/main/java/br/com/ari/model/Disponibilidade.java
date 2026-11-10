
package br.com.ari.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Disponibilidade {

    private Long cdDisponibilidade;
    private Long cdEstabelecimento;
    private Integer nrDiaSemana;
    private Integer nrPeriodo;
    private String hrInicio;
    private String hrFim;
    private String flAtivo;
    private LocalDateTime dtCadastro;
}
