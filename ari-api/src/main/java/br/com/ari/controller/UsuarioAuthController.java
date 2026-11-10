
package br.com.ari.controller;

import br.com.ari.dto.LoginRequestDTO;
import br.com.ari.dto.LoginResponseDTO;
import br.com.ari.service.UsuarioService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth/usuarios")
@RequiredArgsConstructor
@Tag(name = "Autenticação de usuárias", description = "Login e criação da sessão de usuárias.")
public class UsuarioAuthController {

    private final UsuarioService usuarioService;

    @PostMapping("/login")
    @Operation(
            summary = "Autenticar usuária",
            description = "Valida e-mail e senha e retorna um UUID de sessão para as operações da usuária.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciais da usuária.",
                    required = true
            )
    )
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {

        LoginResponseDTO resposta = usuarioService.autenticar(dto);

        return ResponseEntity.ok(resposta);
    }
}
