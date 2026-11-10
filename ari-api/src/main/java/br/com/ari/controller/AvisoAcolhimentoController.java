
package br.com.ari.controller;

import br.com.ari.dto.AvisoAcolhimentoRequestDTO;
import br.com.ari.dto.AvisoAcolhimentoResponseDTO;

import br.com.ari.service.AvisoAcolhimentoService;
import br.com.ari.service.FuncionarioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/avisos-acolhimento")
public class AvisoAcolhimentoController {

    private final AvisoAcolhimentoService avisoAcolhimentoService;
    private final FuncionarioService funcionarioService;

    // CONSTRUTOR
    public AvisoAcolhimentoController(
            AvisoAcolhimentoService avisoAcolhimentoService,
            FuncionarioService funcionarioService) {

        this.avisoAcolhimentoService = avisoAcolhimentoService;
        this.funcionarioService = funcionarioService;
    }

    // =====================================================
    // SOLICITAR ACOLHIMENTO
    // =====================================================

    // Somente usuária com sessão válida pode solicitar.
    @PostMapping
    public ResponseEntity<AvisoAcolhimentoResponseDTO> criar(
            @RequestHeader("X-Session-UUID") String uuid,
            @Valid @RequestBody AvisoAcolhimentoRequestDTO dto) {

        AvisoAcolhimentoResponseDTO resposta =
                avisoAcolhimentoService.criar(uuid, dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resposta);
    }

    // =====================================================
    // CONSULTAR AVISO ATIVO DA USUÁRIA
    // =====================================================

    @GetMapping("/me/ativo")
    public ResponseEntity<AvisoAcolhimentoResponseDTO> buscarAvisoAtivo(
            @RequestHeader("X-Session-UUID") String uuid) {

        return avisoAcolhimentoService.buscarAvisoAtivo(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // =====================================================
    // CANCELAR AVISO
    // =====================================================

    // Somente a usuária proprietária pode cancelar.
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<AvisoAcolhimentoResponseDTO> cancelar(
            @RequestHeader("X-Session-UUID") String uuid,
            @PathVariable Long id) {

        AvisoAcolhimentoResponseDTO resposta =
                avisoAcolhimentoService.cancelar(uuid, id);

        return ResponseEntity.ok(resposta);
    }

    // =====================================================
    // VISUALIZAR AVISOS PENDENTES DA EMPRESA
    // =====================================================

    // Funcionário ativo consulta os avisos da própria empresa.
    @GetMapping("/painel/pendentes")
    public ResponseEntity<List<AvisoAcolhimentoResponseDTO>>
    buscarPendentes(Principal principal) {

        Long cdFuncionario = obterFuncionarioId(principal);

        List<AvisoAcolhimentoResponseDTO> avisos =
                avisoAcolhimentoService.buscarPendentesPorEstabelecimento(
                        cdFuncionario
                );

        return ResponseEntity.ok(avisos);
    }

    // =====================================================
    // CONFIRMAR ACOLHIMENTO
    // =====================================================

    // Somente funcionário ativo, treinado e da empresa correta.
    @PatchMapping("/painel/{id}/confirmar")
    public ResponseEntity<AvisoAcolhimentoResponseDTO> confirmar(
            Principal principal,
            @PathVariable Long id) {

        Long cdFuncionario = obterFuncionarioId(principal);

        AvisoAcolhimentoResponseDTO resposta =
                avisoAcolhimentoService.confirmar(
                        cdFuncionario,
                        id
                );

        return ResponseEntity.ok(resposta);
    }

    // =====================================================
    // IDENTIFICAR FUNCIONÁRIO AUTENTICADO
    // =====================================================

    private Long obterFuncionarioId(Principal principal) {

        if (principal == null || principal.getName() == null) {
            throw new IllegalStateException(
                    "Funcionário não autenticado."
            );
        }

        return funcionarioService.buscarIdFuncionarioAutenticado(
                principal.getName()
        );
    }

}
