
package br.com.ari.controller;

import br.com.ari.dto.FuncionarioResponseDTO;
import br.com.ari.model.Funcionario;
import br.com.ari.service.FuncionarioService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estabelecimentos/{idEstabelecimento}/funcionarios")
@RequiredArgsConstructor
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    // CADASTRAR FUNCIONÁRIO NO ESTABELECIMENTO
    @PostMapping
    public ResponseEntity<Void> criar(
            @PathVariable Long idEstabelecimento,
            @RequestBody Funcionario funcionario) {

        // O estabelecimento vem da URL, não do corpo da requisição.
        funcionario.setCdEstabelecimento(idEstabelecimento);

        funcionarioService.criar(funcionario);

        return ResponseEntity.status(201).build();
    }

    // LISTAR FUNCIONÁRIOS DO ESTABELECIMENTO
    @GetMapping
    public ResponseEntity<List<FuncionarioResponseDTO>> buscarPorEstabelecimento(
            @PathVariable Long idEstabelecimento) {

        List<FuncionarioResponseDTO> funcionarios =
                funcionarioService.buscarPorEstabelecimento(idEstabelecimento)
                        .stream()
                        .map(this::converterParaResponse)
                        .toList();

        return ResponseEntity.ok(funcionarios);
    }

    // CONVERTER MODEL PARA RESPONSE DTO
    private FuncionarioResponseDTO converterParaResponse(Funcionario funcionario) {

        return new FuncionarioResponseDTO(
                funcionario.getCdFuncionario(),
                funcionario.getCdEstabelecimento(),
                funcionario.getNrTelefone(),
                funcionario.getNmFuncionario(),
                funcionario.getDsEmailLogin(),
                funcionario.getTpPerfil(),
                funcionario.getFlTreinado(),
                funcionario.getStFuncionario(),
                funcionario.getDtCadastro()
        );
    }
}
