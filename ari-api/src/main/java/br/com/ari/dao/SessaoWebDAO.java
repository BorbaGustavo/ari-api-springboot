package br.com.ari.dao;

import br.com.ari.model.SessaoWeb;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SessaoWebDAO implements CrudDAO<SessaoWeb, Long> {
    private final JdbcTemplate jdbcTemplate;

    public SessaoWebDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int criar(SessaoWeb sessao) {

        String sql = """
            INSERT INTO T_ARI_SESSAO_WEB (
                DS_UUID,
                CD_USUARIO,
                DT_EXPIRACAO,
                ST_SESSAO
            ) VALUES (?, ?, ?, ?)
            """;

        return jdbcTemplate.update(
                sql,
                sessao.getDsUuid(),
                sessao.getCdUsuario(),
                sessao.getDtExpiracao(),
                sessao.getStSessao()
        );
    }


    @Override
    public Optional<SessaoWeb> buscarPorId(Long id) {

        String sql = """
            SELECT *
            FROM T_ARI_SESSAO_WEB
            WHERE CD_SESSAO_WEB = ?
            """;

        RowMapper<SessaoWeb> mapper = (rs, rowNum) ->
                SessaoWeb.builder()
                        .cdSessaoWeb(rs.getLong("CD_SESSAO_WEB"))
                        .dsUuid(rs.getString("DS_UUID"))
                        .cdUsuario(rs.getObject("CD_USUARIO", Long.class))
                        .dtCriacao(rs.getTimestamp("DT_CRIACAO").toLocalDateTime())
                        .dtExpiracao(rs.getTimestamp("DT_EXPIRACAO").toLocalDateTime())
                        .stSessao(rs.getString("ST_SESSAO"))
                        .build();

        return jdbcTemplate.query(sql, mapper, id)
                .stream()
                .findFirst();
    }


    @Override
    public int atualizar(SessaoWeb sessao) {

        String sql = """
            UPDATE T_ARI_SESSAO_WEB
            SET
                DT_EXPIRACAO = ?,
                ST_SESSAO = ?
            WHERE CD_SESSAO_WEB = ?
            """;

        return jdbcTemplate.update(
                sql,
                sessao.getDtExpiracao(),
                sessao.getStSessao(),
                sessao.getCdSessaoWeb()
        );
    }


    @Override
    public int excluir(Long id) {

        String sql = """
            DELETE FROM T_ARI_SESSAO_WEB
            WHERE CD_SESSAO_WEB = ?
            """;

        return jdbcTemplate.update(sql, id);
    }
}
