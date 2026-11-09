package br.com.ari.service;

import br.com.ari.dao.EstabelecimentoDAO;
import br.com.ari.dto.EstabelecimentoRequestDTO;
import br.com.ari.model.Estabelecimento;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EstabelecimentoService {

    private final EstabelecimentoDAO estabelecimentoDAO;

    // CADASTRAR ESTABELECIMENTO
    public Estabelecimento criar(EstabelecimentoRequestDTO dto) {

        validarDados(dto);

        Estabelecimento estabelecimento = converterParaModel(dto);

        estabelecimentoDAO.criar(estabelecimento);

        return estabelecimentoDAO.buscarPorCnpj(estabelecimento.getNrCnpj())
                .orElseThrow(() ->
                        new IllegalStateException("Erro ao recuperar estabelecimento cadastrado.")
                );
    }

    // LISTAR TODOS OS ESTABELECIMENTOS
    public List<Estabelecimento> buscarTodos() {
        return estabelecimentoDAO.buscarTodos();
    }

    // BUSCAR ESTABELECIMENTO POR ID
    public Optional<Estabelecimento> buscarPorId(Long id) {

        validarId(id);

        return estabelecimentoDAO.buscarPorId(id);
    }

    // ATUALIZAR ESTABELECIMENTO
    public void atualizar(Long id, EstabelecimentoRequestDTO dto) {

        validarId(id);
        validarDados(dto);

        if (estabelecimentoDAO.buscarPorId(id).isEmpty()) {
            throw new IllegalArgumentException("Estabelecimento não encontrado.");
        }

        Estabelecimento estabelecimento = converterParaModel(dto);
        estabelecimento.setCdEstabelecimento(id);

        estabelecimentoDAO.atualizar(estabelecimento);
    }

    // EXCLUIR / DESATIVAR ESTABELECIMENTO
    public void excluir(Long id) {

        validarId(id);

        if (estabelecimentoDAO.buscarPorId(id).isEmpty()) {
            throw new IllegalArgumentException("Estabelecimento não encontrado.");
        }

        estabelecimentoDAO.excluir(id);
    }

    // BUSCAR ESTABELECIMENTOS PRÓXIMOS
    public List<Estabelecimento> buscarProximos(Double latitude, Double longitude) {

        if (latitude == null || longitude == null
                || latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Coordenadas inválidas.");
        }

        return estabelecimentoDAO.buscarProximos(latitude, longitude);
    }

    // BUSCAR ESTABELECIMENTOS POR CEP
    public List<Estabelecimento> buscarPorCep(String cep) {

        if (cep == null || !cep.matches("\\d{8}")) {
            throw new IllegalArgumentException("CEP inválido. Informe 8 dígitos.");
        }

        return estabelecimentoDAO.buscarPorCep(cep);
    }

    // VALIDAR ID
    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }
    }

    // VALIDAR DADOS
    private void validarDados(EstabelecimentoRequestDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException("Dados do estabelecimento são obrigatórios.");
        }

        if (dto.nrCnpj() == null || !dto.nrCnpj().matches("\\d{14}")) {
            throw new IllegalArgumentException("CNPJ deve conter 14 dígitos.");
        }

        if (dto.nmRazaoSocial() == null || dto.nmRazaoSocial().isBlank()) {
            throw new IllegalArgumentException("Razão social é obrigatória.");
        }

        if (dto.nmFantasia() == null || dto.nmFantasia().isBlank()) {
            throw new IllegalArgumentException("Nome fantasia é obrigatório.");
        }

        if (dto.nrCep() == null || !dto.nrCep().matches("\\d{8}")) {
            throw new IllegalArgumentException("CEP deve conter 8 dígitos.");
        }

        if (dto.cdTipoEstabelecimento() == null || dto.cdTipoEstabelecimento() <= 0) {
            throw new IllegalArgumentException("Tipo de estabelecimento inválido.");
        }
    }

    // CONVERTER DTO PARA MODEL
    private Estabelecimento converterParaModel(EstabelecimentoRequestDTO dto) {

        return Estabelecimento.builder()
                .cdTipoEstabelecimento(dto.cdTipoEstabelecimento())
                .nrCnpj(dto.nrCnpj())
                .nmRazaoSocial(dto.nmRazaoSocial())
                .nmFantasia(dto.nmFantasia())
                .nrCep(dto.nrCep())
                .nmLogradouro(dto.nmLogradouro())
                .nrEndereco(dto.nrEndereco())
                .nmBairro(dto.nmBairro())
                .nmCidade(dto.nmCidade())
                .sgUf(dto.sgUf())
                .nrLatitude(dto.nrLatitude())
                .nrLongitude(dto.nrLongitude())
                .stEstabelecimento(dto.stEstabelecimento())
                .build();
    }
}