
package br.com.ari.service;

import br.com.ari.dao.UsuarioDAO;
import br.com.ari.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;

    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    // CREATE
    public int criar(Usuario usuario) {

        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não informado.");
        }

        if (usuario.getNmUsuario() == null ||
                usuario.getNmUsuario().isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }

        if (usuario.getDsEmail() == null ||
                usuario.getDsEmail().isBlank()) {
            throw new IllegalArgumentException("E-mail é obrigatório.");
        }

        return usuarioDAO.criar(usuario);
    }

    // READ
    public Optional<Usuario> buscarPorId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        return usuarioDAO.buscarPorId(id);
    }

    // UPDATE
    public int atualizar(Usuario usuario) {

        if (usuario == null || usuario.getCdUsuario() == null) {
            throw new IllegalArgumentException("Usuário ou ID inválido.");
        }

        if (usuarioDAO.buscarPorId(usuario.getCdUsuario()).isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        return usuarioDAO.atualizar(usuario);
    }

    // DELETE
    public int excluir(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        if (usuarioDAO.buscarPorId(id).isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        return usuarioDAO.excluir(id);
    }
}
