
package br.com.ari.controller;

import br.com.ari.dto.LoginRequestDTO;
import br.com.ari.dto.LoginResponseDTO;
import br.com.ari.dto.UsuarioRequestDTO;

import br.com.ari.model.SessaoWeb;

import br.com.ari.service.UsuarioService;
import br.com.ari.service.SessaoWebService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final SessaoWebService sessaoWebService;

    // CONSTRUTOR
    public UsuarioController(
            UsuarioService usuarioService,
            SessaoWebService sessaoWebService) {

        this.usuarioService = usuarioService;
        this.sessaoWebService = sessaoWebService;
    }

    // =====================================================
    // CADASTRAR USUÁRIO
    // =====================================================

    @PostMapping
    public ResponseEntity<Void> criar(
            @Valid @RequestBody UsuarioRequestDTO dto) {

        usuarioService.criar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    // =====================================================
    // LOGIN
    // =====================================================

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> autenticar(
            @Valid @RequestBody LoginRequestDTO dto) {

        LoginResponseDTO resposta = usuarioService.autenticar(dto);

        return ResponseEntity.ok(resposta);
    }

    // =====================================================
    // LOGOUT
    // =====================================================

    @PostMapping("/logout")
    public ResponseEntity<Void> sair(
            @RequestHeader("X-Session-UUID") String uuid) {

        SessaoWeb sessao = obterSessaoValida(uuid);

        sessaoWebService.revogar(
                sessao.getCdSessaoWeb()
        );

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // VALIDAR SESSÃO
    // =====================================================

    private SessaoWeb obterSessaoValida(String uuid) {

        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException(
                    "Token de sessão não informado."
            );
        }

        return sessaoWebService.buscarSessaoAtiva(uuid)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sessão inválida ou expirada. "
                                        + "Faça login novamente."
                        )
                );
    }

}
