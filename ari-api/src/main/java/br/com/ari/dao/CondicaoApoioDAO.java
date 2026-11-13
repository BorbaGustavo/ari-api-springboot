
package br.com.ari.dao;

import br.com.ari.model.CondicaoApoio;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
/**
 * DAO responsável pelo cadastro, consulta, atualização
 * e desativação das condições de apoio dos estabelecimentos.
 */
public class CondicaoApoioDAO
        implements CrudDAO<CondicaoApoio, Long> {

    private final JdbcTemplate jdbcTemplate;

    public CondicaoApoioDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // MAPEAMENTO DO RESULTADO DO BANCO PARA MODEL
    private final RowMapper<CondicaoApoio> mapper =
            (rs, rowNum) -> mapearCondicao(rs);

    // MAPEAMENTO DOS ATRIBUTOS DA CONDIÇÃO DE APOIO
    private static CondicaoApoio mapearCondicao(ResultSet rs)
            throws SQLException {

        Timestamp dtCadastro = rs.getTimestamp("DT_CADASTRO");

        return CondicaoApoio.builder()
                .cdCondicaoApoio(rs.getLong("CD_CONDICAO_APOIO"))
                .cdEstabelecimento(rs.getLong("CD_ESTABELECIMENTO"))
                .tpCategoria(rs.getString("TP_CATEGORIA"))
                .nmCondicao(rs.getString("NM_CONDICAO"))
                .dsCondicao(rs.getString("DS_CONDICAO"))
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
            CD_CONDICAO_APOIO,
            CD_ESTABELECIMENTO,
            TP_CATEGORIA,
            NM_CONDICAO,
            DS_CONDICAO,
            FL_ATIVO,
            DT_CADASTRO
            """;

    // CADASTRAR CONDIÇÃO DE APOIO
    @Override
    public int criar(CondicaoApoio condicao) {

        String sql = """
                INSERT INTO T_ARI_CONDICAO_APOIO (
                    CD_ESTABELECIMENTO,
                    TP_CATEGORIA,
                    NM_CONDICAO,
                    DS_CONDICAO
                ) VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int linhasAfetadas = jdbcTemplate.update(connection -> {

            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    new String[]{"CD_CONDICAO_APOIO"}
            );

            ps.setLong(1, condicao.getCdEstabelecimento());
            ps.setString(2, condicao.getTpCategoria());
            ps.setString(3, condicao.getNmCondicao());

            if (condicao.getDsCondicao() != null) {
                ps.setString(4, condicao.getDsCondicao());
            } else {
                ps.setNull(4, Types.VARCHAR);
            }

            return ps;

        }, keyHolder);

        // RECUPERAR ID GERADO PELO ORACLE
        Number chaveGerada = keyHolder.getKey();

        if (chaveGerada != null) {
            condicao.setCdCondicaoApoio(chaveGerada.longValue());
        }

        return linhasAfetadas;
    }

    // BUSCAR CONDIÇÃO POR ID
    @Override
    public Optional<CondicaoApoio> buscarPorId(Long id) {

        String sql = """
                SELECT %s
                FROM T_ARI_CONDICAO_APOIO
                WHERE CD_CONDICAO_APOIO = ?
                """.formatted(COLUNAS);

        return jdbcTemplate.query(sql, mapper, id)
                .stream()
                .findFirst();
    }

    // BUSCAR TODAS AS CONDIÇÕES DE UM ESTABELECIMENTO
    // Inclui condições ativas e inativas.
    public List<CondicaoApoio> buscarPorEstabelecimento(
            Long cdEstabelecimento) {

        String sql = """
                SELECT %s
                FROM T_ARI_CONDICAO_APOIO
                WHERE CD_ESTABELECIMENTO = ?
                ORDER BY CD_CONDICAO_APOIO
                """.formatted(COLUNAS);

        return jdbcTemplate.query(
                sql,
                mapper,
                cdEstabelecimento
        );
    }

    // BUSCAR SOMENTE CONDIÇÕES ATIVAS
    // Utilizado na apresentação das condições ao público.
    public List<CondicaoApoio> buscarAtivasPorEstabelecimento(
            Long cdEstabelecimento) {

        String sql = """
                SELECT %s
                FROM T_ARI_CONDICAO_APOIO
                WHERE CD_ESTABELECIMENTO = ?
                  AND FL_ATIVO = 'S'
                ORDER BY NM_CONDICAO
                """.formatted(COLUNAS);

        return jdbcTemplate.query(
                sql,
                mapper,
                cdEstabelecimento
        );
    }

    // ATUALIZAR CONDIÇÃO DE APOIO
    // Restringe a alteração à condição pertencente ao estabelecimento.
    @Override
    public int atualizar(CondicaoApoio condicao) {

        String sql = """
                UPDATE T_ARI_CONDICAO_APOIO
                SET
                    TP_CATEGORIA = ?,
                    NM_CONDICAO = ?,
                    DS_CONDICAO = ?
                WHERE CD_CONDICAO_APOIO = ?
                  AND CD_ESTABELECIMENTO = ?
                """;

        return jdbcTemplate.update(
                sql,
                condicao.getTpCategoria(),
                condicao.getNmCondicao(),
                condicao.getDsCondicao(),
                condicao.getCdCondicaoApoio(),
                condicao.getCdEstabelecimento()
        );
    }

    // ATUALIZAR STATUS DA CONDIÇÃO
    // S = ativa | N = inativa.
    public int atualizarStatus(
            Long cdCondicaoApoio,
            Long cdEstabelecimento,
            String status) {

        String sql = """
                UPDATE T_ARI_CONDICAO_APOIO
                SET FL_ATIVO = ?
                WHERE CD_CONDICAO_APOIO = ?
                  AND CD_ESTABELECIMENTO = ?
                """;

        return jdbcTemplate.update(
                sql,
                status,
                cdCondicaoApoio,
                cdEstabelecimento
        );
    }

    // ATUALIZAR TODAS AS SELEÇÕES DO ESTABELECIMENTO
    // IDs selecionados recebem S; os demais recebem N.
    public int atualizarSelecoes(
            Long cdEstabelecimento,
            List<Long> idsSelecionados) {

        if (idsSelecionados == null || idsSelecionados.isEmpty()) {

            String sql = """
                    UPDATE T_ARI_CONDICAO_APOIO
                    SET FL_ATIVO = 'N'
                    WHERE CD_ESTABELECIMENTO = ?
                    """;

            return jdbcTemplate.update(sql, cdEstabelecimento);
        }

        String placeholders = String.join(
                ",",
                java.util.Collections.nCopies(
                        idsSelecionados.size(),
                        "?"
                )
        );

        String sql = """
                UPDATE T_ARI_CONDICAO_APOIO
                SET FL_ATIVO = CASE
                    WHEN CD_CONDICAO_APOIO IN (%s) THEN 'S'
                    ELSE 'N'
                END
                WHERE CD_ESTABELECIMENTO = ?
                """.formatted(placeholders);

        Object[] parametros = new Object[idsSelecionados.size() + 1];

        for (int i = 0; i < idsSelecionados.size(); i++) {
            parametros[i] = idsSelecionados.get(i);
        }

        parametros[idsSelecionados.size()] = cdEstabelecimento;

        return jdbcTemplate.update(sql, parametros);
    }

    // DESATIVAR CONDIÇÃO DE APOIO
    // Preserva o registro no banco.
    @Override
    public int excluir(Long id) {

        String sql = """
                UPDATE T_ARI_CONDICAO_APOIO
                SET FL_ATIVO = 'N'
                WHERE CD_CONDICAO_APOIO = ?
                """;

        return jdbcTemplate.update(sql, id);
    }
}
