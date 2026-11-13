
package br.com.ari.controller;

import br.com.ari.dto.CondicaoApoioRequestDTO;
import br.com.ari.dto.CondicaoApoioUpdateDTO;
import br.com.ari.dto.CondicaoApoioResponseDTO;
import br.com.ari.dto.CondicaoApoioSelecaoDTO;

import br.com.ari.exception.NaoAutenticadoException;
import br.com.ari.service.CondicaoApoioService;
import br.com.ari.service.FuncionarioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/condicoes-apoio")
@Tag(name = "Condições de apoio", description = "Apoios oferecidos pelos estabelecimentos.")
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
    @Operation(
            summary = "Listar condições públicas",
            description = "Retorna somente as condições ativas de um estabelecimento."
    )
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
    @Operation(
            summary = "Listar condições do estabelecimento",
            description = "Lista condições ativas e inativas do estabelecimento do administrador."
    )
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
    @Operation(
            summary = "Cadastrar condição de apoio",
            description = "Cadastra uma condição de apoio para o estabelecimento."
    )
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
    @Operation(
            summary = "Atualizar condição de apoio",
            description = "Atualiza a categoria, o nome e a descrição de uma condição."
    )
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
    // SALVAR SELEÇÕES DOS CHECKBOXES
    // =====================================================

    // Somente ADMIN. Salva todas as condições selecionadas
    // e desativa as demais condições do estabelecimento.
    @PutMapping("/selecoes")
    @Operation(
            summary = "Salvar condições selecionadas",
            description = "Atualiza em conjunto as condições oferecidas pelo estabelecimento."
    )
    public ResponseEntity<List<CondicaoApoioResponseDTO>> atualizarSelecoes(
            Principal principal,
            @Valid @RequestBody CondicaoApoioSelecaoDTO dto) {

        Long cdFuncionarioAdmin = obterAdminId(principal);

        List<CondicaoApoioResponseDTO> resposta =
                condicaoApoioService.atualizarSelecoes(
                        cdFuncionarioAdmin,
                        dto.idsSelecionados()
                );

        return ResponseEntity.ok(resposta);
    }

    // =====================================================
    // IDENTIFICAR ADMIN AUTENTICADO
    // =====================================================

    private Long obterAdminId(Principal principal) {

        if (principal == null || principal.getName() == null) {
            throw new NaoAutenticadoException(
                    "Funcionário não autenticado."
            );
        }

        return funcionarioService.buscarIdAdminAutenticado(
                principal.getName()
        );
    }

}
