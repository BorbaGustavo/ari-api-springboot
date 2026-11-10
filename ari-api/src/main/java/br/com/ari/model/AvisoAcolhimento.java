
package br.com.ari.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvisoAcolhimento {

    private Long cdAviso;

    private Long cdUsuario;
    private Long cdSessaoWeb;
    private Long cdEstabelecimento;
    private Long cdFuncionario;

    private LocalDateTime dtAviso;

    private String stAviso;

    private Integer nrTempoEstimado;

    private LocalDateTime dtAcolhimento;
}
