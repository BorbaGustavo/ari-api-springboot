
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

    // MAPEAMENTO DO RESULTADO DO BANCO PARA MODEL
    // A senha só é recuperada quando necessária para autenticação.
    private final RowMapper<Funcionario> mapperComSenha =
            (rs, rowNum) -> mapearFuncionario(rs, true);

    private final RowMapper<Funcionario> mapperSemSenha =
            (rs, rowNum) -> mapearFuncionario(rs, false);

    // MAPEAMENTO DOS ATRIBUTOS DO FUNCIONÁRIO
    private static Funcionario mapearFuncionario(
            ResultSet rs, boolean incluirSenha) throws SQLException {

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

    // COLUNAS UTILIZADAS NAS CONSULTAS
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

    // CADASTRAR FUNCIONÁRIO
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

    // BUSCAR FUNCIONÁRIO POR ID
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

    // BUSCAR FUNCIONÁRIO POR E-MAIL DE LOGIN
    // Recupera o hash da senha para autenticação no Service.
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

    // LISTAR FUNCIONÁRIOS POR ESTABELECIMENTO
    // Inclui funcionários ATIVOS e INATIVOS.
    // Não retorna o hash da senha.
    public List<Funcionario> buscarPorEstabelecimento(
            Long idEstabelecimento) {

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

    // ATUALIZAR DADOS DO FUNCIONÁRIO
    // A autorização ADMIN deve ser validada no Service.
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

    // ATUALIZAR SENHA DO FUNCIONÁRIO
    // Recebe a senha já criptografada com BCrypt.
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

    // ATUALIZAR SITUAÇÃO DE TREINAMENTO DO FUNCIONÁRIO
    public int atualizarTreinamento(
            Long id,
            Long idEstabelecimento,
            String flTreinado) {

        String sql = """
                UPDATE T_ARI_FUNCIONARIO
                SET FL_TREINADO = ?
                WHERE CD_FUNCIONARIO = ?
                  AND CD_ESTABELECIMENTO = ?
                  AND ST_FUNCIONARIO = 'ATIVO'
                """;

        return jdbcTemplate.update(sql, flTreinado, id, idEstabelecimento);
    }

    // INATIVAR FUNCIONÁRIO
    // Preserva o histórico dos avisos associados.
    public int inativar(Long id, Long idEstabelecimento) {

        String sql = """
                UPDATE T_ARI_FUNCIONARIO
                SET ST_FUNCIONARIO = 'INATIVO'
                WHERE CD_FUNCIONARIO = ?
                  AND CD_ESTABELECIMENTO = ?
                """;

        return jdbcTemplate.update(sql, id, idEstabelecimento);
    }

    // EXCLUIR FUNCIONÁRIO
    // A exclusão física só será possível quando não houver
    // referências históricas que impeçam a operação.
    @Override
    public int excluir(Long id) {

        String sql = """
                DELETE FROM T_ARI_FUNCIONARIO
                WHERE CD_FUNCIONARIO = ?
                """;

        return jdbcTemplate.update(sql, id);
    }

}
