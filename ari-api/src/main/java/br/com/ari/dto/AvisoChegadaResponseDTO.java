package br.com.ari.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AvisoChegadaResponseDTO(
        Long cdAvisoChegada,
        Long cdEstabelecimento,
        Long cdFuncionario,
        LocalDateTime dtAviso,
        String stAviso,
        BigDecimal nrDistanciaMetros,
        Long nrTempoEstimadoMin,
        LocalDateTime dtAcolhimento
) {}