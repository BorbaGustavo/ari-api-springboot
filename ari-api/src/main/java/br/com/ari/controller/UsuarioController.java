
package br.com.ari.controller;

import br.com.ari.dto.UsuarioResponseDTO;
import br.com.ari.model.Usuario;
import br.com.ari.service.UsuarioService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }


    // CREATE
    @PostMapping
    public ResponseEntity<Void> criar(@RequestBody Usuario usuario) {

        usuarioService.criar(usuario);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // READ
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(
            @PathVariable Long id) {

        return usuarioService.buscarPorId(id)
                .map(usuario -> ResponseEntity.ok(
                        new UsuarioResponseDTO(
                                usuario.getCdUsuario(),
                                usuario.getNmUsuario(),
                                usuario.getNrTelefone(),
                                usuario.getDsEmail(),
                                usuario.getStUsuario(),
                                usuario.getDtCadastro()
                        )
                ))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> buscarTodos() {

        List<UsuarioResponseDTO> usuarios = usuarioService.buscarTodos()
                .stream()
                .map(usuario -> new UsuarioResponseDTO(
                        usuario.getCdUsuario(),
                        usuario.getNmUsuario(),
                        usuario.getNrTelefone(),
                        usuario.getDsEmail(),
                        usuario.getStUsuario(),
                        usuario.getDtCadastro()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(usuarios);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(
            @PathVariable Long id,
            @RequestBody Usuario usuario) {

        usuario.setCdUsuario(id);

        usuarioService.atualizar(usuario);

        return ResponseEntity.noContent().build();
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {

        usuarioService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}
