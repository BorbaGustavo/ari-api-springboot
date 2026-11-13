
package br.com.ari.controller;

import br.com.ari.dto.EstabelecimentoResponseDTO;
import br.com.ari.dto.EstabelecimentoRequestDTO;
import br.com.ari.dto.EstabelecimentoUpdateDTO;
import br.com.ari.dto.EstabelecimentoAcolhimentoUpdateDTO;
import br.com.ari.dto.EstabelecimentoSuspensaoDTO;

import br.com.ari.exception.NaoAutenticadoException;
import br.com.ari.model.Estabelecimento;

import br.com.ari.service.EstabelecimentoService;
import br.com.ari.service.FuncionarioService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/estabelecimentos")
@Tag(name = "Estabelecimentos", description = "Cadastro, localização e configuração do acolhimento.")
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

    @PostMapping
    @Operation(summary = "Cadastrar estabelecimento", description = "Cria um estabelecimento e retorna seus dados.")
    public ResponseEntity<EstabelecimentoResponseDTO> criar(
            @Valid @RequestBody EstabelecimentoRequestDTO dto) {

        return ResponseEntity.status(201)
                .body(converterParaResponse(estabelecimentoService.criar(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar estabelecimento", description = "Atualiza os dados cadastrais do estabelecimento.")
    public ResponseEntity<Void> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody EstabelecimentoUpdateDTO dto) {

        estabelecimentoService.atualizar(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir estabelecimento", description = "Exclui o estabelecimento identificado.")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        estabelecimentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // BUSCAR ESTABELECIMENTOS PRÓXIMOS
    // =====================================================

    @GetMapping("/proximos")
    @Operation(summary = "Buscar estabelecimentos próximos", description = "Retorna estabelecimentos próximos às coordenadas informadas.")
    public ResponseEntity<List<EstabelecimentoResponseDTO>> buscarProximos(
            @Parameter(description = "Latitude entre -90 e 90.", example = "-23.5505")
            @RequestParam Double latitude,
            @Parameter(description = "Longitude entre -180 e 180.", example = "-46.6333")
            @RequestParam Double longitude) {

        List<EstabelecimentoResponseDTO> estabelecimentos =
                estabelecimentoService.buscarProximos(
                                latitude,
                                longitude
                        ).stream()
                        .map(this::converterParaResponse)
                        .toList();

        return ResponseEntity.ok(estabelecimentos);
    }

    // =====================================================
    // BUSCAR ESTABELECIMENTO POR ID
    // =====================================================

    @GetMapping("/{id}")
    @Operation(summary = "Buscar estabelecimento", description = "Consulta um estabelecimento ativo pelo identificador.")
    public ResponseEntity<EstabelecimentoResponseDTO> buscarPorId(
            @PathVariable Long id) {

        return estabelecimentoService.buscarPorId(id)
                .filter(estabelecimento ->
                        "ATIVO".equals(
                                estabelecimento.getStEstabelecimento()
                        )
                )
                .map(this::converterParaResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================================
    // HABILITAR OU DESABILITAR ACOLHIMENTO
    // =====================================================

    // Somente ADMIN do estabelecimento pode alterar.
    @PatchMapping("/{id}/acolhimento")
    @Operation(summary = "Ativar ou desativar acolhimento", description = "Altera se o estabelecimento recebe novas solicitações de acolhimento.")
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
    // =====================================================

    // Somente ADMIN do estabelecimento pode alterar.
    // Uma data futura configura a suspensão.
    // Uma data nula remove a suspensão.
    @PatchMapping("/{id}/suspensao")
    @Operation(summary = "Configurar suspensão", description = "Configura uma data futura de suspensão ou remova a suspensão enviando dtSuspensaoAte como null.")
    public ResponseEntity<Void> atualizarSuspensao(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody EstabelecimentoSuspensaoDTO dto) {

        validarAdmin(principal, id);

        if (dto.dtSuspensaoAte() == null) {
            estabelecimentoService.removerSuspensao(id);
        } else {
            estabelecimentoService.atualizarSuspensao(id, dto);
        }

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

        funcionarioService.validarAcessoAdminEstabelecimento(
                principal.getName(),
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
