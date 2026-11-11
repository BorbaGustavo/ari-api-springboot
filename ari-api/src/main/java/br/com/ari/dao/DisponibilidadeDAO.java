
package br.com.ari.dao;

import br.com.ari.model.Disponibilidade;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class DisponibilidadeDAO
        implements CrudDAO<Disponibilidade, Long> {

    private final JdbcTemplate jdbcTemplate;

    public DisponibilidadeDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // MAPEAMENTO DO RESULTADO DO BANCO PARA MODEL
    private final RowMapper<Disponibilidade> mapper =
            (rs, rowNum) -> mapearDisponibilidade(rs);

    // MAPEAMENTO DOS ATRIBUTOS DA DISPONIBILIDADE
    private static Disponibilidade mapearDisponibilidade(
            ResultSet rs) throws SQLException {

        Timestamp dtCadastro = rs.getTimestamp("DT_CADASTRO");

        return Disponibilidade.builder()
                .cdDisponibilidade(rs.getLong("CD_DISPONIBILIDADE"))
                .cdEstabelecimento(rs.getLong("CD_ESTABELECIMENTO"))
                .nrDiaSemana(rs.getInt("NR_DIA_SEMANA"))
                .nrPeriodo(rs.getInt("NR_PERIODO"))
                .hrInicio(rs.getString("HR_INICIO"))
                .hrFim(rs.getString("HR_FIM"))
                .flAtivo(rs.getString("FL_ATIVO"))
                .dtCadastro(
                        dtCadastro != null
                                ? dtCadastro.toLocalDateTime()
                                : null
                )
                .build();
    }

    // COLUNAS UTILIZADAS NAS CONSULTAS
    private static final String COLUNAS = """
            CD_DISPONIBILIDADE,
            CD_ESTABELECIMENTO,
            NR_DIA_SEMANA,
            NR_PERIODO,
            HR_INICIO,
            HR_FIM,
            FL_ATIVO,
            DT_CADASTRO
            """;

    // CADASTRAR DISPONIBILIDADE
    @Override
    public int criar(Disponibilidade disponibilidade) {

        String sql = """
                INSERT INTO T_ARI_DISPONIBILIDADE (
                    CD_ESTABELECIMENTO,
                    NR_DIA_SEMANA,
                    NR_PERIODO,
                    HR_INICIO,
                    HR_FIM,
                    FL_ATIVO
                ) VALUES (?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int linhasAfetadas = jdbcTemplate.update(connection -> {

            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    new String[]{"CD_DISPONIBILIDADE"}
            );

            ps.setLong(
                    1,
                    disponibilidade.getCdEstabelecimento()
            );

            ps.setInt(
                    2,
                    disponibilidade.getNrDiaSemana()
            );

            ps.setInt(
                    3,
                    disponibilidade.getNrPeriodo()
            );

            ps.setString(
                    4,
                    disponibilidade.getHrInicio()
            );

            ps.setString(
                    5,
                    disponibilidade.getHrFim()
            );

            // DEFAULT S, caso não seja informado no cadastro.
            ps.setString(
                    6,
                    disponibilidade.getFlAtivo() != null
                            ? disponibilidade.getFlAtivo()
                            : "S"
            );

            return ps;

        }, keyHolder);

        // RECUPERAR ID GERADO PELO ORACLE
        Number chaveGerada = keyHolder.getKey();

        if (chaveGerada != null) {
            disponibilidade.setCdDisponibilidade(
                    chaveGerada.longValue()
            );
        }

        return linhasAfetadas;
    }

    // BUSCAR DISPONIBILIDADE POR ID
    @Override
    public Optional<Disponibilidade> buscarPorId(Long id) {

        String sql = """
                SELECT %s
                FROM T_ARI_DISPONIBILIDADE
                WHERE CD_DISPONIBILIDADE = ?
                """.formatted(COLUNAS);

        return jdbcTemplate.query(sql, mapper, id)
                .stream()
                .findFirst();
    }

    // BUSCAR TODAS AS DISPONIBILIDADES DE UM ESTABELECIMENTO
    // Inclui períodos ativos e inativos.
    public List<Disponibilidade> buscarPorEstabelecimento(
            Long cdEstabelecimento) {

        String sql = """
                SELECT %s
                FROM T_ARI_DISPONIBILIDADE
                WHERE CD_ESTABELECIMENTO = ?
                ORDER BY NR_DIA_SEMANA, NR_PERIODO
                """.formatted(COLUNAS);

        return jdbcTemplate.query(
                sql,
                mapper,
                cdEstabelecimento
        );
    }

    // BUSCAR SOMENTE PERÍODOS ATIVOS
    public List<Disponibilidade> buscarAtivasPorEstabelecimento(
            Long cdEstabelecimento) {

        String sql = """
                SELECT %s
                FROM T_ARI_DISPONIBILIDADE
                WHERE CD_ESTABELECIMENTO = ?
                  AND FL_ATIVO = 'S'
                ORDER BY NR_DIA_SEMANA, NR_PERIODO
                """.formatted(COLUNAS);

        return jdbcTemplate.query(
                sql,
                mapper,
                cdEstabelecimento
        );
    }

    // BUSCAR DISPONIBILIDADES POR DIA DA SEMANA
    public List<Disponibilidade> buscarPorDiaSemana(
            Long cdEstabelecimento,
            Integer nrDiaSemana) {

        String sql = """
                SELECT %s
                FROM T_ARI_DISPONIBILIDADE
                WHERE CD_ESTABELECIMENTO = ?
                  AND NR_DIA_SEMANA = ?
                  AND FL_ATIVO = 'S'
                ORDER BY NR_PERIODO
                """.formatted(COLUNAS);

        return jdbcTemplate.query(
                sql,
                mapper,
                cdEstabelecimento,
                nrDiaSemana
        );
    }

    // ATUALIZAR DISPONIBILIDADE
    @Override
    public int atualizar(Disponibilidade disponibilidade) {

        String sql = """
                UPDATE T_ARI_DISPONIBILIDADE
                SET
                    NR_DIA_SEMANA = ?,
                    NR_PERIODO = ?,
                    HR_INICIO = ?,
                    HR_FIM = ?,
                    FL_ATIVO = COALESCE(?, FL_ATIVO)
                WHERE CD_DISPONIBILIDADE = ?
                  AND CD_ESTABELECIMENTO = ?
                """;

        return jdbcTemplate.update(
                sql,
                disponibilidade.getNrDiaSemana(),
                disponibilidade.getNrPeriodo(),
                disponibilidade.getHrInicio(),
                disponibilidade.getHrFim(),
                disponibilidade.getFlAtivo(),
                disponibilidade.getCdDisponibilidade(),
                disponibilidade.getCdEstabelecimento()
        );
    }


    // DESATIVAR PERÍODO DE DISPONIBILIDADE
    // Preserva o registro e altera FL_ATIVO para N.
    public int desativar(Long id) {

        String sql = """
            UPDATE T_ARI_DISPONIBILIDADE
            SET FL_ATIVO = 'N'
            WHERE CD_DISPONIBILIDADE = ?
            """;

        return jdbcTemplate.update(sql, id);
    }

    @Override
    public int excluir(Long id) {
        String sql = """
            UPDATE T_ARI_DISPONIBILIDADE
            SET FL_ATIVO = 'N'
            WHERE CD_DISPONIBILIDADE = ?
            """;

        return jdbcTemplate.update(sql, id);
    }

}
