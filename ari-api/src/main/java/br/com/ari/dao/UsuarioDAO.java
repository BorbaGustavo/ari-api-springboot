
package br.com.ari.dao;

import br.com.ari.model.Usuario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class UsuarioDAO implements CrudDAO<Usuario, Long> {

    private final JdbcTemplate jdbcTemplate;

    public UsuarioDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ==========================================
    // MAPEAMENTO DO RESULTADO DO BANCO PARA MODEL
    // ==========================================

    // Recupera os dados do usuário sem o hash da senha.
    private final RowMapper<Usuario> mapperSemSenha =
            (rs, rowNum) -> mapearUsuario(rs, false);

    // Recupera os dados do usuário incluindo o hash.
    // Utilizado exclusivamente na autenticação.
    private final RowMapper<Usuario> mapperComSenha =
            (rs, rowNum) -> mapearUsuario(rs, true);

    // Converte os atributos retornados pelo banco para o Model.
    private static Usuario mapearUsuario(
            ResultSet rs,
            boolean incluirSenha
    ) throws SQLException {

        Timestamp dtCadastro = rs.getTimestamp("DT_CADASTRO");

        Usuario.UsuarioBuilder builder = Usuario.builder()
                .cdUsuario(rs.getLong("CD_USUARIO"))
                .nmUsuario(rs.getString("NM_USUARIO"))
                .nrTelefone(rs.getString("NR_TELEFONE"))
                .dsEmail(rs.getString("DS_EMAIL"))
                .stUsuario(rs.getString("ST_USUARIO"))
                .dtCadastro(
                        dtCadastro != null
                                ? dtCadastro.toLocalDateTime()
                                : null
                );

        if (incluirSenha) {
            builder.dsSenhaHash(rs.getString("DS_SENHA_HASH"));
        }

        return builder.build();
    }

    // ==========================================
    // CADASTRAR USUÁRIO
    // ==========================================

    @Override
    public int criar(Usuario usuario) {

        String sql = """
                INSERT INTO T_ARI_USUARIO (
                    NM_USUARIO,
                    NR_TELEFONE,
                    DS_EMAIL,
                    DS_SENHA_HASH
                ) VALUES (?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                usuario.getNmUsuario(),
                usuario.getNrTelefone(),
                usuario.getDsEmail(),
                usuario.getDsSenhaHash()
        );
    }

    // ==========================================
    // BUSCAR USUÁRIO POR ID
    // ==========================================

    @Override
    public Optional<Usuario> buscarPorId(Long cdUsuario) {

        String sql = """
                SELECT
                    CD_USUARIO,
                    NM_USUARIO,
                    NR_TELEFONE,
                    DS_EMAIL,
                    ST_USUARIO,
                    DT_CADASTRO
                FROM T_ARI_USUARIO
                WHERE CD_USUARIO = ?
                """;

        return jdbcTemplate.query(sql, mapperSemSenha, cdUsuario)
                .stream()
                .findFirst();
    }

    // ==========================================
    // BUSCAR USUÁRIO POR E-MAIL
    // ==========================================

    // Recupera o hash para validação da senha no Service.
    public Optional<Usuario> buscarPorEmail(String email) {

        String sql = """
                SELECT
                    CD_USUARIO,
                    NM_USUARIO,
                    NR_TELEFONE,
                    DS_EMAIL,
                    DS_SENHA_HASH,
                    ST_USUARIO,
                    DT_CADASTRO
                FROM T_ARI_USUARIO
                WHERE LOWER(DS_EMAIL) = LOWER(?)
                """;

        return jdbcTemplate.query(sql, mapperComSenha, email)
                .stream()
                .findFirst();
    }

    // ==========================================
    // ATUALIZAR DADOS CADASTRAIS
    // ==========================================

    @Override
    public int atualizar(Usuario usuario) {

        String sql = """
                UPDATE T_ARI_USUARIO
                SET
                    NM_USUARIO = ?,
                    NR_TELEFONE = ?,
                    DS_EMAIL = ?
                WHERE CD_USUARIO = ?
                """;

        return jdbcTemplate.update(
                sql,
                usuario.getNmUsuario(),
                usuario.getNrTelefone(),
                usuario.getDsEmail(),
                usuario.getCdUsuario()
        );
    }

    // ==========================================
    // ATUALIZAR SENHA
    // ==========================================

    // Recebe somente o hash gerado pelo BCrypt no Service.
    public int atualizarSenha(Long cdUsuario, String dsSenhaHash) {

        String sql = """
                UPDATE T_ARI_USUARIO
                SET DS_SENHA_HASH = ?
                WHERE CD_USUARIO = ?
                """;

        return jdbcTemplate.update(
                sql,
                dsSenhaHash,
                cdUsuario
        );
    }

    // ==========================================
    // BLOQUEAR USUÁRIO
    // ==========================================

    // Realiza exclusão lógica, preservando o histórico.
    @Override
    public int excluir(Long cdUsuario) {

        String sql = """
                UPDATE T_ARI_USUARIO
                SET ST_USUARIO = 'BLOQUEADO'
                WHERE CD_USUARIO = ?
                """;

        return jdbcTemplate.update(sql, cdUsuario);
    }
}
