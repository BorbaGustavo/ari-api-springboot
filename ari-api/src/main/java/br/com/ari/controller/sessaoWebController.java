package br.com.ari.controller;

import br.com.ari.dto.SessaoWebResponseDTO;
import br.com.ari.model.SessaoWeb;
import br.com.ari.service.SessaoWebService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessoes")
@Tag(
        name = "Sessões web",
        description = "Criação de sessão anônima para acesso ao mapa público."
)
public class SessaoWebController {

    private final SessaoWebService sessaoWebService;

    public SessaoWebController(SessaoWebService sessaoWebService) {
        this.sessaoWebService = sessaoWebService;
    }

    @PostMapping("/anonima")
    @Operation(
            summary = "Criar sessão anônima",
            description = "Cria uma sessão sem vínculo com usuária para permitir acesso ao mapa. A sessão expira em 2 horas."
    )
    public ResponseEntity<SessaoWebResponseDTO> criarSessaoAnonima() {

        SessaoWeb sessao = sessaoWebService.criarAnonima();

        SessaoWebResponseDTO resposta = new SessaoWebResponseDTO(
                sessao.getDsUuid(),
                sessao.getDtExpiracao()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resposta);
    }
}
