
package br.com.ari.controller;

import br.com.ari.dto.DisponibilidadeRequestDTO;
import br.com.ari.dto.DisponibilidadeUpdateDTO;
import br.com.ari.dto.DisponibilidadeResponseDTO;
import br.com.ari.dto.DisponibilidadeStatusDTO;

import br.com.ari.service.DisponibilidadeService;
import br.com.ari.service.FuncionarioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/disponibilidades")
public class DisponibilidadeController {

    private final DisponibilidadeService disponibilidadeService;
    private final FuncionarioService funcionarioService;

    // CONSTRUTOR
    public DisponibilidadeController(
            DisponibilidadeService disponibilidadeService,
            FuncionarioService funcionarioService) {

        this.disponibilidadeService = disponibilidadeService;
        this.funcionarioService = funcionarioService;
    }

    // =====================================================
    // CONSULTAR GRADE SEMANAL DO ESTABELECIMENTO
    // =====================================================

    @GetMapping
    public ResponseEntity<List<DisponibilidadeResponseDTO>>
    buscarPorEstabelecimento(Principal principal) {

        Long cdFuncionarioAdmin = obterAdminId(principal);

        List<DisponibilidadeResponseDTO> disponibilidades =
                disponibilidadeService.buscarPorEstabelecimento(
                        cdFuncionarioAdmin
                );

        return ResponseEntity.ok(disponibilidades);
    }

    // =====================================================
    // CADASTRAR PERÍODO DE DISPONIBILIDADE
    // =====================================================

    @PostMapping
    public ResponseEntity<DisponibilidadeResponseDTO> criar(
            Principal principal,
            @Valid @RequestBody DisponibilidadeRequestDTO dto) {

        Long cdFuncionarioAdmin = obterAdminId(principal);

        DisponibilidadeResponseDTO resposta =
                disponibilidadeService.criar(
                        cdFuncionarioAdmin,
                        dto
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resposta);
    }

    // =====================================================
    // ATUALIZAR PERÍODO DE DISPONIBILIDADE
    // =====================================================

    @PutMapping("/{id}")
    public ResponseEntity<DisponibilidadeResponseDTO> atualizar(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody DisponibilidadeUpdateDTO dto) {

        Long cdFuncionarioAdmin = obterAdminId(principal);

        DisponibilidadeResponseDTO resposta =
                disponibilidadeService.atualizar(
                        cdFuncionarioAdmin,
                        id,
                        dto
                );

        return ResponseEntity.ok(resposta);
    }

    // =====================================================
    // ATIVAR OU DESATIVAR PERÍODO
    // =====================================================

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> atualizarStatus(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody DisponibilidadeStatusDTO dto) {

        Long cdFuncionarioAdmin = obterAdminId(principal);

        if ("S".equals(dto.flAtivo())) {

            disponibilidadeService.ativar(
                    cdFuncionarioAdmin,
                    id
            );

        } else {

            disponibilidadeService.desativar(
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
