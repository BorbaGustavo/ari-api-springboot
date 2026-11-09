
package br.com.ari.dao;

import br.com.ari.model.SessaoWeb;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Optional;

@Repository
public class SessaoWebDAO implements CrudDAO<SessaoWeb, Long> {

    private final JdbcTemplate jdbcTemplate;

    public SessaoWebDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // MAPEAMENTO DO RESULTADO DO BANCO PARA MODEL
    private final RowMapper<SessaoWeb> mapper = (rs, rowNum) ->
            SessaoWeb.builder()
                    .cdSessaoWeb(rs.getLong("CD_SESSAO_WEB"))
                    .dsUuid(rs.getString("DS_UUID"))
                    .cdUsuario(rs.getObject("CD_USUARIO", Long.class))
                    .dtCriacao(rs.getTimestamp("DT_CRIACAO").toLocalDateTime())
                    .dtExpiracao(rs.getTimestamp("DT_EXPIRACAO").toLocalDateTime())
                    .stSessao(rs.getString("ST_SESSAO"))
                    .build();

    // COLUNAS UTILIZADAS NAS CONSULTAS
    private static final String COLUNAS = """
            CD_SESSAO_WEB,
            DS_UUID,
            CD_USUARIO,
            DT_CRIACAO,
            DT_EXPIRACAO,
            ST_SESSAO
            """;

    // CRIAR SESSÃO
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

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int linhasAfetadas = jdbcTemplate.update(connection -> {

            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    new String[]{"CD_SESSAO_WEB"}
            );

            ps.setString(1, sessao.getDsUuid());
            ps.setObject(2, sessao.getCdUsuario());
            ps.setObject(3, sessao.getDtExpiracao());
            ps.setString(4, sessao.getStSessao());

            return ps;

        }, keyHolder);

        if (keyHolder.getKey() != null) {
            sessao.setCdSessaoWeb(keyHolder.getKey().longValue());
        }

        return linhasAfetadas;
    }

    // BUSCAR SESSÃO POR ID
    @Override
    public Optional<SessaoWeb> buscarPorId(Long id) {

        String sql = "SELECT " + COLUNAS + """
                FROM T_ARI_SESSAO_WEB
                WHERE CD_SESSAO_WEB = ?
                """;

        return jdbcTemplate.query(sql, mapper, id)
                .stream()
                .findFirst();
    }

    // BUSCAR SESSÃO PELO UUID
    public Optional<SessaoWeb> buscarPorUuid(String uuid) {

        String sql = "SELECT " + COLUNAS + """
                FROM T_ARI_SESSAO_WEB
                WHERE DS_UUID = ?
                """;

        return jdbcTemplate.query(sql, mapper, uuid)
                .stream()
                .findFirst();
    }

    // ATUALIZAR SESSÃO
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

    // ATUALIZAR STATUS
    public int atualizarStatus(Long id, String status) {

        String sql = """
                UPDATE T_ARI_SESSAO_WEB
                SET ST_SESSAO = ?
                WHERE CD_SESSAO_WEB = ?
                AND ST_SESSAO = 'ATIVA'
                """;

        return jdbcTemplate.update(sql, status, id);
    }

    // REVOGAR SESSÃO
    @Override
    public int excluir(Long id) {
        return atualizarStatus(id, "REVOGADA");
    }
}
