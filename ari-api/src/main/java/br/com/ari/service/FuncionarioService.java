
package br.com.ari.service;

import br.com.ari.dao.FuncionarioDAO;
import br.com.ari.model.Funcionario;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FuncionarioService {

    private final FuncionarioDAO funcionarioDAO;

    public FuncionarioService(FuncionarioDAO funcionarioDAO) {
        this.funcionarioDAO = funcionarioDAO;
    }

    // CREATE
    public int criar(Funcionario funcionario) {

        if (funcionario == null) {
            throw new IllegalArgumentException(
                    "Funcionário não informado."
            );
        }

        if (funcionario.getCdEstabelecimento() == null ||
                funcionario.getCdEstabelecimento() <= 0) {
            throw new IllegalArgumentException(
                    "Estabelecimento inválido."
            );
        }

        if (funcionario.getNmFuncionario() == null ||
                funcionario.getNmFuncionario().isBlank()) {
            throw new IllegalArgumentException(
                    "Nome do funcionário é obrigatório."
            );
        }

        if (funcionario.getDsEmailLogin() == null ||
                funcionario.getDsEmailLogin().isBlank()) {
            throw new IllegalArgumentException(
                    "E-mail de login é obrigatório."
            );
        }

        if (funcionario.getDsSenhaHash() == null ||
                funcionario.getDsSenhaHash().isBlank()) {
            throw new IllegalArgumentException(
                    "Senha é obrigatória."
            );
        }

        return funcionarioDAO.criar(funcionario);
    }

    // READ
    public Optional<Funcionario> buscarPorId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        return funcionarioDAO.buscarPorId(id);
    }

    // UPDATE
    public int atualizar(Funcionario funcionario) {

        if (funcionario == null ||
                funcionario.getCdFuncionario() == null ||
                funcionario.getCdFuncionario() <= 0) {
            throw new IllegalArgumentException(
                    "Funcionário ou ID inválido."
            );
        }

        if (funcionarioDAO.buscarPorId(
                funcionario.getCdFuncionario()
        ).isEmpty()) {
            throw new IllegalArgumentException(
                    "Funcionário não encontrado."
            );
        }

        return funcionarioDAO.atualizar(funcionario);
    }

    // DELETE
    public int excluir(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        if (funcionarioDAO.buscarPorId(id).isEmpty()) {
            throw new IllegalArgumentException(
                    "Funcionário não encontrado."
            );
        }

        return funcionarioDAO.excluir(id);
    }

    // BUSCAR FUNCIONÁRIOS POR ESTABELECIMENTO
    public java.util.List<Funcionario> buscarPorEstabelecimento(Long idEstabelecimento) {

        if (idEstabelecimento == null || idEstabelecimento <= 0) {
            throw new IllegalArgumentException("Estabelecimento inválido.");
        }

        return funcionarioDAO.buscarPorEstabelecimento(idEstabelecimento);
    }
}
