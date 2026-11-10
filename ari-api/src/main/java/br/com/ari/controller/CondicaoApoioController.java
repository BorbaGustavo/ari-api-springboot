
package br.com.ari.controller;

import br.com.ari.dto.CondicaoApoioRequestDTO;
import br.com.ari.dto.CondicaoApoioUpdateDTO;
import br.com.ari.dto.CondicaoApoioResponseDTO;
import br.com.ari.dto.CondicaoApoioStatusDTO;

import br.com.ari.service.CondicaoApoioService;
import br.com.ari.service.FuncionarioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/condicoes-apoio")
public class CondicaoApoioController {

    private final CondicaoApoioService condicaoApoioService;
    private final FuncionarioService funcionarioService;

    // CONSTRUTOR
    public CondicaoApoioController(
            CondicaoApoioService condicaoApoioService,
            FuncionarioService funcionarioService) {

        this.condicaoApoioService = condicaoApoioService;
        this.funcionarioService = funcionarioService;
    }

    // =====================================================
    // CONSULTAR CONDIÇÕES PÚBLICAS
    // =====================================================

    // Utilizado pelo Front-end para apresentar os apoios oferecidos.
    @GetMapping("/estabelecimento/{id}/publicas")
    public ResponseEntity<List<CondicaoApoioResponseDTO>>
    buscarAtivasPublicas(@PathVariable Long id) {

        List<CondicaoApoioResponseDTO> condicoes =
                condicaoApoioService.buscarAtivasPublicas(id);

        return ResponseEntity.ok(condicoes);
    }

    // =====================================================
    // LISTAR CONDIÇÕES DA EMPRESA
    // =====================================================

    // Somente ADMIN. Inclui condições ativas e inativas.
    @GetMapping
    public ResponseEntity<List<CondicaoApoioResponseDTO>>
    buscarPorEstabelecimento(Principal principal) {

        Long cdFuncionarioAdmin = obterAdminId(principal);

        List<CondicaoApoioResponseDTO> condicoes =
                condicaoApoioService.buscarPorEstabelecimento(
                        cdFuncionarioAdmin
                );

        return ResponseEntity.ok(condicoes);
    }

    // =====================================================
    // CADASTRAR CONDIÇÃO DE APOIO
    // =====================================================

    @PostMapping
    public ResponseEntity<CondicaoApoioResponseDTO> criar(
            Principal principal,
            @Valid @RequestBody CondicaoApoioRequestDTO dto) {

        Long cdFuncionarioAdmin = obterAdminId(principal);

        CondicaoApoioResponseDTO resposta =
                condicaoApoioService.criar(
                        cdFuncionarioAdmin,
                        dto
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resposta);
    }

    // =====================================================
    // ATUALIZAR CONDIÇÃO DE APOIO
    // =====================================================

    @PutMapping("/{id}")
    public ResponseEntity<CondicaoApoioResponseDTO> atualizar(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody CondicaoApoioUpdateDTO dto) {

        Long cdFuncionarioAdmin = obterAdminId(principal);

        CondicaoApoioResponseDTO resposta =
                condicaoApoioService.atualizar(
                        cdFuncionarioAdmin,
                        id,
                        dto
                );

        return ResponseEntity.ok(resposta);
    }

    // =====================================================
    // ATIVAR OU DESATIVAR CONDIÇÃO DE APOIO
    // =====================================================

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> atualizarStatus(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody CondicaoApoioStatusDTO dto) {

        Long cdFuncionarioAdmin = obterAdminId(principal);

        if ("S".equals(dto.flAtivo())) {

            condicaoApoioService.ativar(
                    cdFuncionarioAdmin,
                    id
            );

        } else {

            condicaoApoioService.desativar(
                    cdFuncionarioAdmin,
                    id
            );
        }

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // IDENTIFICAR ADMIN AUTENTICADO
    // =====================================================

    private Long obterAdminId(Principal principal) {

        if (principal == null || principal.getName() == null) {
            throw new IllegalStateException(
                    "Funcionário não autenticado."
            );
        }

        return funcionarioService.buscarIdAdminAutenticado(
                principal.getName()
        );
    }

}
