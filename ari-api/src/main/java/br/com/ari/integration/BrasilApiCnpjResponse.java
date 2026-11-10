package br.com.ari.integration;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BrasilApiCnpjResponse(
        String cnpj,
        @JsonProperty("razao_social") String razaoSocial,
        @JsonProperty("nome_fantasia") String nomeFantasia,
        @JsonProperty("descricao_situacao_cadastral")
        String descricaoSituacaoCadastral,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String municipio,
        String uf,
        String cep
) {}
