package br.com.ari.integration;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BrasilApiCepResponse(
        String cep,
        @JsonProperty("state") String uf,
        String city,
        String neighborhood,
        String street
) {}
