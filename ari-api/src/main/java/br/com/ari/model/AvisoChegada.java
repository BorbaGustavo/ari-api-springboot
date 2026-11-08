package br.com.ari.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvisoChegada {

    private Long cdAvisoChegada;
    private Long cdSessaoWeb;
    private Long cdEstabelecimento;
    private Long cdFuncionario;

    private LocalDateTime dtAviso;

    private String stAviso;

    private BigDecimal nrDistanciaMetros;
    private Long nrTempoEstimadoMin;

    private LocalDateTime dtAcolhimento;
}