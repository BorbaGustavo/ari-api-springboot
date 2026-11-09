package br.com.ari.dto;

import java.math.BigDecimal;

public record AvisoChegadaRequestDTO(
        String dsUuid,
        Long cdEstabelecimento,
        BigDecimal nrDistanciaMetros,
        Long nrTempoEstimadoMin
) {}