package br.com.ari.dao;

import br.com.ari.model.Funcionario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FuncionarioDAO implements  CrudDAO<Funcionario, Long> {
    private final JdbcTemplate jdbcTemplate;

    public FuncionarioDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public int criar(Funcionario funcionario) {

        String sql = """
            INSERT INTO T_ARI_FUNCIONARIO (
                CD_ESTABELECIMENTO,
                NR_TELEFONE,
                NM_FUNCIONARIO,
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
                funcionario.getNrTelefone(),
                funcionario.getNmFuncionario(),
                funcionario.getDsEmailLogin(),
                funcionario.getDsSenhaHash(),
                funcionario.getTpPerfil(),
                funcionario.getFlTreinado(),
                funcionario.getStFuncionario()
        );
    }


    @Override
    public Optional<Funcionario> buscarPorId(Long id) {

        String sql = """
            SELECT *
            FROM T_ARI_FUNCIONARIO
            WHERE CD_FUNCIONARIO = ?
            """;

        RowMapper<Funcionario> mapper = (rs, rowNum) ->
                Funcionario.builder()
                        .cdFuncionario(rs.getLong("CD_FUNCIONARIO"))
                        .cdEstabelecimento(rs.getLong("CD_ESTABELECIMENTO"))
                        .nrTelefone(rs.getString("NR_TELEFONE"))
                        .nmFuncionario(rs.getString("NM_FUNCIONARIO"))
                        .dsEmailLogin(rs.getString("DS_EMAIL_LOGIN"))
                        .dsSenhaHash(rs.getString("DS_SENHA_HASH"))
                        .tpPerfil(rs.getString("TP_PERFIL"))
                        .flTreinado(rs.getString("FL_TREINADO"))
                        .stFuncionario(rs.getString("ST_FUNCIONARIO"))
                        .dtCadastro(rs.getTimestamp("DT_CADASTRO").toLocalDateTime())
                        .build();

        return jdbcTemplate.query(sql, mapper, id)
                .stream()
                .findFirst();
    }

    // UPDATE
    @Override
    public int atualizar(Funcionario funcionario) {

        String sql = """
            UPDATE T_ARI_FUNCIONARIO
            SET
                NR_TELEFONE = ?,
                NM_FUNCIONARIO = ?,
                TP_PERFIL = ?,
                FL_TREINADO = ?,
                ST_FUNCIONARIO = ?
            WHERE CD_FUNCIONARIO = ?
            """;

        return jdbcTemplate.update(
                sql,
                funcionario.getNrTelefone(),
                funcionario.getNmFuncionario(),
                funcionario.getTpPerfil(),
                funcionario.getFlTreinado(),
                funcionario.getStFuncionario(),
                funcionario.getCdFuncionario()
        );
    }


    @Override
    public int excluir(Long id) {

        String sql = """
            DELETE FROM T_ARI_FUNCIONARIO
            WHERE CD_FUNCIONARIO = ?
            """;

        return jdbcTemplate.update(sql, id);
    }

    // BUSCAR FUNCIONÁRIOS POR ESTABELECIMENTO
    public List<Funcionario> buscarPorEstabelecimento(Long idEstabelecimento) {

        String sql = """
            SELECT *
            FROM T_ARI_FUNCIONARIO
            WHERE CD_ESTABELECIMENTO = ?
            ORDER BY NM_FUNCIONARIO
            """;

        RowMapper<Funcionario> mapper = (rs, rowNum) ->
                Funcionario.builder()
                        .cdFuncionario(rs.getLong("CD_FUNCIONARIO"))
                        .cdEstabelecimento(rs.getLong("CD_ESTABELECIMENTO"))
                        .nrTelefone(rs.getString("NR_TELEFONE"))
                        .nmFuncionario(rs.getString("NM_FUNCIONARIO"))
                        .dsEmailLogin(rs.getString("DS_EMAIL_LOGIN"))
                        .dsSenhaHash(rs.getString("DS_SENHA_HASH"))
                        .tpPerfil(rs.getString("TP_PERFIL"))
                        .flTreinado(rs.getString("FL_TREINADO"))
                        .stFuncionario(rs.getString("ST_FUNCIONARIO"))
                        .dtCadastro(rs.getTimestamp("DT_CADASTRO").toLocalDateTime())
                        .build();

        return jdbcTemplate.query(sql, mapper, idEstabelecimento);
    }
}
