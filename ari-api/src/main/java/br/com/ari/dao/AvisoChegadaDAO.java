package br.com.ari.dao;

import br.com.ari.model.AvisoChegada;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AvisoChegadaDAO implements CrudDAO<AvisoChegada, Long> {

    private final JdbcTemplate jdbcTemplate;

    public AvisoChegadaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public int criar(AvisoChegada aviso) {

        String sql = """
            INSERT INTO T_ARI_AVISO_CHEGADA (
                CD_SESSAO_WEB,
                CD_ESTABELECIMENTO,
                CD_FUNCIONARIO,
                ST_AVISO,
                NR_DISTANCIA_METROS,
                NR_TEMPO_ESTIMADO_MIN
            ) VALUES (?, ?, ?, ?, ?, ?)
            """;

        return jdbcTemplate.update(
                sql,
                aviso.getCdSessaoWeb(),
                aviso.getCdEstabelecimento(),
                aviso.getCdFuncionario(),
                aviso.getStAviso(),
                aviso.getNrDistanciaMetros(),
                aviso.getNrTempoEstimadoMin()
        );
    }


    @Override
    public Optional<AvisoChegada> buscarPorId(Long id) {

        String sql = """
            SELECT *
            FROM T_ARI_AVISO_CHEGADA
            WHERE CD_AVISO_CHEGADA = ?
            """;

        RowMapper<AvisoChegada> mapper = (rs, rowNum) ->
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

        return jdbcTemplate.query(sql, mapper, id)
                .stream()
                .findFirst();
    }


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


    @Override
    public int excluir(Long id) {

        String sql = """
            DELETE FROM T_ARI_AVISO_CHEGADA
            WHERE CD_AVISO_CHEGADA = ?
            """;

        return jdbcTemplate.update(sql, id);
    }
}
