
package br.com.ari.service;

import br.com.ari.exception.CredenciaisInvalidasException;
import br.com.ari.exception.RecursoNaoEncontradoException;
import br.com.ari.dao.FuncionarioDAO;
import br.com.ari.dto.FuncionarioLoginDTO;
import br.com.ari.dto.FuncionarioRequestDTO;
import br.com.ari.dto.FuncionarioResponseDTO;
import br.com.ari.dto.FuncionarioSenhaUpdateDTO;
import br.com.ari.dto.FuncionarioUpdateDTO;
import br.com.ari.dto.ResponsavelEstabelecimentoRequestDTO;
import br.com.ari.model.Funcionario;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
public class FuncionarioService {

    private static final String PERFIL_ADMIN = "ADMIN";
    private static final String PERFIL_ATENDENTE = "ATENDENTE";

    private static final String STATUS_ATIVO = "ATIVO";
    private static final String STATUS_INATIVO = "INATIVO";

    private static final String NAO_TREINADO = "N";

    private final FuncionarioDAO funcionarioDAO;
    private final PasswordEncoder passwordEncoder;
    private final FuncionarioSessaoService funcionarioSessaoService;

    public FuncionarioService(
            FuncionarioDAO funcionarioDAO,
            PasswordEncoder passwordEncoder,
            FuncionarioSessaoService funcionarioSessaoService) {

        this.funcionarioDAO = funcionarioDAO;
        this.passwordEncoder = passwordEncoder;
        this.funcionarioSessaoService = funcionarioSessaoService;
    }

    // =====================================================
    // CADASTRAR FUNCIONÁRIO
    // Somente ADMIN.
    // O funcionário pertence ao estabelecimento do ADMIN.
    // Todo novo funcionário começa ATENDENTE, NÃO TREINADO
    // e ATIVO.
    // =====================================================

    @Transactional
    public int criar(
            Long cdFuncionarioAdmin,
            FuncionarioRequestDTO dto) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados do funcionário não informados."
            );
        }

        validarNome(dto.nmFuncionario());
        validarEmail(dto.dsEmailLogin());
        validarSenha(dto.dsSenha());

        String email = normalizarEmail(dto.dsEmailLogin());

        validarEmailDisponivel(email, null);

        Funcionario funcionario = Funcionario.builder()
                .cdEstabelecimento(admin.getCdEstabelecimento())
                .nmFuncionario(dto.nmFuncionario().trim())
                .nrTelefone(normalizarTelefone(dto.nrTelefone()))
                .dsEmailLogin(email)
                .dsSenhaHash(passwordEncoder.encode(dto.dsSenha()))
                .tpPerfil(PERFIL_ATENDENTE)
                .flTreinado(NAO_TREINADO)
                .stFuncionario(STATUS_ATIVO)
                .build();

        try {
            int linhasAfetadas = funcionarioDAO.criar(funcionario);

            validarLinhasAfetadas(
                    linhasAfetadas,
                    "Não foi possível cadastrar o funcionário."
            );

            return linhasAfetadas;

        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "Não foi possível cadastrar. Verifique se o e-mail já está sendo utilizado.",
                    e
            );
        }
    }

    // =====================================================
    // CADASTRAR ADMINISTRADOR INICIAL
    // Utilizado durante a criação do estabelecimento.
    // Deve participar da mesma transação do estabelecimento.
    // =====================================================

    @Transactional
    public int criarAdministradorInicial(
            Long cdEstabelecimento,
            ResponsavelEstabelecimentoRequestDTO dto) {

        validarId(cdEstabelecimento);

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados do responsável não informados."
            );
        }

        validarNome(dto.nmResponsavel());
        validarEmail(dto.dsEmailLogin());
        validarSenha(dto.dsSenha());

        String email = normalizarEmail(dto.dsEmailLogin());

        validarEmailDisponivel(email, null);

        Funcionario administrador = Funcionario.builder()
                .cdEstabelecimento(cdEstabelecimento)
                .nmFuncionario(dto.nmResponsavel().trim())
                .nrTelefone(normalizarTelefone(dto.nrTelefone()))
                .dsEmailLogin(email)
                .dsSenhaHash(passwordEncoder.encode(dto.dsSenha()))
                .tpPerfil(PERFIL_ADMIN)
                .flTreinado(NAO_TREINADO)
                .stFuncionario(STATUS_ATIVO)
                .build();

        try {
            int linhasAfetadas = funcionarioDAO.criar(administrador);

            validarLinhasAfetadas(
                    linhasAfetadas,
                    "Não foi possível cadastrar o administrador."
            );

            return linhasAfetadas;

        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "Não foi possível cadastrar o administrador. Verifique se o e-mail já está sendo utilizado.",
                    e
            );
        }
    }

    // =====================================================
    // BUSCAR FUNCIONÁRIO POR ID
    // Somente ADMIN do mesmo estabelecimento.
    // =====================================================

    public FuncionarioResponseDTO buscarPorId(
            Long cdFuncionarioAdmin,
            Long cdFuncionarioAlvo) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        Funcionario funcionario = buscarFuncionarioDaEmpresa(
                cdFuncionarioAlvo,
                admin.getCdEstabelecimento()
        );

        return converterParaResponse(funcionario);
    }

    // =====================================================
    // LISTAR FUNCIONÁRIOS DO ESTABELECIMENTO
    // Somente ADMIN.
    // Inclui funcionários ATIVOS e INATIVOS.
    // =====================================================

    public List<FuncionarioResponseDTO> buscarPorEstabelecimento(
            Long cdFuncionarioAdmin) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        return funcionarioDAO.buscarPorEstabelecimento(
                        admin.getCdEstabelecimento()
                )
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    // =====================================================
    // BUSCAR DADOS DO FUNCIONÁRIO AUTENTICADO
    // ADMIN e ATENDENTE ativos.
    // =====================================================

    public FuncionarioResponseDTO buscarFuncionarioAutenticado(
            Long cdFuncionarioAutenticado) {

        Funcionario funcionario = buscarFuncionarioAtivo(
                cdFuncionarioAutenticado
        );

        return converterParaResponse(funcionario);
    }

    // =====================================================
    // ATUALIZAR OS PRÓPRIOS DADOS
    // ADMIN e ATENDENTE podem atualizar:
    // nome, telefone e e-mail.
    //
    // Não altera perfil, treinamento, status ou senha.
    // O ID vem da autenticação, não do corpo da requisição.
    // =====================================================

    @Transactional
    public int atualizar(
            Long cdFuncionarioAutenticado,
            FuncionarioUpdateDTO dto) {

        Funcionario existente = buscarFuncionarioAtivo(
                cdFuncionarioAutenticado
        );

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados do funcionário não informados."
            );
        }

        validarNome(dto.nmFuncionario());
        validarEmail(dto.dsEmailLogin());

        String email = normalizarEmail(dto.dsEmailLogin());

        validarEmailDisponivel(
                email,
                cdFuncionarioAutenticado
        );

        Funcionario funcionario = Funcionario.builder()
                .cdFuncionario(existente.getCdFuncionario())
                .cdEstabelecimento(existente.getCdEstabelecimento())
                .nmFuncionario(dto.nmFuncionario().trim())
                .nrTelefone(normalizarTelefone(dto.nrTelefone()))
                .dsEmailLogin(email)
                .dsSenhaHash(existente.getDsSenhaHash())
                .tpPerfil(existente.getTpPerfil())
                .flTreinado(existente.getFlTreinado())
                .stFuncionario(existente.getStFuncionario())
                .dtCadastro(existente.getDtCadastro())
                .build();

        try {
            int linhasAfetadas = funcionarioDAO.atualizar(funcionario);

            validarLinhasAfetadas(
                    linhasAfetadas,
                    "Não foi possível atualizar os dados do funcionário."
            );

            return linhasAfetadas;

        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "Não foi possível atualizar. O e-mail informado já está sendo utilizado.",
                    e
            );
        }
    }

    // =====================================================
    // ATUALIZAR A PRÓPRIA SENHA
    // ADMIN e ATENDENTE ativos.
    // =====================================================

    @Transactional
    public int atualizarSenha(
            Long cdFuncionarioAutenticado,
            FuncionarioSenhaUpdateDTO dto) {

        Funcionario funcionario = buscarFuncionarioAtivo(
                cdFuncionarioAutenticado
        );

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados da nova senha não informados."
            );
        }

        validarSenha(dto.dsNovaSenha());

        String senhaHash = passwordEncoder.encode(
                dto.dsNovaSenha()
        );

        int linhasAfetadas = funcionarioDAO.atualizarSenha(
                funcionario.getCdFuncionario(),
                senhaHash
        );

        validarLinhasAfetadas(
                linhasAfetadas,
                "Não foi possível atualizar a senha."
        );

        return linhasAfetadas;
    }

    // =====================================================
    // ATUALIZAR TREINAMENTO DO FUNCIONÁRIO
    // Somente ADMIN do mesmo estabelecimento.
    // =====================================================

    @Transactional
    public int atualizarTreinamento(
            Long cdFuncionarioAdmin,
            Long cdFuncionarioAlvo,
            String flTreinado) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        if (!"S".equals(flTreinado) && !"N".equals(flTreinado)) {
            throw new IllegalArgumentException(
                    "Informe S para treinado ou N para não treinado."
            );
        }

        Funcionario funcionario = buscarFuncionarioDaEmpresa(
                cdFuncionarioAlvo,
                admin.getCdEstabelecimento()
        );

        if (!STATUS_ATIVO.equals(funcionario.getStFuncionario())) {
            throw new IllegalStateException(
                    "Não é possível alterar o treinamento de um funcionário inativo."
            );
        }

        int linhasAfetadas = funcionarioDAO.atualizarTreinamento(
                funcionario.getCdFuncionario(),
                admin.getCdEstabelecimento(),
                flTreinado
        );

        validarLinhasAfetadas(
                linhasAfetadas,
                "Não foi possível atualizar o treinamento do funcionário."
        );

        return linhasAfetadas;
    }

    // =====================================================
    // INATIVAR FUNCIONÁRIO
    // Somente ADMIN do mesmo estabelecimento.
    // Não permite inativar a própria conta.
    // Encerra todas as sessões do funcionário.
    // =====================================================

    @Transactional
    public int inativar(
            Long cdFuncionarioAdmin,
            Long cdFuncionarioAlvo) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        Funcionario funcionario = buscarFuncionarioDaEmpresa(
                cdFuncionarioAlvo,
                admin.getCdEstabelecimento()
        );

        if (Objects.equals(
                funcionario.getCdFuncionario(),
                admin.getCdFuncionario())) {

            throw new IllegalArgumentException(
                    "O administrador não pode inativar a própria conta por esta operação."
            );
        }

        if (STATUS_INATIVO.equals(funcionario.getStFuncionario())) {
            throw new IllegalStateException(
                    "Este funcionário já está inativo."
            );
        }

        int linhasAfetadas = funcionarioDAO.inativar(
                cdFuncionarioAlvo,
                admin.getCdEstabelecimento()
        );

        validarLinhasAfetadas(
                linhasAfetadas,
                "Não foi possível inativar o funcionário."
        );

        funcionarioSessaoService.encerrarTodasDoFuncionario(
                cdFuncionarioAlvo
        );

        return linhasAfetadas;
    }

    // =====================================================
    // EXCLUIR FUNCIONÁRIO
    // Exclusão física.
    // Somente ADMIN do mesmo estabelecimento.
    // Não permite excluir a própria conta.
    // =====================================================

    @Transactional
    public int excluir(
            Long cdFuncionarioAdmin,
            Long cdFuncionarioAlvo) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        Funcionario funcionario = buscarFuncionarioDaEmpresa(
                cdFuncionarioAlvo,
                admin.getCdEstabelecimento()
        );

        if (Objects.equals(
                funcionario.getCdFuncionario(),
                admin.getCdFuncionario())) {

            throw new IllegalArgumentException(
                    "O administrador não pode excluir a própria conta por esta operação. "
                            + "Para excluir seu cadastro, utilize a exclusão do estabelecimento."
            );
        }

        try {
            int linhasAfetadas = funcionarioDAO.excluir(
                    cdFuncionarioAlvo
            );

            validarLinhasAfetadas(
                    linhasAfetadas,
                    "Não foi possível excluir o funcionário."
            );

            funcionarioSessaoService.encerrarTodasDoFuncionario(
                    cdFuncionarioAlvo
            );

            return linhasAfetadas;

        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "Não foi possível excluir o funcionário porque existem registros relacionados a ele.",
                    e
            );
        }
    }

    // =====================================================
    // AUTENTICAR FUNCIONÁRIO
    // ADMIN e ATENDENTE ativos podem fazer login.
    // O treinamento não impede o login.
    // =====================================================

    public FuncionarioResponseDTO autenticar(
            FuncionarioLoginDTO dto) {

        if (dto == null
                || dto.dsEmailLogin() == null
                || dto.dsEmailLogin().isBlank()
                || dto.dsSenha() == null
                || dto.dsSenha().isBlank()) {

            throw new IllegalArgumentException(
                    "E-mail e senha são obrigatórios."
            );
        }

        String email = normalizarEmail(dto.dsEmailLogin());

        Funcionario funcionario = funcionarioDAO
                .buscarPorEmailLogin(email)
                .orElseThrow(() ->
                        new CredenciaisInvalidasException(
                                "E-mail ou senha inválidos."
                        )
                );

        if (!STATUS_ATIVO.equals(funcionario.getStFuncionario())) {
            throw new CredenciaisInvalidasException(
                    "E-mail ou senha inválidos."
            );
        }

        boolean senhaValida = passwordEncoder.matches(
                dto.dsSenha(),
                funcionario.getDsSenhaHash()
        );

        if (!senhaValida) {
            throw new CredenciaisInvalidasException(
                    "E-mail ou senha inválidos."
            );
        }

        return converterParaResponse(funcionario);
    }

    // =====================================================
    // BUSCAR ID DO ADMIN AUTENTICADO
    // O Principal deve conter o ID numérico do funcionário.
    // =====================================================

    public Long buscarIdAdminAutenticado(Long cdFuncionario) {

        Funcionario admin = buscarAdmin(cdFuncionario);

        return admin.getCdFuncionario();
    }

    // =====================================================
    // BUSCAR ID DO FUNCIONÁRIO AUTENTICADO
    // ADMIN e ATENDENTE ativos.
    // =====================================================

    public Long buscarIdFuncionarioAutenticado(Long cdFuncionario) {

        Funcionario funcionario = buscarFuncionarioAtivo(
                cdFuncionario
        );

        return funcionario.getCdFuncionario();
    }

    // =====================================================
    // VALIDAR ACESSO DO ADMIN AO ESTABELECIMENTO
    // =====================================================

    public void validarAcessoAdminEstabelecimento(
            Long cdFuncionarioAdmin,
            Long cdEstabelecimento) {

        validarId(cdEstabelecimento);

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        if (!Objects.equals(
                admin.getCdEstabelecimento(),
                cdEstabelecimento)) {

            throw new IllegalArgumentException(
                    "Você não possui acesso a este estabelecimento."
            );
        }
    }

    // =====================================================
    // CONVERTER MODEL PARA RESPONSE DTO
    // Nunca retorna o hash da senha.
    // =====================================================

    private FuncionarioResponseDTO converterParaResponse(
            Funcionario funcionario) {

        return new FuncionarioResponseDTO(
                funcionario.getCdFuncionario(),
                funcionario.getCdEstabelecimento(),
                funcionario.getNmFuncionario(),
                funcionario.getNrTelefone(),
                funcionario.getDsEmailLogin(),
                funcionario.getTpPerfil(),
                funcionario.getFlTreinado(),
                funcionario.getStFuncionario(),
                funcionario.getDtCadastro()
        );
    }

    // =====================================================
    // BUSCAR E VALIDAR ADMIN
    // =====================================================

    private Funcionario buscarAdmin(Long id) {

        validarId(id);

        Funcionario admin = funcionarioDAO.buscarPorId(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Funcionário administrador não encontrado."
                        )
                );

        validarFuncionarioAtivo(admin);

        if (!PERFIL_ADMIN.equals(admin.getTpPerfil())) {
            throw new IllegalArgumentException(
                    "Acesso permitido somente para ADMIN."
            );
        }

        return admin;
    }

    // =====================================================
    // BUSCAR FUNCIONÁRIO DO ESTABELECIMENTO
    // =====================================================

    private Funcionario buscarFuncionarioDaEmpresa(
            Long id,
            Long cdEstabelecimento) {

        validarId(id);

        Funcionario funcionario = funcionarioDAO.buscarPorId(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Funcionário não encontrado."
                        )
                );

        if (!Objects.equals(
                funcionario.getCdEstabelecimento(),
                cdEstabelecimento)) {

            throw new IllegalArgumentException(
                    "Funcionário não pertence ao seu estabelecimento."
            );
        }

        return funcionario;
    }

    // =====================================================
    // BUSCAR FUNCIONÁRIO ATIVO
    // =====================================================

    private Funcionario buscarFuncionarioAtivo(Long id) {

        validarId(id);

        Funcionario funcionario = funcionarioDAO.buscarPorId(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Funcionário não encontrado."
                        )
                );

        validarFuncionarioAtivo(funcionario);

        return funcionario;
    }

    // =====================================================
    // VALIDAR SITUAÇÃO DO FUNCIONÁRIO
    // =====================================================

    private void validarFuncionarioAtivo(Funcionario funcionario) {

        if (!STATUS_ATIVO.equals(funcionario.getStFuncionario())) {
            throw new IllegalStateException(
                    "Funcionário inativo não pode realizar esta operação."
            );
        }
    }

    // =====================================================
    // VALIDAR E-MAIL DISPONÍVEL
    // =====================================================

    private void validarEmailDisponivel(
            String email,
            Long cdFuncionarioIgnorado) {

        Optional<Funcionario> funcionarioComEmail =
                funcionarioDAO.buscarPorEmailLogin(email);

        if (funcionarioComEmail.isPresent()) {

            Long idEncontrado = funcionarioComEmail.get()
                    .getCdFuncionario();

            if (cdFuncionarioIgnorado == null
                    || !idEncontrado.equals(cdFuncionarioIgnorado)) {

                throw new IllegalArgumentException(
                        "Este e-mail já está sendo utilizado."
                );
            }
        }
    }

    // =====================================================
    // NORMALIZAR E-MAIL
    // =====================================================

    private String normalizarEmail(String email) {

        if (email == null) {
            throw new IllegalArgumentException(
                    "E-mail de login é obrigatório."
            );
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    // =====================================================
    // NORMALIZAR TELEFONE
    // =====================================================

    private String normalizarTelefone(String telefone) {

        if (telefone == null || telefone.isBlank()) {
            return null;
        }

        return telefone.trim();
    }

    // =====================================================
    // VALIDAR NOME
    // =====================================================

    private void validarNome(String nome) {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException(
                    "Nome é obrigatório."
            );
        }

        if (nome.trim().length() > 120) {
            throw new IllegalArgumentException(
                    "O nome deve ter no máximo 120 caracteres."
            );
        }
    }

    // =====================================================
    // VALIDAR E-MAIL
    // =====================================================

    private void validarEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "E-mail de login é obrigatório."
            );
        }

        String emailNormalizado = email.trim();

        if (emailNormalizado.length() > 150
                || !emailNormalizado.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            throw new IllegalArgumentException(
                    "E-mail informado é inválido."
            );
        }
    }

    // =====================================================
    // VALIDAR SENHA
    // =====================================================

    private void validarSenha(String senha) {

        if (senha == null || senha.length() < 8) {
            throw new IllegalArgumentException(
                    "A senha deve ter pelo menos 8 caracteres."
            );
        }
    }

    // =====================================================
    // VALIDAR ID
    // =====================================================

    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "ID inválido."
            );
        }
    }

    // =====================================================
    // VALIDAR RESULTADO DO DAO
    // =====================================================

    private void validarLinhasAfetadas(
            int linhasAfetadas,
            String mensagem) {

        if (linhasAfetadas != 1) {
            throw new IllegalStateException(mensagem);
        }
    }
}
