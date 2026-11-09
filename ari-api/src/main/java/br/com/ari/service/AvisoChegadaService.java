package br.com.ari.service;

import br.com.ari.dao.AvisoChegadaDAO;
import br.com.ari.dto.AvisoChegadaRequestDTO;
import br.com.ari.model.AvisoChegada;
import br.com.ari.model.SessaoWeb;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AvisoChegadaService {

    private final AvisoChegadaDAO avisoChegadaDAO;
    private final SessaoWebService sessaoWebService;

    // CADASTRAR AVISO
    public AvisoChegada criar(AvisoChegadaRequestDTO dto) {

        validarDados(dto);

        // Verifica se a sessão existe, está ativa e não expirou
        SessaoWeb sessao = sessaoWebService.buscarSessaoAtiva(dto.dsUuid())
                .orElseThrow(() ->
                        new IllegalArgumentException("Sessão inválida ou expirada.")
                );

        AvisoChegada aviso = AvisoChegada.builder()
                .cdSessaoWeb(sessao.getCdSessaoWeb())
                .cdEstabelecimento(dto.cdEstabelecimento())
                .nrDistanciaMetros(dto.nrDistanciaMetros())
                .nrTempoEstimadoMin(dto.nrTempoEstimadoMin())
                .build();

        avisoChegadaDAO.criar(aviso);

        if (aviso.getCdAvisoChegada() == null) {
            throw new IllegalStateException("Erro ao cadastrar aviso.");
        }

        return avisoChegadaDAO.buscarPorId(aviso.getCdAvisoChegada())
                .orElseThrow(() ->
                        new IllegalStateException("Erro ao recuperar aviso cadastrado.")
                );
    }

    // LISTAR TODOS OS AVISOS
    public List<AvisoChegada> buscarTodos() {
        return avisoChegadaDAO.buscarTodos();
    }

    // BUSCAR AVISO POR ID
    public Optional<AvisoChegada> buscarPorId(Long id) {

        validarId(id);

        return avisoChegadaDAO.buscarPorId(id);
    }

    // ATUALIZAR STATUS DO AVISO
    public void atualizar(Long id, String status) {

        validarId(id);
        validarStatus(status);

        AvisoChegada aviso = avisoChegadaDAO.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Aviso não encontrado.")
                );

        if (!"A_CAMINHO".equals(aviso.getStAviso())) {
            throw new IllegalArgumentException(
                    "Não é possível alterar um aviso que já foi finalizado."
            );
        }

        avisoChegadaDAO.atualizarStatus(id, status);

        // Confirma se a atualização realmente aconteceu
        AvisoChegada atualizado = avisoChegadaDAO.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalStateException("Erro ao recuperar aviso.")
                );

        if (!status.equals(atualizado.getStAviso())) {
            throw new IllegalStateException("Não foi possível atualizar o aviso.");
        }
    }

    // CANCELAR AVISO
    public void excluir(Long id) {

        validarId(id);

        AvisoChegada aviso = avisoChegadaDAO.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Aviso não encontrado.")
                );

        if (!"A_CAMINHO".equals(aviso.getStAviso())) {
            throw new IllegalArgumentException(
                    "Não é possível cancelar um aviso já finalizado."
            );
        }

        avisoChegadaDAO.cancelar(id);
    }

    // VALIDAR DADOS DO AVISO
    private void validarDados(AvisoChegadaRequestDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException("Dados do aviso são obrigatórios.");
        }

        if (dto.dsUuid() == null || dto.dsUuid().isBlank()) {
            throw new IllegalArgumentException("Identificação da sessão obrigatória.");
        }

        if (dto.cdEstabelecimento() == null || dto.cdEstabelecimento() <= 0) {
            throw new IllegalArgumentException("Estabelecimento inválido.");
        }

        if (dto.nrDistanciaMetros() != null
                && dto.nrDistanciaMetros().signum() < 0) {
            throw new IllegalArgumentException("Distância não pode ser negativa.");
        }

        if (dto.nrTempoEstimadoMin() != null
                && dto.nrTempoEstimadoMin() < 0) {
            throw new IllegalArgumentException("Tempo estimado não pode ser negativo.");
        }
    }

    // VALIDAR ID
    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }
    }

    // VALIDAR STATUS
    private void validarStatus(String status) {

        if (status == null || !List.of(
                "ACOLHIDA",
                "CANCELADO",
                "EXPIRADO"
        ).contains(status)) {
            throw new IllegalArgumentException("Status do aviso inválido.");
        }
    }
}