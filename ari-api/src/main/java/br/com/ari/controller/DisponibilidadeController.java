
package br.com.ari.controller;

import br.com.ari.dto.DisponibilidadeRequestDTO;
import br.com.ari.dto.DisponibilidadeUpdateDTO;
import br.com.ari.dto.DisponibilidadeResponseDTO;
import br.com.ari.dto.DisponibilidadeStatusDTO;

import br.com.ari.exception.NaoAutenticadoException;
import br.com.ari.service.DisponibilidadeService;
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
@RequestMapping("/api/disponibilidades")
@Tag(name = "Disponibilidades", description = "Grade semanal de atendimento do estabelecimento.")
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
    @Operation(summary = "Listar disponibilidades", description = "Lista a grade semanal do estabelecimento do administrador autenticado.")
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
    // CONSULTAR SOMENTE PERÍODOS ATIVOS
    // =====================================================

    @GetMapping("/ativas")
    @Operation(summary = "Listar disponibilidades ativas", description = "Retorna somente os períodos ativos do estabelecimento do administrador.")
    public ResponseEntity<List<DisponibilidadeResponseDTO>> buscarAtivas(
            Principal principal) {

        return ResponseEntity.ok(
                disponibilidadeService.buscarAtivas(obterAdminId(principal))
        );
    }

    // =====================================================
    // CONSULTAR PERÍODOS ATIVOS POR DIA DA SEMANA
    // =====================================================

    @GetMapping("/dia/{dia}")
    @Operation(summary = "Listar disponibilidades por dia", description = "Retorna os períodos ativos de um dia da semana (1 a 7).")
    public ResponseEntity<List<DisponibilidadeResponseDTO>> buscarPorDiaSemana(
            Principal principal,
            @PathVariable Integer dia) {

        return ResponseEntity.ok(
                disponibilidadeService.buscarPorDiaSemana(
                        obterAdminId(principal), dia
                )
        );
    }

    // =====================================================
    // CONSULTAR DISPONIBILIDADE POR ID
    // =====================================================

    @GetMapping("/{id}")
    @Operation(summary = "Buscar disponibilidade", description = "Consulta um período pertencente ao estabelecimento do administrador.")
    public ResponseEntity<DisponibilidadeResponseDTO> buscarPorId(
            Principal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(
                disponibilidadeService.buscarPorId(obterAdminId(principal), id)
        );
    }

    // =====================================================
    // CADASTRAR PERÍODO DE DISPONIBILIDADE
    // =====================================================

    @PostMapping
    @Operation(summary = "Cadastrar disponibilidade", description = "Adiciona um período de atendimento à grade semanal.")
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
    @Operation(summary = "Atualizar disponibilidade", description = "Atualiza dia, período e horários de uma disponibilidade.")
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
    @Operation(summary = "Ativar ou desativar disponibilidade", description = "Use flAtivo=S para ativar ou flAtivo=N para desativar.")
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

    // =====================================================
// IDENTIFICAR ADMIN AUTENTICADO
// =====================================================

    private Long obterAdminId(Principal principal) {

        if (principal == null || principal.getName() == null) {
            throw new NaoAutenticadoException(
                    "Funcionário não autenticado."
            );
        }

        try {
            Long cdFuncionario = Long.valueOf(principal.getName());

            return funcionarioService.buscarIdAdminAutenticado(
                    cdFuncionario
            );

        } catch (NumberFormatException e) {
            throw new NaoAutenticadoException(
                    "Identificação do funcionário autenticado inválida."
            );
        }
    }

}
