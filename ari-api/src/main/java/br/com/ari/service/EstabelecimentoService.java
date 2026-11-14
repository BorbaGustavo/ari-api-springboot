
package br.com.ari.service;

import br.com.ari.dao.EstabelecimentoDAO;
import br.com.ari.dto.*;
import br.com.ari.integration.BrasilApiCepResponse;
import br.com.ari.integration.BrasilApiCnpjResponse;
import br.com.ari.integration.BrasilApiService;
import br.com.ari.model.Estabelecimento;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class EstabelecimentoService {

    private static final List<String> STATUS_PERMITIDOS = List.of(
            "PENDENTE",
            "ATIVO",
            "SUSPENSO",
            "REPROVADO"
    );

    private final EstabelecimentoDAO estabelecimentoDAO;
    private final BrasilApiService brasilApiService;
    private final FuncionarioService funcionarioService;

    public EstabelecimentoService(
            EstabelecimentoDAO estabelecimentoDAO,
            BrasilApiService brasilApiService,
            FuncionarioService funcionarioService) {

        this.estabelecimentoDAO = estabelecimentoDAO;
        this.brasilApiService = brasilApiService;
        this.funcionarioService = funcionarioService;
    }

    // =====================================================
    // CADASTRAR ESTABELECIMENTO E ADMINISTRADOR INICIAL
    // =====================================================

    @Transactional
    public Estabelecimento criar(EstabelecimentoRequestDTO dto) {

        validarDadosCadastro(dto);

        String cnpj = somenteDigitos(dto.nrCnpj());

        BrasilApiCnpjResponse dadosCnpj =
                brasilApiService.consultarCnpj(cnpj);

        // Na criação, o CNPJ precisa estar ativo.
        validarDadosCnpj(dadosCnpj, cnpj, true);

        BrasilApiCepResponse dadosCep =
                brasilApiService.consultarCep(
                        somenteDigitos(dadosCnpj.cep())
                );

        validarDadosCep(dadosCnpj, dadosCep);

        if (estabelecimentoDAO.buscarPorCnpj(cnpj).isPresent()) {
            throw new IllegalArgumentException(
                    "Este CNPJ já está cadastrado."
            );
        }

        Estabelecimento estabelecimento =
                converterParaModel(dto, dadosCnpj, dadosCep);

        try {
            int linhasAfetadas =
                    estabelecimentoDAO.criar(estabelecimento);

            if (linhasAfetadas != 1) {
                throw new IllegalStateException(
                        "Não foi possível cadastrar o estabelecimento."
                );
            }

        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "Não foi possível cadastrar o estabelecimento. "
                            + "Verifique se o CNPJ já está cadastrado "
                            + "ou se algum dado viola as regras do banco.",
                    e
            );
        }

        Estabelecimento criado =
                estabelecimentoDAO.buscarPorCnpj(cnpj)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Erro ao recuperar o estabelecimento cadastrado."
                                )
                        );

        // O estabelecimento e seu primeiro ADMIN são cadastrados
        // na mesma transação.
        funcionarioService.criarAdministradorInicial(
                criado.getCdEstabelecimento(),
                dto.responsavel()
        );

        return criado;
    }

    // =====================================================
    // CONSULTAS
    // =====================================================

    public List<Estabelecimento> buscarTodos() {
        return estabelecimentoDAO.buscarTodos();
    }

    public Optional<Estabelecimento> buscarPorId(Long id) {

        validarId(id);

        return estabelecimentoDAO.buscarPorId(id);
    }

    public Optional<Estabelecimento> buscarPorIdAtivo(Long id) {

        validarId(id);

        return estabelecimentoDAO.buscarPorId(id)
                .filter(estabelecimento ->
                        "ATIVO".equals(
                                estabelecimento.getStEstabelecimento()
                        )
                );
    }

    // =====================================================
    // ATUALIZAR DADOS CADASTRAIS
    // =====================================================

    @Transactional
    public int atualizar(
            Long id,
            EstabelecimentoUpdateDTO dto) {

        validarId(id);
        validarDadosAtualizacao(dto);

        Estabelecimento existente =
                buscarEstabelecimentoExistente(id);

        String cnpj = somenteDigitos(existente.getNrCnpj());

        BrasilApiCnpjResponse dadosCnpj =
                brasilApiService.consultarCnpj(cnpj);

        // Na atualização, valida os dados oficiais e a correspondência
        // do CNPJ, mas não bloqueia a edição operacional caso a situação
        // cadastral tenha mudado depois do cadastro.
        validarDadosCnpj(dadosCnpj, cnpj, false);

        BrasilApiCepResponse dadosCep =
                brasilApiService.consultarCep(
                        somenteDigitos(dadosCnpj.cep())
                );

        validarDadosCep(dadosCnpj, dadosCep);

        Estabelecimento atualizado =
                converterParaModel(
                        dto,
                        existente,
                        dadosCnpj,
                        dadosCep
                );

        return verificarAtualizacao(
                estabelecimentoDAO.atualizar(atualizado)
        );
    }

    // =====================================================
    // EXCLUIR ESTABELECIMENTO FISICAMENTE
    // =====================================================

    @Transactional
    public int excluir(Long id) {

        validarId(id);
        buscarEstabelecimentoExistente(id);

        try {
            int linhasAfetadas =
                    estabelecimentoDAO.excluirFisico(id);

            if (linhasAfetadas == 0) {
                throw new IllegalArgumentException(
                        "Estabelecimento não encontrado."
                );
            }

            return linhasAfetadas;

        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "Não é possível excluir o estabelecimento "
                            + "porque existem registros vinculados.",
                    e
            );
        }
    }

    // =====================================================
    // HABILITAR OU DESABILITAR ACOLHIMENTO
    // =====================================================

    @Transactional
    public int atualizarAcolhimento(
            Long id,
            EstabelecimentoAcolhimentoUpdateDTO dto) {

        validarId(id);

        if (dto == null
                || dto.flAcolhimentoAtivo() == null
                || (!"S".equals(dto.flAcolhimentoAtivo())
                && !"N".equals(dto.flAcolhimentoAtivo()))) {

            throw new IllegalArgumentException(
                    "Informe S ou N para o acolhimento."
            );
        }

        Estabelecimento estabelecimento =
                buscarEstabelecimentoExistente(id);

        if ("S".equals(dto.flAcolhimentoAtivo())) {

            if (!"ATIVO".equals(
                    estabelecimento.getStEstabelecimento())) {

                throw new IllegalStateException(
                        "Somente estabelecimentos ativos podem habilitar acolhimentos."
                );
            }

            if (estabelecimento.getDtSuspensaoAte() != null
                    && estabelecimento.getDtSuspensaoAte()
                    .isAfter(LocalDateTime.now())) {

                throw new IllegalStateException(
                        "O estabelecimento possui uma suspensão temporária ativa."
                );
            }
        }

        estabelecimento.setFlAcolhimentoAtivo(
                dto.flAcolhimentoAtivo()
        );

        return verificarAtualizacao(
                estabelecimentoDAO.atualizar(estabelecimento)
        );
    }

    // =====================================================
    // CONFIGURAR OU REMOVER SUSPENSÃO TEMPORÁRIA
    // =====================================================

    @Transactional
    public int atualizarSuspensao(
            Long id,
            EstabelecimentoSuspensaoDTO dto) {

        validarId(id);

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados da suspensão não informados."
            );
        }

        Estabelecimento estabelecimento =
                buscarEstabelecimentoExistente(id);

        if (!"ATIVO".equals(
                estabelecimento.getStEstabelecimento())) {

            throw new IllegalStateException(
                    "Somente estabelecimentos ativos podem configurar suspensão."
            );
        }

        if (dto.dtSuspensaoAte() != null
                && !dto.dtSuspensaoAte().isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "A data de suspensão deve estar no futuro."
            );
        }

        estabelecimento.setDtSuspensaoAte(
                dto.dtSuspensaoAte()
        );

        return verificarAtualizacao(
                estabelecimentoDAO.atualizar(estabelecimento)
        );
    }

    // Mantido para compatibilidade com chamadas existentes.
    public int suspenderAcolhimento(
            Long id,
            EstabelecimentoSuspensaoDTO dto) {

        return atualizarSuspensao(id, dto);
    }

    public int removerSuspensao(Long id) {

        return atualizarSuspensao(
                id,
                new EstabelecimentoSuspensaoDTO(null)
        );
    }

    // =====================================================
    // ATUALIZAR STATUS DO ESTABELECIMENTO
    // =====================================================

    @Transactional
    public int atualizarStatus(
            Long id,
            EstabelecimentoStatusUpdateDTO dto) {

        validarId(id);

        if (dto == null
                || dto.stEstabelecimento() == null
                || dto.stEstabelecimento().isBlank()) {

            throw new IllegalArgumentException(
                    "Informe o status do estabelecimento."
            );
        }

        String status = dto.stEstabelecimento()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!STATUS_PERMITIDOS.contains(status)) {
            throw new IllegalArgumentException(
                    "Status inválido. Utilize PENDENTE, ATIVO, SUSPENSO ou REPROVADO."
            );
        }

        buscarEstabelecimentoExistente(id);

        int linhasAfetadas =
                estabelecimentoDAO.atualizarStatus(id, status);

        if (linhasAfetadas == 0) {
            throw new IllegalArgumentException(
                    "Estabelecimento não encontrado."
            );
        }

        return linhasAfetadas;
    }

    // =====================================================
    // BUSCAR ESTABELECIMENTOS PRÓXIMOS
    // =====================================================

    public List<Estabelecimento> buscarProximos(
            Double latitude,
            Double longitude) {

        if (latitude == null
                || longitude == null
                || !Double.isFinite(latitude)
                || !Double.isFinite(longitude)
                || latitude < -90
                || latitude > 90
                || longitude < -180
                || longitude > 180) {

            throw new IllegalArgumentException(
                    "Coordenadas inválidas."
            );
        }

        return estabelecimentoDAO.buscarProximos(
                latitude,
                longitude
        );
    }

    // =====================================================
    // BUSCAR ESTABELECIMENTOS POR CEP
    // =====================================================

    public List<Estabelecimento> buscarPorCep(String cep) {

        String cepNormalizado = somenteDigitos(cep);

        if (!cepNormalizado.matches("\\d{8}")) {
            throw new IllegalArgumentException(
                    "CEP inválido. Informe 8 dígitos."
            );
        }

        return estabelecimentoDAO.buscarPorCep(cepNormalizado);
    }

    // =====================================================
    // MÉTODOS AUXILIARES
    // =====================================================

    private Estabelecimento buscarEstabelecimentoExistente(Long id) {

        return estabelecimentoDAO.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Estabelecimento não encontrado."
                        )
                );
    }

    private int verificarAtualizacao(int linhasAfetadas) {

        if (linhasAfetadas == 0) {
            throw new IllegalArgumentException(
                    "Estabelecimento não encontrado ou nenhum registro foi atualizado."
            );
        }

        return linhasAfetadas;
    }

    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "ID do estabelecimento inválido."
            );
        }
    }

    // =====================================================
    // VALIDAÇÃO DO CADASTRO
    // =====================================================

    private void validarDadosCadastro(EstabelecimentoRequestDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados do estabelecimento são obrigatórios."
            );
        }

        validarCnpj(dto.nrCnpj());

        if (dto.nmEmpresa() == null || dto.nmEmpresa().isBlank()) {
            throw new IllegalArgumentException(
                    "Nome da empresa é obrigatório."
            );
        }

        if (dto.dsEmail() == null || dto.dsEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "E-mail é obrigatório."
            );
        }

        if (dto.dsTipoEstabelecimento() == null
                || dto.dsTipoEstabelecimento().isBlank()) {

            throw new IllegalArgumentException(
                    "Tipo de estabelecimento é obrigatório."
            );
        }

        validarCoordenadas(
                dto.nrLatitude(),
                dto.nrLongitude()
        );

        if (dto.responsavel() == null) {
            throw new IllegalArgumentException(
                    "Responsável pelo estabelecimento é obrigatório."
            );
        }
    }

    // =====================================================
    // VALIDAÇÃO DA ATUALIZAÇÃO
    // =====================================================

    private void validarDadosAtualizacao(
            EstabelecimentoUpdateDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados do estabelecimento não informados."
            );
        }

        if (dto.nmEmpresa() == null || dto.nmEmpresa().isBlank()) {
            throw new IllegalArgumentException(
                    "Nome da empresa é obrigatório."
            );
        }

        if (dto.dsEmail() == null || dto.dsEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "E-mail é obrigatório."
            );
        }

        if (dto.dsTipoEstabelecimento() == null
                || dto.dsTipoEstabelecimento().isBlank()) {

            throw new IllegalArgumentException(
                    "Tipo de estabelecimento é obrigatório."
            );
        }

        if ((dto.nrLatitude() == null)
                != (dto.nrLongitude() == null)) {

            throw new IllegalArgumentException(
                    "Latitude e longitude devem ser informadas juntas."
            );
        }

        if (dto.nrLatitude() != null) {
            validarCoordenadas(
                    dto.nrLatitude(),
                    dto.nrLongitude()
            );
        }
    }

    // =====================================================
    // VALIDAÇÃO DAS COORDENADAS
    // =====================================================

    private void validarCoordenadas(
            BigDecimal latitude,
            BigDecimal longitude) {

        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException(
                    "Latitude e longitude são obrigatórias."
            );
        }

        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0
                || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {

            throw new IllegalArgumentException(
                    "Latitude deve estar entre -90 e 90."
            );
        }

        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0
                || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {

            throw new IllegalArgumentException(
                    "Longitude deve estar entre -180 e 180."
            );
        }
    }

    // =====================================================
    // VALIDAR DADOS RETORNADOS PELA BRASIL API - CNPJ
    // =====================================================

    private void validarDadosCnpj(
            BrasilApiCnpjResponse dadosCnpj,
            String cnpjInformado,
            boolean exigirCnpjAtivo) {

        if (dadosCnpj == null) {
            throw new IllegalArgumentException(
                    "A Brasil API não retornou os dados do CNPJ."
            );
        }

        String cnpjRetornado = somenteDigitos(dadosCnpj.cnpj());

        if (!cnpjInformado.equals(cnpjRetornado)) {
            throw new IllegalArgumentException(
                    "O CNPJ retornado pela Brasil API não corresponde ao CNPJ informado."
            );
        }

        if (dadosCnpj.razaoSocial() == null
                || dadosCnpj.razaoSocial().isBlank()) {

            throw new IllegalArgumentException(
                    "A Brasil API não retornou a razão social."
            );
        }

        if (exigirCnpjAtivo
                && !"ATIVA".equalsIgnoreCase(
                normalizar(dadosCnpj.descricaoSituacaoCadastral()))) {

            throw new IllegalArgumentException(
                    "O CNPJ não está ativo na Receita Federal."
            );
        }

        String cep = somenteDigitos(dadosCnpj.cep());

        if (!cep.matches("\\d{8}")) {
            throw new IllegalArgumentException(
                    "A Brasil API não retornou um CEP válido para o CNPJ."
            );
        }
    }

    // =====================================================
    // VALIDAR DADOS RETORNADOS PELA BRASIL API - CEP
    // =====================================================

    private void validarDadosCep(
            BrasilApiCnpjResponse dadosCnpj,
            BrasilApiCepResponse dadosCep) {

        if (dadosCep == null) {
            throw new IllegalArgumentException(
                    "A Brasil API não retornou os dados do CEP."
            );
        }

        String cepCnpj = somenteDigitos(dadosCnpj.cep());
        String cepConsultado = somenteDigitos(dadosCep.cep());

        if (!cepCnpj.equals(cepConsultado)) {
            throw new IllegalArgumentException(
                    "O CEP consultado não corresponde ao CEP vinculado ao CNPJ."
            );
        }

        if (dadosCep.city() == null || dadosCep.city().isBlank()
                || dadosCep.uf() == null || dadosCep.uf().isBlank()) {

            throw new IllegalArgumentException(
                    "A Brasil API não retornou cidade e UF para o CEP."
            );
        }

        if (dadosCnpj.municipio() != null
                && !normalizar(dadosCnpj.municipio())
                .equals(normalizar(dadosCep.city()))) {

            throw new IllegalArgumentException(
                    "A cidade retornada pelo CEP diverge da cidade do CNPJ."
            );
        }

        if (dadosCnpj.uf() != null
                && !normalizar(dadosCnpj.uf())
                .equals(normalizar(dadosCep.uf()))) {

            throw new IllegalArgumentException(
                    "A UF retornada pelo CEP diverge da UF do CNPJ."
            );
        }
    }

    // =====================================================
    // VALIDAR FORMATO E DÍGITOS VERIFICADORES DO CNPJ
    // =====================================================

    private void validarCnpj(String cnpj) {

        if (cnpj == null || !cnpj.matches("\\d{14}")) {
            throw new IllegalArgumentException(
                    "CNPJ deve conter 14 dígitos."
            );
        }

        if (cnpj.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("CNPJ inválido.");
        }

        int[] pesosPrimeiroDigito = {
                5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2
        };

        int[] pesosSegundoDigito = {
                6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2
        };

        int primeiroDigito = calcularDigitoCnpj(
                cnpj.substring(0, 12),
                pesosPrimeiroDigito
        );

        if (primeiroDigito != Character.getNumericValue(
                cnpj.charAt(12))) {

            throw new IllegalArgumentException("CNPJ inválido.");
        }

        int segundoDigito = calcularDigitoCnpj(
                cnpj.substring(0, 13),
                pesosSegundoDigito
        );

        if (segundoDigito != Character.getNumericValue(
                cnpj.charAt(13))) {

            throw new IllegalArgumentException("CNPJ inválido.");
        }
    }

    private int calcularDigitoCnpj(
            String base,
            int[] pesos) {

        int soma = 0;

        for (int i = 0; i < pesos.length; i++) {
            soma += Character.getNumericValue(base.charAt(i))
                    * pesos[i];
        }

        int resto = soma % 11;

        return resto < 2 ? 0 : 11 - resto;
    }

    // =====================================================
    // CONVERTER REQUEST DTO E BRASIL API PARA MODEL
    // =====================================================

    private Estabelecimento converterParaModel(
            EstabelecimentoRequestDTO dto,
            BrasilApiCnpjResponse dadosCnpj,
            BrasilApiCepResponse dadosCep) {

        return Estabelecimento.builder()

                .nrCnpj(somenteDigitos(dadosCnpj.cnpj()))
                .nmRazaoSocial(dadosCnpj.razaoSocial().trim())
                .nmFantasia(
                        dadosCnpj.nomeFantasia() == null
                                || dadosCnpj.nomeFantasia().isBlank()
                                ? null
                                : dadosCnpj.nomeFantasia().trim()
                )

                .nmEmpresa(dto.nmEmpresa().trim())
                .dsEmail(dto.dsEmail().trim().toLowerCase(Locale.ROOT))
                .nrTelefone(normalizarOpcional(dto.nrTelefone()))
                .dsTipoEstabelecimento(
                        dto.dsTipoEstabelecimento().trim()
                )

                .nrCep(somenteDigitos(dadosCep.cep()))
                .dsLogradouro(normalizarOpcional(dadosCnpj.logradouro()))
                .nrEndereco(normalizarOpcional(dadosCnpj.numero()))
                .dsComplemento(normalizarOpcional(dadosCnpj.complemento()))
                .nmBairro(normalizarOpcional(dadosCnpj.bairro()))
                .nmCidade(dadosCep.city().trim())
                .sgUf(dadosCep.uf().trim().toUpperCase(Locale.ROOT))

                .nrLatitude(dto.nrLatitude())
                .nrLongitude(dto.nrLongitude())

                // O SQL define PENDENTE e N como valores padrão.
                .stEstabelecimento("PENDENTE")
                .flAcolhimentoAtivo("N")

                .build();
    }

    // =====================================================
    // CONVERTER UPDATE DTO E BRASIL API PARA MODEL
    // =====================================================

    private Estabelecimento converterParaModel(
            EstabelecimentoUpdateDTO dto,
            Estabelecimento existente,
            BrasilApiCnpjResponse dadosCnpj,
            BrasilApiCepResponse dadosCep) {

        return Estabelecimento.builder()

                .cdEstabelecimento(existente.getCdEstabelecimento())

                // DADOS OFICIAIS
                .nrCnpj(existente.getNrCnpj())
                .nmRazaoSocial(dadosCnpj.razaoSocial().trim())
                .nmFantasia(
                        dadosCnpj.nomeFantasia() == null
                                || dadosCnpj.nomeFantasia().isBlank()
                                ? null
                                : dadosCnpj.nomeFantasia().trim()
                )

                // DADOS OPERACIONAIS EDITÁVEIS
                .nmEmpresa(dto.nmEmpresa().trim())
                .dsEmail(dto.dsEmail().trim().toLowerCase(Locale.ROOT))
                .nrTelefone(normalizarOpcional(dto.nrTelefone()))
                .dsTipoEstabelecimento(
                        dto.dsTipoEstabelecimento().trim()
                )

                // ENDEREÇO OFICIAL
                .nrCep(somenteDigitos(dadosCep.cep()))
                .dsLogradouro(normalizarOpcional(dadosCnpj.logradouro()))
                .nrEndereco(normalizarOpcional(dadosCnpj.numero()))
                .dsComplemento(normalizarOpcional(dadosCnpj.complemento()))
                .nmBairro(normalizarOpcional(dadosCnpj.bairro()))
                .nmCidade(dadosCep.city().trim())
                .sgUf(dadosCep.uf().trim().toUpperCase(Locale.ROOT))

                // PRESERVAR COORDENADAS NÃO ENVIADAS
                .nrLatitude(
                        dto.nrLatitude() != null
                                ? dto.nrLatitude()
                                : existente.getNrLatitude()
                )
                .nrLongitude(
                        dto.nrLongitude() != null
                                ? dto.nrLongitude()
                                : existente.getNrLongitude()
                )

                // PRESERVAR CONTROLES OPERACIONAIS
                .flAcolhimentoAtivo(
                        existente.getFlAcolhimentoAtivo()
                )
                .dtSuspensaoAte(
                        existente.getDtSuspensaoAte()
                )
                .stEstabelecimento(
                        existente.getStEstabelecimento()
                )
                .dtCadastro(
                        existente.getDtCadastro()
                )

                .build();
    }

    // =====================================================
    // NORMALIZAÇÃO
    // =====================================================

    private String normalizar(String valor) {

        if (valor == null) {
            return "";
        }

        return Normalizer.normalize(
                        valor.trim(),
                        Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
    }

    private String somenteDigitos(String valor) {

        if (valor == null) {
            return "";
        }

        return valor.replaceAll("\\D", "");
    }

    private String normalizarOpcional(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
