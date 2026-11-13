
package br.com.ari.controller;

import br.com.ari.dto.AvisoAcolhimentoRequestDTO;
import br.com.ari.dto.AvisoAcolhimentoResponseDTO;
import br.com.ari.exception.NaoAutenticadoException;
import br.com.ari.service.AvisoAcolhimentoService;
import br.com.ari.service.FuncionarioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/avisos-acolhimento")
@Tag(
        name = "Avisos de acolhimento",
        description = "Solicitação, acompanhamento e confirmação do acolhimento."
)
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

    @PostMapping
    @Operation(
            summary = "Solicitar acolhimento",
            description = "Cria uma solicitação utilizando uma sessão válida de uma usuária autenticada."
    )
    public ResponseEntity<AvisoAcolhimentoResponseDTO> criar(

            @Parameter(
                    description = "UUID da sessão da usuária.",
                    example = "550e8400-e29b-41d4-a716-446655440000",
                    in = ParameterIn.HEADER
            )
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
    @Operation(
            summary = "Consultar aviso ativo",
            description = "Retorna o aviso ativo da usuária ou 204 quando não houver aviso."
    )
    public ResponseEntity<AvisoAcolhimentoResponseDTO> buscarAvisoAtivo(

            @Parameter(
                    description = "UUID da sessão da usuária.",
                    example = "550e8400-e29b-41d4-a716-446655440000",
                    in = ParameterIn.HEADER
            )
            @RequestHeader("X-Session-UUID") String uuid) {

        return avisoAcolhimentoService.buscarAvisoAtivo(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // =====================================================
    // CANCELAR AVISO
    // =====================================================

    @PatchMapping("/{id}/cancelar")
    @Operation(
            summary = "Cancelar aviso",
            description = "Cancela um aviso pertencente à usuária da sessão."
    )
    public ResponseEntity<AvisoAcolhimentoResponseDTO> cancelar(

            @Parameter(
                    description = "UUID da sessão da usuária.",
                    in = ParameterIn.HEADER
            )
            @RequestHeader("X-Session-UUID") String uuid,

            @Parameter(
                    description = "Identificador do aviso.",
                    example = "1"
            )
            @PathVariable("id") Long id) {

        AvisoAcolhimentoResponseDTO resposta =
                avisoAcolhimentoService.cancelar(uuid, id);

        return ResponseEntity.ok(resposta);
    }

    // =====================================================
    // VISUALIZAR AVISOS PENDENTES DA EMPRESA
    // =====================================================

    @GetMapping("/painel/pendentes")
    @Operation(
            summary = "Listar avisos pendentes",
            description = "Lista os avisos A_CAMINHO do estabelecimento do funcionário autenticado."
    )
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

    @PatchMapping("/painel/{id}/confirmar")
    @Operation(
            summary = "Confirmar acolhimento",
            description = "Confirma o acolhimento por funcionário ativo, treinado e vinculado ao estabelecimento do aviso."
    )
    public ResponseEntity<AvisoAcolhimentoResponseDTO> confirmar(

            Principal principal,

            @Parameter(
                    description = "Identificador do aviso.",
                    example = "1"
            )
            @PathVariable("id") Long id) {

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
            throw new NaoAutenticadoException(
                    "Funcionário não autenticado."
            );
        }

        return funcionarioService.buscarIdFuncionarioAutenticado(
                principal.getName()
        );
    }
}
