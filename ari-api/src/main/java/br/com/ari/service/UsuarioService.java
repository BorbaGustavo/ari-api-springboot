
package br.com.ari.service;

import br.com.ari.dao.UsuarioDAO;
import br.com.ari.dto.LoginRequestDTO;
import br.com.ari.dto.LoginResponseDTO;
import br.com.ari.dto.UsuarioRequestDTO;
import br.com.ari.dto.UsuarioResponseDTO;
import br.com.ari.dto.UsuarioSenhaUpdateDTO;
import br.com.ari.dto.UsuarioUpdateDTO;
import br.com.ari.model.SessaoWeb;
import br.com.ari.model.Usuario;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
public class UsuarioService {

    private static final String STATUS_ATIVO = "ATIVO";

    private static final int TAMANHO_MAX_NOME = 120;
    private static final int TAMANHO_MAX_TELEFONE = 20;
    private static final int TAMANHO_MAX_EMAIL = 150;
    private static final int TAMANHO_MIN_SENHA = 8;

    private final UsuarioDAO usuarioDAO;
    private final PasswordEncoder passwordEncoder;
    private final SessaoWebService sessaoWebService;

    // CONSTRUTOR
    public UsuarioService(
            UsuarioDAO usuarioDAO,
            PasswordEncoder passwordEncoder,
            SessaoWebService sessaoWebService) {

        this.usuarioDAO = usuarioDAO;
        this.passwordEncoder = passwordEncoder;
        this.sessaoWebService = sessaoWebService;
    }

    // ==================================================
    // CADASTRAR USUÁRIO
    // OPERAÇÃO PÚBLICA
    // ==================================================

    public int criar(UsuarioRequestDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados do usuário não informados."
            );
        }

        validarDadosCadastro(dto);

        String email = normalizarEmail(dto.dsEmail());

        // VERIFICAR E-MAIL DUPLICADO
        if (usuarioDAO.buscarPorEmail(email).isPresent()) {
            throw new IllegalArgumentException(
                    "Este e-mail já está cadastrado."
            );
        }

        // GERAR HASH DA SENHA
        String senhaHash = passwordEncoder.encode(dto.dsSenha());

        // CONVERTER DTO PARA MODEL
        Usuario usuario = Usuario.builder()
                .nmUsuario(dto.nmUsuario().trim())
                .nrTelefone(normalizarTelefone(dto.nrTelefone()))
                .dsEmail(email)
                .dsSenhaHash(senhaHash)
                .stUsuario(STATUS_ATIVO)
                .build();

        try {

            int linhasAfetadas = usuarioDAO.criar(usuario);

            if (linhasAfetadas == 0) {
                throw new IllegalStateException(
                        "Não foi possível cadastrar o usuário."
                );
            }

            return linhasAfetadas;

        } catch (DuplicateKeyException e) {

            throw new IllegalArgumentException(
                    "Este e-mail já está cadastrado."
            );
        }
    }

    // ==================================================
    // AUTENTICAR USUÁRIO
    // OPERAÇÃO PÚBLICA
    // ==================================================

    public LoginResponseDTO autenticar(LoginRequestDTO dto) {

        if (dto == null
                || dto.dsEmail() == null
                || dto.dsEmail().isBlank()
                || dto.dsSenha() == null
                || dto.dsSenha().isBlank()) {

            throw new IllegalArgumentException(
                    "E-mail e senha são obrigatórios."
            );
        }

        String email = normalizarEmail(dto.dsEmail());

        // BUSCAR USUÁRIO PELO E-MAIL
        Usuario usuario = usuarioDAO.buscarPorEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "E-mail ou senha inválidos."
                        )
                );

        // VERIFICAR SE A CONTA ESTÁ ATIVA
        validarUsuarioAtivo(usuario);

        // CONFERIR SENHA COM HASH BCrypt
        boolean senhaValida = passwordEncoder.matches(
                dto.dsSenha(),
                usuario.getDsSenhaHash()
        );

        if (!senhaValida) {
            throw new IllegalArgumentException(
                    "E-mail ou senha inválidos."
            );
        }

        // CRIAR SESSÃO AUTENTICADA
        // A DURAÇÃO DE 8 HORAS É DEFINIDA NO SESSAOWEBSERVICE.
        SessaoWeb sessao = sessaoWebService.criar(
                usuario.getCdUsuario()
        );

        return new LoginResponseDTO(
                sessao.getDsUuid(),
                sessao.getDtExpiracao()
        );
    }

    // ==================================================
    // CONSULTAR PRÓPRIO PERFIL
    // OPERAÇÃO AUTENTICADA
    // ==================================================

    public UsuarioResponseDTO buscarMeuPerfil(String uuid) {

        Usuario usuario = obterUsuarioAutenticado(uuid);

        // CONVERTER MODEL PARA DTO DE RESPOSTA
        return converterParaResponseDTO(usuario);
    }

    // ==================================================
    // ATUALIZAR PRÓPRIO PERFIL
    // OPERAÇÃO AUTENTICADA
    // ==================================================

    public int atualizarMeuPerfil(
            String uuid,
            UsuarioUpdateDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados do usuário não informados."
            );
        }

        validarDadosAtualizacao(dto);

        // IDENTIFICAR USUÁRIO PELA SESSÃO
        Usuario existente = obterUsuarioAutenticado(uuid);

        String email = normalizarEmail(dto.dsEmail());

        // VERIFICAR SE O E-MAIL JÁ PERTENCE A OUTRA CONTA
        Optional<Usuario> usuarioComEmail =
                usuarioDAO.buscarPorEmail(email);

        if (usuarioComEmail.isPresent()
                && !usuarioComEmail.get()
                .getCdUsuario()
                .equals(existente.getCdUsuario())) {

            throw new IllegalArgumentException(
                    "Este e-mail já está sendo utilizado."
            );
        }

        // CONVERTER DTO PARA MODEL
        Usuario usuario = Usuario.builder()
                .cdUsuario(existente.getCdUsuario())
                .nmUsuario(dto.nmUsuario().trim())
                .nrTelefone(normalizarTelefone(dto.nrTelefone()))
                .dsEmail(email)
                .build();

        try {

            int linhasAfetadas = usuarioDAO.atualizar(usuario);

            if (linhasAfetadas == 0) {
                throw new IllegalStateException(
                        "Não foi possível atualizar o perfil."
                );
            }

            return linhasAfetadas;

        } catch (DuplicateKeyException e) {

            throw new IllegalArgumentException(
                    "Este e-mail já está sendo utilizado."
            );
        }
    }

    // ==================================================
    // ATUALIZAR PRÓPRIA SENHA
    // OPERAÇÃO AUTENTICADA
    // ==================================================

    public int atualizarMinhaSenha(
            String uuid,
            UsuarioSenhaUpdateDTO dto) {

        if (dto == null
                || dto.dsNovaSenha() == null
                || dto.dsNovaSenha().isBlank()) {

            throw new IllegalArgumentException(
                    "Nova senha não informada."
            );
        }

        if (dto.dsNovaSenha().length() < TAMANHO_MIN_SENHA) {
            throw new IllegalArgumentException(
                    "A senha deve ter pelo menos 8 caracteres."
            );
        }

        // IDENTIFICAR USUÁRIO PELA SESSÃO
        Usuario usuario = obterUsuarioAutenticado(uuid);

        // GERAR NOVO HASH BCrypt
        String senhaHash = passwordEncoder.encode(
                dto.dsNovaSenha()
        );

        int linhasAfetadas = usuarioDAO.atualizarSenha(
                usuario.getCdUsuario(),
                senhaHash
        );

        if (linhasAfetadas == 0) {
            throw new IllegalStateException(
                    "Não foi possível atualizar a senha."
            );
        }

        return linhasAfetadas;
    }

    // ==================================================
    // BLOQUEAR PRÓPRIA CONTA
    // OPERAÇÃO AUTENTICADA
    // ==================================================

    @Transactional
    public int excluirMinhaConta(String uuid) {

        // IDENTIFICAR USUÁRIO AUTENTICADO
        Usuario usuario = obterUsuarioAutenticado(uuid);

        // REALIZAR BLOQUEIO LÓGICO DA CONTA
        int linhasAfetadas = usuarioDAO.excluir(
                usuario.getCdUsuario()
        );

        if (linhasAfetadas == 0) {
            throw new IllegalStateException(
                    "Não foi possível bloquear a conta."
            );
        }

        // REVOGAR TODAS AS SESSÕES ATIVAS DO USUÁRIO
        sessaoWebService.revogarSessoesAtivasUsuario(
                usuario.getCdUsuario()
        );

        return linhasAfetadas;
    }

    // ==================================================
    // ENCERRAR SESSÃO
    // OPERAÇÃO AUTENTICADA
    // ==================================================

    public void encerrarSessao(String uuid) {

        // VALIDAR E RECUPERAR A SESSÃO ATIVA
        SessaoWeb sessao = obterSessaoAutenticada(uuid);

        // IMPEDIR LOGOUT POR SESSÃO ANÔNIMA
        if (sessao.getCdUsuario() == null) {
            throw new IllegalStateException(
                    "Esta operação exige autenticação."
            );
        }

        // REVOGAR A SESSÃO
        sessaoWebService.revogar(
                sessao.getCdSessaoWeb()
        );
    }

    // ==================================================
    // IDENTIFICAR USUÁRIO AUTENTICADO
    // ==================================================

    private Usuario obterUsuarioAutenticado(String uuid) {

        SessaoWeb sessao = obterSessaoAutenticada(uuid);

        // SESSÃO ANÔNIMA NÃO POSSUI USUÁRIO ASSOCIADO
        if (sessao.getCdUsuario() == null) {
            throw new IllegalStateException(
                    "Esta operação exige autenticação."
            );
        }

        // RECUPERAR USUÁRIO ASSOCIADO À SESSÃO
        Usuario usuario = usuarioDAO.buscarPorId(
                sessao.getCdUsuario()
        ).orElseThrow(() ->
                new IllegalStateException(
                        "Usuário associado à sessão não encontrado."
                )
        );

        // IMPEDIR ACESSO DE CONTAS BLOQUEADAS
        validarUsuarioAtivo(usuario);

        return usuario;
    }

    // ==================================================
    // VALIDAR SESSÃO ATIVA
    // ==================================================

    private SessaoWeb obterSessaoAutenticada(String uuid) {

        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException(
                    "UUID da sessão não informado."
            );
        }

        return sessaoWebService.buscarSessaoAtiva(uuid)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Sessão inválida ou expirada."
                        )
                );
    }

    // ==================================================
    // VALIDAÇÕES DE CADASTRO
    // ==================================================

    private void validarDadosCadastro(UsuarioRequestDTO dto) {

        validarNome(dto.nmUsuario());
        validarEmail(dto.dsEmail());
        validarTelefone(dto.nrTelefone());

        if (dto.dsSenha() == null || dto.dsSenha().isBlank()) {
            throw new IllegalArgumentException(
                    "Senha é obrigatória."
            );
        }

        if (dto.dsSenha().length() < TAMANHO_MIN_SENHA) {
            throw new IllegalArgumentException(
                    "A senha deve ter pelo menos 8 caracteres."
            );
        }
    }

    // ==================================================
    // VALIDAÇÕES DE ATUALIZAÇÃO
    // ==================================================

    private void validarDadosAtualizacao(UsuarioUpdateDTO dto) {

        validarNome(dto.nmUsuario());
        validarEmail(dto.dsEmail());
        validarTelefone(dto.nrTelefone());
    }

    // ==================================================
    // VALIDAÇÃO DE NOME
    // ==================================================

    private void validarNome(String nome) {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException(
                    "Nome é obrigatório."
            );
        }

        if (nome.trim().length() > TAMANHO_MAX_NOME) {
            throw new IllegalArgumentException(
                    "Nome deve ter no máximo 120 caracteres."
            );
        }
    }

    // ==================================================
    // VALIDAÇÃO DE E-MAIL
    // ==================================================

    private void validarEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "E-mail é obrigatório."
            );
        }

        String emailNormalizado = normalizarEmail(email);

        if (emailNormalizado.length() > TAMANHO_MAX_EMAIL) {
            throw new IllegalArgumentException(
                    "E-mail deve ter no máximo 150 caracteres."
            );
        }

        if (!emailNormalizado.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            throw new IllegalArgumentException(
                    "E-mail inválido."
            );
        }
    }

    // ==================================================
    // VALIDAÇÃO DE TELEFONE
    // ==================================================

    private void validarTelefone(String telefone) {

        if (telefone != null
                && telefone.trim().length() > TAMANHO_MAX_TELEFONE) {

            throw new IllegalArgumentException(
                    "Telefone deve ter no máximo 20 caracteres."
            );
        }
    }

    // ==================================================
    // VALIDAR STATUS DO USUÁRIO
    // ==================================================

    private void validarUsuarioAtivo(Usuario usuario) {

        if (!STATUS_ATIVO.equals(usuario.getStUsuario())) {
            throw new IllegalStateException(
                    "A conta está bloqueada."
            );
        }
    }

    // ==================================================
    // CONVERTER MODEL PARA DTO
    // ==================================================

    private UsuarioResponseDTO converterParaResponseDTO(
            Usuario usuario) {

        return new UsuarioResponseDTO(
                usuario.getCdUsuario(),
                usuario.getNmUsuario(),
                usuario.getNrTelefone(),
                usuario.getDsEmail(),
                usuario.getStUsuario(),
                usuario.getDtCadastro()
        );
    }

    // ==================================================
    // NORMALIZAÇÃO
    // ==================================================

    private String normalizarEmail(String email) {

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarTelefone(String telefone) {

        if (telefone == null || telefone.isBlank()) {
            return null;
        }

        return telefone.trim();
    }
}
