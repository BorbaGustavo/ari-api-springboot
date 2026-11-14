
package br.com.ari.controller;

import br.com.ari.dto.*;

import br.com.ari.exception.NaoAutenticadoException;
import br.com.ari.model.Estabelecimento;

import br.com.ari.service.EstabelecimentoService;
import br.com.ari.service.FuncionarioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/estabelecimentos")
@Tag(
        name = "Estabelecimentos",
        description = "Cadastro, localização e configuração do acolhimento."
)
public class EstabelecimentoController {

    private final EstabelecimentoService estabelecimentoService;
    private final FuncionarioService funcionarioService;

    // CONSTRUTOR
    public EstabelecimentoController(
            EstabelecimentoService estabelecimentoService,
            FuncionarioService funcionarioService) {

        this.estabelecimentoService = estabelecimentoService;
        this.funcionarioService = funcionarioService;
    }

    // =====================================================
    // CADASTRAR ESTABELECIMENTO
    // PÚBLICO
    // =====================================================

    @PostMapping
    @Operation(
            summary = "Cadastrar estabelecimento",
            description = "Cria um estabelecimento e seu primeiro administrador."
    )
    public ResponseEntity<EstabelecimentoResponseDTO> criar(
            @Valid @RequestBody EstabelecimentoRequestDTO dto) {

        Estabelecimento estabelecimento =
                estabelecimentoService.criar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(converterParaResponse(estabelecimento));
    }

    // =====================================================
    // ATUALIZAR ESTABELECIMENTO
    // ADMIN
    // =====================================================

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar estabelecimento",
            description = "Atualiza os dados operacionais e as coordenadas."
    )
    public ResponseEntity<Void> atualizar(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody EstabelecimentoUpdateDTO dto) {

        validarAdmin(principal, id);

        estabelecimentoService.atualizar(id, dto);

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // EXCLUIR ESTABELECIMENTO
    // ADMIN
    // =====================================================

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir estabelecimento",
            description = "Exclui fisicamente o estabelecimento, caso não existam registros vinculados."
    )
    public ResponseEntity<Void> excluir(
            Principal principal,
            @PathVariable Long id) {

        validarAdmin(principal, id);

        estabelecimentoService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // BUSCAR ESTABELECIMENTOS PRÓXIMOS
    // PÚBLICO
    // =====================================================

    @GetMapping("/proximos")
    @Operation(
            summary = "Buscar estabelecimentos próximos",
            description = "Retorna estabelecimentos próximos às coordenadas informadas."
    )
    public ResponseEntity<List<EstabelecimentoResponseDTO>> buscarProximos(

            @Parameter(
                    description = "Latitude entre -90 e 90.",
                    example = "-23.5505"
            )
            @RequestParam Double latitude,

            @Parameter(
                    description = "Longitude entre -180 e 180.",
                    example = "-46.6333"
            )
            @RequestParam Double longitude) {

        List<EstabelecimentoResponseDTO> estabelecimentos =
                estabelecimentoService.buscarProximos(
                                latitude,
                                longitude
                        )
                        .stream()
                        .map(this::converterParaResponse)
                        .toList();

        return ResponseEntity.ok(estabelecimentos);
    }

    // =====================================================
    // BUSCAR ESTABELECIMENTOS POR CEP
    // PÚBLICO
    // =====================================================

    @GetMapping("/cep/{cep}")
    @Operation(summary = "Buscar estabelecimentos por CEP", description = "Retorna estabelecimentos ativos, com acolhimento habilitado e sem suspensão vigente no CEP informado.")
    public ResponseEntity<List<EstabelecimentoResponseDTO>> buscarPorCep(
            @PathVariable String cep) {

        List<EstabelecimentoResponseDTO> estabelecimentos =
                estabelecimentoService.buscarPorCep(cep).stream()
                        .map(this::converterParaResponse)
                        .toList();

        return ResponseEntity.ok(estabelecimentos);
    }

    // =====================================================
    // BUSCAR ESTABELECIMENTO POR ID
    // PÚBLICO
    // =====================================================

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar estabelecimento",
            description = "Consulta um estabelecimento ativo pelo identificador."
    )
    public ResponseEntity<EstabelecimentoResponseDTO> buscarPorId(
            @PathVariable Long id) {

        return estabelecimentoService.buscarPorIdAtivo(id)
                .map(this::converterParaResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================================
    // HABILITAR OU DESABILITAR ACOLHIMENTO
    // ADMIN
    // =====================================================

    @PatchMapping("/{id}/acolhimento")
    @Operation(
            summary = "Ativar ou desativar acolhimento",
            description = "Altera se o estabelecimento recebe novas solicitações de acolhimento."
    )
    public ResponseEntity<Void> atualizarAcolhimento(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody EstabelecimentoAcolhimentoUpdateDTO dto) {

        validarAdmin(principal, id);

        estabelecimentoService.atualizarAcolhimento(id, dto);

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // CONFIGURAR OU REMOVER SUSPENSÃO TEMPORÁRIA
    // ADMIN
    // =====================================================

    @PatchMapping("/{id}/suspensao")
    @Operation(
            summary = "Configurar suspensão",
            description = "Informa uma data futura para suspender ou null para remover a suspensão."
    )
    public ResponseEntity<Void> atualizarSuspensao(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody EstabelecimentoSuspensaoDTO dto) {

        validarAdmin(principal, id);

        estabelecimentoService.atualizarSuspensao(id, dto);

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // ATUALIZAR STATUS
    // ADMIN
    // =====================================================

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Atualizar status",
            description = "Altera o status do estabelecimento."
    )
    public ResponseEntity<Void> atualizarStatus(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody EstabelecimentoStatusUpdateDTO dto) {

        validarAdmin(principal, id);

        estabelecimentoService.atualizarStatus(id, dto);

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // VALIDAR ADMINISTRADOR DO ESTABELECIMENTO
    // =====================================================

    private void validarAdmin(
            Principal principal,
            Long cdEstabelecimento) {

        if (principal == null || principal.getName() == null) {
            throw new NaoAutenticadoException(
                    "Funcionário não autenticado."
            );
        }

        Long cdFuncionarioAdmin;

        try {
            // Principal agora contém o ID do funcionário.
            cdFuncionarioAdmin = Long.valueOf(principal.getName());

        } catch (NumberFormatException e) {
            throw new NaoAutenticadoException(
                    "Identificação do funcionário autenticado inválida."
            );
        }

        // Verifica se é ADMIN e pertence ao estabelecimento informado.
        funcionarioService.validarAcessoAdminEstabelecimento(
                cdFuncionarioAdmin,
                cdEstabelecimento
        );
    }

    // =====================================================
    // CONVERTER MODEL PARA RESPONSE DTO
    // =====================================================

    private EstabelecimentoResponseDTO converterParaResponse(
            Estabelecimento estabelecimento) {

        return new EstabelecimentoResponseDTO(
                estabelecimento.getCdEstabelecimento(),
                estabelecimento.getNrCnpj(),
                estabelecimento.getNmRazaoSocial(),
                estabelecimento.getNmFantasia(),
                estabelecimento.getNmEmpresa(),
                estabelecimento.getDsEmail(),
                estabelecimento.getNrTelefone(),
                estabelecimento.getDsTipoEstabelecimento(),
                estabelecimento.getNrCep(),
                estabelecimento.getDsLogradouro(),
                estabelecimento.getNrEndereco(),
                estabelecimento.getDsComplemento(),
                estabelecimento.getNmBairro(),
                estabelecimento.getNmCidade(),
                estabelecimento.getSgUf(),
                estabelecimento.getNrLatitude(),
                estabelecimento.getNrLongitude(),
                estabelecimento.getFlAcolhimentoAtivo(),
                estabelecimento.getDtSuspensaoAte(),
                estabelecimento.getStEstabelecimento(),
                estabelecimento.getDtCadastro()
        );
    }
}
