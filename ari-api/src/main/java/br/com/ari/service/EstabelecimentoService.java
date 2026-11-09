
package br.com.ari.service;

import br.com.ari.dao.EstabelecimentoDAO;
import br.com.ari.model.Estabelecimento;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EstabelecimentoService {

    private final EstabelecimentoDAO estabelecimentoDAO;

    public EstabelecimentoService(EstabelecimentoDAO estabelecimentoDAO) {
        this.estabelecimentoDAO = estabelecimentoDAO;
    }

    // CREATE
    public int criar(Estabelecimento estabelecimento) {

        if (estabelecimento == null) {
            throw new IllegalArgumentException(
                    "Estabelecimento não informado."
            );
        }

        if (estabelecimento.getCdTipoEstabelecimento() == null ||
                estabelecimento.getCdTipoEstabelecimento() <= 0) {
            throw new IllegalArgumentException(
                    "Tipo de estabelecimento inválido."
            );
        }

        if (estabelecimento.getNrCnpj() == null ||
                estabelecimento.getNrCnpj().isBlank()) {
            throw new IllegalArgumentException(
                    "CNPJ é obrigatório."
            );
        }

        if (estabelecimento.getNmRazaoSocial() == null ||
                estabelecimento.getNmRazaoSocial().isBlank()) {
            throw new IllegalArgumentException(
                    "Razão social é obrigatória."
            );
        }

        return estabelecimentoDAO.criar(estabelecimento);
    }

    // READ
    public Optional<Estabelecimento> buscarPorId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        return estabelecimentoDAO.buscarPorId(id);
    }

    // UPDATE
    public int atualizar(Estabelecimento estabelecimento) {

        if (estabelecimento == null ||
                estabelecimento.getCdEstabelecimento() == null ||
                estabelecimento.getCdEstabelecimento() <= 0) {
            throw new IllegalArgumentException(
                    "Estabelecimento ou ID inválido."
            );
        }

        if (estabelecimentoDAO.buscarPorId(
                estabelecimento.getCdEstabelecimento()
        ).isEmpty()) {
            throw new IllegalArgumentException(
                    "Estabelecimento não encontrado."
            );
        }

        return estabelecimentoDAO.atualizar(estabelecimento);
    }

    // DELETE
    public int excluir(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        if (estabelecimentoDAO.buscarPorId(id).isEmpty()) {
            throw new IllegalArgumentException(
                    "Estabelecimento não encontrado."
            );
        }

        return estabelecimentoDAO.excluir(id);
    }
}
