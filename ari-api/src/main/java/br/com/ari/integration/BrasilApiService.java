package br.com.ari.integration;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class BrasilApiService {

    private final RestClient restClient;

    public BrasilApiService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://brasilapi.com.br/api")
                .build();
    }

    public BrasilApiCnpjResponse consultarCnpj(String cnpj) {
        try {
            BrasilApiCnpjResponse response = restClient.get()
                    .uri("/cnpj/v1/{cnpj}", cnpj)
                    .retrieve()
                    .body(BrasilApiCnpjResponse.class);

            if (response == null) {
                throw new IllegalStateException(
                        "A Brasil API não retornou dados para o CNPJ."
                );
            }

            return response;
        } catch (RestClientException e) {
            throw new IllegalArgumentException(
                    "Não foi possível consultar o CNPJ na Brasil API.",
                    e
            );
        }
    }

    public BrasilApiCepResponse consultarCep(String cep) {
        try {
            BrasilApiCepResponse response = restClient.get()
                    .uri("/cep/v2/{cep}", cep)
                    .retrieve()
                    .body(BrasilApiCepResponse.class);

            if (response == null) {
                throw new IllegalStateException(
                        "A Brasil API não retornou dados para o CEP."
                );
            }

            return response;
        } catch (RestClientException e) {
            throw new IllegalArgumentException(
                    "Não foi possível consultar o CEP na Brasil API.",
                    e
            );
        }
    }
}
