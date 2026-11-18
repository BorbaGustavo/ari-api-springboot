
package br.com.ari.dao;

import br.com.ari.model.Funcionario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class FuncionarioDAO implements CrudDAO<Funcionario, Long> {

    private final JdbcTemplate jdbcTemplate;

    public FuncionarioDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =====================================================
    // MAPEAMENTO DO RESULTADO DO BANCO PARA MODEL
    // =====================================================

    // Recupera o hash somente quando necessário para autenticação.
    private final RowMapper<Funcionario> mapperComSenha =
            (rs, rowNum) -> mapearFuncionario(rs, true);

    // Não recupera o hash nas consultas comuns.
    private final RowMapper<Funcionario> mapperSemSenha =
            (rs, rowNum) -> mapearFuncionario(rs, false);

    private static Funcionario mapearFuncionario(
            ResultSet rs,
            boolean incluirSenha
    ) throws SQLException {

        Timestamp dtCadastro = rs.getTimestamp("DT_CADASTRO");

        Funcionario.FuncionarioBuilder builder = Funcionario.builder()
                .cdFuncionario(rs.getLong("CD_FUNCIONARIO"))
                .cdEstabelecimento(rs.getLong("CD_ESTABELECIMENTO"))
                .nmFuncionario(rs.getString("NM_FUNCIONARIO"))
                .nrTelefone(rs.getString("NR_TELEFONE"))
                .dsEmailLogin(rs.getString("DS_EMAIL_LOGIN"))
                .tpPerfil(rs.getString("TP_PERFIL"))
                .flTreinado(rs.getString("FL_TREINADO"))
                .stFuncionario(rs.getString("ST_FUNCIONARIO"))
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

    // =====================================================
    // COLUNAS UTILIZADAS NAS CONSULTAS
    // Não inclui DS_SENHA_HASH.
    // =====================================================

    private static final String COLUNAS = """
            CD_FUNCIONARIO,
            CD_ESTABELECIMENTO,
            NM_FUNCIONARIO,
            NR_TELEFONE,
            DS_EMAIL_LOGIN,
            TP_PERFIL,
            FL_TREINADO,
            ST_FUNCIONARIO,
            DT_CADASTRO
            """;

    // =====================================================
    // CADASTRAR FUNCIONÁRIO
    // =====================================================

    @Override
    public int criar(Funcionario funcionario) {

        String sql = """
                INSERT INTO T_ARI_FUNCIONARIO (
                    CD_ESTABELECIMENTO,
                    NM_FUNCIONARIO,
                    NR_TELEFONE,
                    DS_EMAIL_LOGIN,
                    DS_SENHA_HASH,
                    TP_PERFIL,
                    FL_TREINADO,
                    ST_FUNCIONARIO
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                funcionario.getCdEstabelecimento(),
                funcionario.getNmFuncionario(),
                funcionario.getNrTelefone(),
                funcionario.getDsEmailLogin(),
                funcionario.getDsSenhaHash(),
                funcionario.getTpPerfil(),
                funcionario.getFlTreinado(),
                funcionario.getStFuncionario()
        );
    }

    // =====================================================
    // BUSCAR FUNCIONÁRIO POR ID
    // =====================================================

    @Override
    public Optional<Funcionario> buscarPorId(Long id) {

        String sql = """
                SELECT %s
                FROM T_ARI_FUNCIONARIO
                WHERE CD_FUNCIONARIO = ?
                """.formatted(COLUNAS);

        return jdbcTemplate.query(sql, mapperSemSenha, id)
                .stream()
                .findFirst();
    }

    // =====================================================
    // BUSCAR FUNCIONÁRIO POR E-MAIL DE LOGIN
    // Recupera o hash para autenticação no Service.
    // =====================================================

    public Optional<Funcionario> buscarPorEmailLogin(String email) {

        String sql = """
                SELECT
                    CD_FUNCIONARIO,
                    CD_ESTABELECIMENTO,
                    NM_FUNCIONARIO,
                    NR_TELEFONE,
                    DS_EMAIL_LOGIN,
                    DS_SENHA_HASH,
                    TP_PERFIL,
                    FL_TREINADO,
                    ST_FUNCIONARIO,
                    DT_CADASTRO
                FROM T_ARI_FUNCIONARIO
                WHERE LOWER(DS_EMAIL_LOGIN) = LOWER(?)
                """;

        return jdbcTemplate.query(sql, mapperComSenha, email)
                .stream()
                .findFirst();
    }

    // =====================================================
    // LISTAR FUNCIONÁRIOS POR ESTABELECIMENTO
    // Inclui ATIVOS e INATIVOS.
    // Não retorna o hash da senha.
    // =====================================================

    public List<Funcionario> buscarPorEstabelecimento(
            Long idEstabelecimento
    ) {

        String sql = """
                SELECT %s
                FROM T_ARI_FUNCIONARIO
                WHERE CD_ESTABELECIMENTO = ?
                ORDER BY NM_FUNCIONARIO
                """.formatted(COLUNAS);

        return jdbcTemplate.query(
                sql,
                mapperSemSenha,
                idEstabelecimento
        );
    }

    // =====================================================
    // ATUALIZAR DADOS DO FUNCIONÁRIO
    // Atualiza nome, telefone e e-mail.
    // O Service controla as permissões de edição.
    // =====================================================

    @Override
    public int atualizar(Funcionario funcionario) {

        String sql = """
                UPDATE T_ARI_FUNCIONARIO
                SET
                    NM_FUNCIONARIO = ?,
                    NR_TELEFONE = ?,
                    DS_EMAIL_LOGIN = ?
                WHERE CD_FUNCIONARIO = ?
                  AND CD_ESTABELECIMENTO = ?
                """;

        return jdbcTemplate.update(
                sql,
                funcionario.getNmFuncionario(),
                funcionario.getNrTelefone(),
                funcionario.getDsEmailLogin(),
                funcionario.getCdFuncionario(),
                funcionario.getCdEstabelecimento()
        );
    }

    // =====================================================
    // ATUALIZAR SENHA DO FUNCIONÁRIO
    // Recebe o hash já criptografado com BCrypt.
    // =====================================================

    public int atualizarSenha(Long id, String dsSenhaHash) {

        String sql = """
                UPDATE T_ARI_FUNCIONARIO
                SET DS_SENHA_HASH = ?
                WHERE CD_FUNCIONARIO = ?
                """;

        return jdbcTemplate.update(
                sql,
                dsSenhaHash,
                id
        );
    }

    // =====================================================
    // ATUALIZAR SITUAÇÃO DE TREINAMENTO
    // Somente funcionários ativos podem ter o treinamento
    // alterado.
    // =====================================================

    public int atualizarTreinamento(
            Long id,
            Long idEstabelecimento,
            String flTreinado
    ) {

        String sql = """
                UPDATE T_ARI_FUNCIONARIO
                SET FL_TREINADO = ?
                WHERE CD_FUNCIONARIO = ?
                  AND CD_ESTABELECIMENTO = ?
                  AND ST_FUNCIONARIO = 'ATIVO'
                """;

        return jdbcTemplate.update(
                sql,
                flTreinado,
                id,
                idEstabelecimento
        );
    }

    // =====================================================
    // ATUALIZAR STATUS DO FUNCIONÁRIO
    // Permite ATIVO ou INATIVO.
    // O Service valida as permissões e o valor recebido.
    // =====================================================

    public int atualizarStatus(
            Long id,
            Long idEstabelecimento,
            String stFuncionario
    ) {

        String sql = """
                UPDATE T_ARI_FUNCIONARIO
                SET ST_FUNCIONARIO = ?
                WHERE CD_FUNCIONARIO = ?
                  AND CD_ESTABELECIMENTO = ?
                """;

        return jdbcTemplate.update(
                sql,
                stFuncionario,
                id,
                idEstabelecimento
        );
    }

    // =====================================================
    // EXCLUIR FUNCIONÁRIO
    // Exclusão física.
    // O banco impede a operação quando existem referências
    // que não permitem excluir o registro.
    // =====================================================

    @Override
    public int excluir(Long id) {

        String sql = """
                DELETE FROM T_ARI_FUNCIONARIO
                WHERE CD_FUNCIONARIO = ?
                """;

        return jdbcTemplate.update(sql, id);
    }
}
