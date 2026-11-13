
package br.com.ari.dao;

import br.com.ari.model.SessaoWeb;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class SessaoWebDAO implements CrudDAO<SessaoWeb, Long> {

    private final JdbcTemplate jdbcTemplate;

    public SessaoWebDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // MAPEAMENTO DO RESULTADO DO BANCO PARA MODEL
    private final RowMapper<SessaoWeb> mapper =
            (rs, rowNum) -> mapearSessao(rs);

    // MAPEAMENTO DOS ATRIBUTOS DA SESSÃO
    private static SessaoWeb mapearSessao(ResultSet rs)
            throws SQLException {

        Long cdUsuario = rs.getObject("CD_USUARIO", Long.class);

        return SessaoWeb.builder()
                .cdSessaoWeb(rs.getLong("CD_SESSAO_WEB"))
                .cdUsuario(cdUsuario)
                .dsUuid(rs.getString("DS_UUID"))
                .dtCriacao(timestampParaLocalDateTime(
                        rs.getTimestamp("DT_CRIACAO")))
                .dtExpiracao(timestampParaLocalDateTime(
                        rs.getTimestamp("DT_EXPIRACAO")))
                .stSessao(rs.getString("ST_SESSAO"))
                .build();
    }

    // CONVERSÃO DE TIMESTAMP PARA LOCALDATETIME
    private static LocalDateTime timestampParaLocalDateTime(
            Timestamp timestamp) {

        return timestamp != null
                ? timestamp.toLocalDateTime()
                : null;
    }

    // CONVERSÃO DE LOCALDATETIME PARA TIMESTAMP
    private static Timestamp localDateTimeParaTimestamp(
            LocalDateTime data) {

        return data != null
                ? Timestamp.valueOf(data)
                : null;
    }

    // COLUNAS UTILIZADAS NAS CONSULTAS
    private static final String COLUNAS = """
            CD_SESSAO_WEB,
            CD_USUARIO,
            DS_UUID,
            DT_CRIACAO,
            DT_EXPIRACAO,
            ST_SESSAO
            """;

    // CRIAR SESSÃO
    @Override
    public int criar(SessaoWeb sessao) {

        String sql = """
                INSERT INTO T_ARI_SESSAO_WEB (
                    CD_USUARIO,
                    DS_UUID,
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

            if (sessao.getCdUsuario() != null) {
                ps.setLong(1, sessao.getCdUsuario());
            } else {
                ps.setNull(1, java.sql.Types.NUMERIC);
            }
            ps.setString(2, sessao.getDsUuid());

            ps.setTimestamp(
                    3,
                    localDateTimeParaTimestamp(sessao.getDtExpiracao())
            );

            ps.setString(4, sessao.getStSessao());

            return ps;

        }, keyHolder);

        // RECUPERAR ID GERADO PELO ORACLE
        Number chaveGerada = keyHolder.getKey();

        if (chaveGerada != null) {
            sessao.setCdSessaoWeb(chaveGerada.longValue());
        }

        return linhasAfetadas;
    }

    // BUSCAR SESSÃO POR ID
    @Override
    public Optional<SessaoWeb> buscarPorId(Long id) {

        String sql = """
                SELECT %s
                FROM T_ARI_SESSAO_WEB
                WHERE CD_SESSAO_WEB = ?
                """.formatted(COLUNAS);

        return jdbcTemplate.query(sql, mapper, id)
                .stream()
                .findFirst();
    }

    // BUSCAR SESSÃO PELO UUID
    public Optional<SessaoWeb> buscarPorUuid(String uuid) {

        String sql = """
                SELECT %s
                FROM T_ARI_SESSAO_WEB
                WHERE DS_UUID = ?
                """.formatted(COLUNAS);

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
                localDateTimeParaTimestamp(sessao.getDtExpiracao()),
                sessao.getStSessao(),
                sessao.getCdSessaoWeb()
        );
    }

    // ATUALIZAR STATUS DA SESSÃO
    // Só permite alterar sessões que ainda estejam ATIVAS.
    public int atualizarStatus(Long id, String status) {

        String sql = """
                UPDATE T_ARI_SESSAO_WEB
                SET ST_SESSAO = ?
                WHERE CD_SESSAO_WEB = ?
                  AND ST_SESSAO = 'ATIVA'
                """;

        return jdbcTemplate.update(sql, status, id);
    }

    // REVOGAR TODAS AS SESSÕES ATIVAS DE UM USUÁRIO
    // Preserva os registros no banco.
    public int revogarSessoesAtivasUsuario(Long cdUsuario) {

        String sql = """
            UPDATE T_ARI_SESSAO_WEB
            SET ST_SESSAO = 'REVOGADA'
            WHERE CD_USUARIO = ?
              AND ST_SESSAO = 'ATIVA'
            """;

        return jdbcTemplate.update(sql, cdUsuario);
    }


    @Override
    public int excluir(Long id) {

        return atualizarStatus(id, "REVOGADA");
    }

}
