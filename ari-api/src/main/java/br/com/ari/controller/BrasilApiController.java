package br.com.ari.controller;

import br.com.ari.integration.BrasilApiCepResponse;
import br.com.ari.integration.BrasilApiCnpjResponse;
import br.com.ari.integration.BrasilApiService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/integracoes")
public class BrasilApiController {

    private final BrasilApiService brasilApiService;

    public BrasilApiController(BrasilApiService brasilApiService) {
        this.brasilApiService = brasilApiService;
    }

    @GetMapping("/cnpj/{cnpj}")
    public ResponseEntity<BrasilApiCnpjResponse> consultarCnpj(
            @PathVariable String cnpj) {

        validarNumeros(cnpj, 14, "CNPJ");
        return ResponseEntity.ok(brasilApiService.consultarCnpj(cnpj));
    }

    @GetMapping("/cep/{cep}")
    public ResponseEntity<BrasilApiCepResponse> consultarCep(
            @PathVariable String cep) {

        validarNumeros(cep, 8, "CEP");
        return ResponseEntity.ok(brasilApiService.consultarCep(cep));
    }

    private void validarNumeros(String valor, int tamanho, String campo) {
        if (valor == null || !valor.matches("\\d{" + tamanho + "}")) {
            throw new IllegalArgumentException(
                    campo + " deve conter " + tamanho + " números."
            );
        }
    }
}
