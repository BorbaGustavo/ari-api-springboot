package br.com.ari.controller;

import br.com.ari.integration.BrasilApiCepResponse;
import br.com.ari.integration.BrasilApiCnpjResponse;
import br.com.ari.integration.BrasilApiService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/integracoes")
@Tag(name = "Integrações", description = "Consultas de CEP e CNPJ realizadas pela Brasil API.")
public class BrasilApiController {

    private final BrasilApiService brasilApiService;

    public BrasilApiController(BrasilApiService brasilApiService) {
        this.brasilApiService = brasilApiService;
    }

    @GetMapping("/cnpj/{cnpj}")
    @Operation(summary = "Consultar CNPJ", description = "Consulta dados de um CNPJ na Brasil API. Informe exatamente 14 dígitos, sem pontuação.")
    public ResponseEntity<BrasilApiCnpjResponse> consultarCnpj(
            @Parameter(description = "CNPJ com 14 dígitos.", example = "12345678000195")
            @PathVariable String cnpj) {

        validarNumeros(cnpj, 14, "CNPJ");
        return ResponseEntity.ok(brasilApiService.consultarCnpj(cnpj));
    }

    @GetMapping("/cep/{cep}")
    @Operation(summary = "Consultar CEP", description = "Consulta endereço de um CEP na Brasil API. Informe exatamente 8 dígitos, sem pontuação.")
    public ResponseEntity<BrasilApiCepResponse> consultarCep(
            @Parameter(description = "CEP com 8 dígitos.", example = "01310100")
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
