
package br.com.ari.controller;

import br.com.ari.dto.FuncionarioLoginDTO;
import br.com.ari.dto.FuncionarioResponseDTO;

import br.com.ari.service.FuncionarioService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    // CONSTRUTOR
    public FuncionarioController(
            FuncionarioService funcionarioService) {

        this.funcionarioService = funcionarioService;
    }

    // =====================================================
    // LOGIN DO FUNCIONÁRIO
    // =====================================================

    @PostMapping("/login")
    public ResponseEntity<FuncionarioResponseDTO> autenticar(
            @Valid @RequestBody FuncionarioLoginDTO dto) {

        FuncionarioResponseDTO resposta =
                funcionarioService.autenticar(dto);

        return ResponseEntity.ok(resposta);
    }

}
