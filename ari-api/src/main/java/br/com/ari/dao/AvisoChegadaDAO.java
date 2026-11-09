
package br.com.ari.dao;

import br.com.ari.model.AvisoChegada;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class AvisoChegadaDAO implements CrudDAO<AvisoChegada, Long> {

    private final JdbcTemplate jdbcTemplate;

    public AvisoChegadaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // MAPEAMENTO DO RESULTADO DO BANCO PARA MODEL
    private final RowMapper<AvisoChegada> mapper = (rs, rowNum) ->
            AvisoChegada.builder()
                    .cdAvisoChegada(rs.getLong("CD_AVISO_CHEGADA"))
                    .cdSessaoWeb(rs.getLong("CD_SESSAO_WEB"))
                    .cdEstabelecimento(rs.getLong("CD_ESTABELECIMENTO"))
                    .cdFuncionario(rs.getObject("CD_FUNCIONARIO", Long.class))
                    .dtAviso(rs.getTimestamp("DT_AVISO").toLocalDateTime())
                    .stAviso(rs.getString("ST_AVISO"))
                    .nrDistanciaMetros(rs.getBigDecimal("NR_DISTANCIA_METROS"))
                    .nrTempoEstimadoMin(rs.getObject("NR_TEMPO_ESTIMADO_MIN", Long.class))
                    .dtAcolhimento(
                            rs.getTimestamp("DT_ACOLHIMENTO") != null
                                    ? rs.getTimestamp("DT_ACOLHIMENTO").toLocalDateTime()
                                    : null
                    )
                    .build();

    // COLUNAS UTILIZADAS NAS CONSULTAS
    private static final String COLUNAS = """
            CD_AVISO_CHEGADA,
            CD_SESSAO_WEB,
            CD_ESTABELECIMENTO,
            CD_FUNCIONARIO,
            DT_AVISO,
            ST_AVISO,
            NR_DISTANCIA_METROS,
            NR_TEMPO_ESTIMADO_MIN,
            DT_ACOLHIMENTO
            """;

    // CADASTRAR AVISO
    @Override
    public int criar(AvisoChegada aviso) {

        String sql = """
                INSERT INTO T_ARI_AVISO_CHEGADA (
                    CD_SESSAO_WEB,
                    CD_ESTABELECIMENTO,
                    NR_DISTANCIA_METROS,
                    NR_TEMPO_ESTIMADO_MIN
                ) VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int linhasAfetadas = jdbcTemplate.update(connection -> {

            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    new String[]{"CD_AVISO_CHEGADA"}
            );

            ps.setObject(1, aviso.getCdSessaoWeb());
            ps.setObject(2, aviso.getCdEstabelecimento());
            ps.setBigDecimal(3, aviso.getNrDistanciaMetros());
            ps.setObject(4, aviso.getNrTempoEstimadoMin());

            return ps;

        }, keyHolder);

        if (keyHolder.getKey() != null) {
            aviso.setCdAvisoChegada(keyHolder.getKey().longValue());
        }

        return linhasAfetadas;
    }

    // BUSCAR AVISO POR ID
    @Override
    public Optional<AvisoChegada> buscarPorId(Long id) {

        String sql = "SELECT " + COLUNAS + """
                FROM T_ARI_AVISO_CHEGADA
                WHERE CD_AVISO_CHEGADA = ?
                """;

        return jdbcTemplate.query(sql, mapper, id)
                .stream()
                .findFirst();
    }

    // LISTAR TODOS OS AVISOS
    public List<AvisoChegada> buscarTodos() {

        String sql = "SELECT " + COLUNAS + """
                FROM T_ARI_AVISO_CHEGADA
                ORDER BY DT_AVISO DESC
                """;

        return jdbcTemplate.query(sql, mapper);
    }

    // ATUALIZAR AVISO
    @Override
    public int atualizar(AvisoChegada aviso) {

        String sql = """
                UPDATE T_ARI_AVISO_CHEGADA
                SET
                    CD_FUNCIONARIO = ?,
                    ST_AVISO = ?,
                    DT_ACOLHIMENTO = ?
                WHERE CD_AVISO_CHEGADA = ?
                """;

        return jdbcTemplate.update(
                sql,
                aviso.getCdFuncionario(),
                aviso.getStAviso(),
                aviso.getDtAcolhimento(),
                aviso.getCdAvisoChegada()
        );
    }

    // ATUALIZAR STATUS DO AVISO
    public int atualizarStatus(Long id, String status) {

        String sql = """
                UPDATE T_ARI_AVISO_CHEGADA
                SET
                    ST_AVISO = ?,
                    DT_ACOLHIMENTO = CASE
                        WHEN ? = 'ACOLHIDA' THEN SYSTIMESTAMP
                        ELSE DT_ACOLHIMENTO
                    END
                WHERE CD_AVISO_CHEGADA = ?
                AND ST_AVISO = 'A_CAMINHO'
                """;

        return jdbcTemplate.update(sql, status, status, id);
    }

    // CANCELAR AVISO
    public int cancelar(Long id) {

        String sql = """
                UPDATE T_ARI_AVISO_CHEGADA
                SET ST_AVISO = 'CANCELADO'
                WHERE CD_AVISO_CHEGADA = ?
                AND ST_AVISO = 'A_CAMINHO'
                """;

        return jdbcTemplate.update(sql, id);
    }

    // EXCLUIR LOGICAMENTE / CANCELAR AVISO
    @Override
    public int excluir(Long id) {
        return cancelar(id);
    }
}
