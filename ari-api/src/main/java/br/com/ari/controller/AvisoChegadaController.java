
package br.com.ari.controller;

import br.com.ari.dto.AvisoChegadaRequestDTO;
import br.com.ari.dto.AvisoChegadaResponseDTO;
import br.com.ari.model.AvisoChegada;
import br.com.ari.service.AvisoChegadaService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/avisos")
@RequiredArgsConstructor
public class AvisoChegadaController {

    private final AvisoChegadaService avisoChegadaService;

    // CADASTRAR AVISO
    @PostMapping
    public ResponseEntity<AvisoChegadaResponseDTO> criar(
            @RequestBody AvisoChegadaRequestDTO dto) {

        AvisoChegada aviso = avisoChegadaService.criar(dto);

        AvisoChegadaResponseDTO response = converterParaResponse(aviso);

        return ResponseEntity
                .created(URI.create("/api/avisos/" + response.cdAvisoChegada()))
                .body(response);
    }

    // LISTAR TODOS OS AVISOS
    @GetMapping
    public ResponseEntity<List<AvisoChegadaResponseDTO>> buscarTodos() {

        List<AvisoChegadaResponseDTO> avisos =
                avisoChegadaService.buscarTodos()
                        .stream()
                        .map(this::converterParaResponse)
                        .toList();

        return ResponseEntity.ok(avisos);
    }

    // BUSCAR AVISO POR ID
    @GetMapping("/{id}")
    public ResponseEntity<AvisoChegadaResponseDTO> buscarPorId(
            @PathVariable Long id) {

        return avisoChegadaService.buscarPorId(id)
                .map(this::converterParaResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ATUALIZAR STATUS DO AVISO
    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(
            @PathVariable Long id,
            @RequestParam String status) {

        avisoChegadaService.atualizar(id, status);

        return ResponseEntity.noContent().build();
    }

    // CANCELAR AVISO
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {

        avisoChegadaService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    // CONVERTER MODEL PARA RESPONSE DTO
    private AvisoChegadaResponseDTO converterParaResponse(
            AvisoChegada aviso) {

        return new AvisoChegadaResponseDTO(
                aviso.getCdAvisoChegada(),
                aviso.getCdEstabelecimento(),
                aviso.getCdFuncionario(),
                aviso.getDtAviso(),
                aviso.getStAviso(),
                aviso.getNrDistanciaMetros(),
                aviso.getNrTempoEstimadoMin(),
                aviso.getDtAcolhimento()
        );
    }
}

