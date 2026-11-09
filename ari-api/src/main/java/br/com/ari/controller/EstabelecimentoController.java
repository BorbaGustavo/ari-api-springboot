package br.com.ari.controller;

import br.com.ari.dto.EstabelecimentoRequestDTO;
import br.com.ari.dto.EstabelecimentoResponseDTO;
import br.com.ari.model.Estabelecimento;
import br.com.ari.service.EstabelecimentoService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/estabelecimentos")
@RequiredArgsConstructor
public class EstabelecimentoController {

    private final EstabelecimentoService estabelecimentoService;

    // CADASTRAR ESTABELECIMENTO
    @PostMapping
    public ResponseEntity<EstabelecimentoResponseDTO> criar(
            @RequestBody EstabelecimentoRequestDTO dto) {

        Estabelecimento estabelecimento = estabelecimentoService.criar(dto);

        EstabelecimentoResponseDTO response = converterParaResponse(estabelecimento);

        return ResponseEntity
                .created(URI.create("/api/estabelecimentos/" + response.cdEstabelecimento()))
                .body(response);
    }

    // LISTAR TODOS OS ESTABELECIMENTOS
    @GetMapping
    public ResponseEntity<List<EstabelecimentoResponseDTO>> buscarTodos() {

        List<EstabelecimentoResponseDTO> estabelecimentos =
                estabelecimentoService.buscarTodos()
                        .stream()
                        .map(this::converterParaResponse)
                        .toList();

        return ResponseEntity.ok(estabelecimentos);
    }

    // BUSCAR ESTABELECIMENTO POR ID
    @GetMapping("/{id}")
    public ResponseEntity<EstabelecimentoResponseDTO> buscarPorId(
            @PathVariable Long id) {

        return estabelecimentoService.buscarPorId(id)
                .map(this::converterParaResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ATUALIZAR ESTABELECIMENTO
    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(
            @PathVariable Long id,
            @RequestBody EstabelecimentoRequestDTO dto) {

        estabelecimentoService.atualizar(id, dto);

        return ResponseEntity.noContent().build();
    }

    // EXCLUIR / DESATIVAR ESTABELECIMENTO
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {

        estabelecimentoService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    // BUSCAR ESTABELECIMENTOS PRÓXIMOS
    @GetMapping("/proximos")
    public ResponseEntity<List<EstabelecimentoResponseDTO>> buscarProximos(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {

        List<EstabelecimentoResponseDTO> estabelecimentos =
                estabelecimentoService.buscarProximos(latitude, longitude)
                        .stream()
                        .map(this::converterParaResponse)
                        .toList();

        return ResponseEntity.ok(estabelecimentos);
    }

    // BUSCAR ESTABELECIMENTOS POR CEP
    @GetMapping("/busca")
    public ResponseEntity<List<EstabelecimentoResponseDTO>> buscarPorCep(
            @RequestParam String cep) {

        List<EstabelecimentoResponseDTO> estabelecimentos =
                estabelecimentoService.buscarPorCep(cep)
                        .stream()
                        .map(this::converterParaResponse)
                        .toList();

        return ResponseEntity.ok(estabelecimentos);
    }

    // CONVERTER MODEL PARA RESPONSE DTO
    private EstabelecimentoResponseDTO converterParaResponse(
            Estabelecimento estabelecimento) {

        return new EstabelecimentoResponseDTO(
                estabelecimento.getCdEstabelecimento(),
                estabelecimento.getCdTipoEstabelecimento(),
                estabelecimento.getNrCnpj(),
                estabelecimento.getNmRazaoSocial(),
                estabelecimento.getNmFantasia(),
                estabelecimento.getNrCep(),
                estabelecimento.getNmLogradouro(),
                estabelecimento.getNrEndereco(),
                estabelecimento.getNmBairro(),
                estabelecimento.getNmCidade(),
                estabelecimento.getSgUf(),
                estabelecimento.getNrLatitude(),
                estabelecimento.getNrLongitude(),
                estabelecimento.getStEstabelecimento(),
                estabelecimento.getDtCadastro()
        );
    }
}
