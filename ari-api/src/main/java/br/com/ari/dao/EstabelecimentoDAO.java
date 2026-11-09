
package br.com.ari.dao;

import br.com.ari.model.Estabelecimento;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EstabelecimentoDAO implements CrudDAO<Estabelecimento, Long> {

    private final JdbcTemplate jdbcTemplate;

    public EstabelecimentoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // MAPEAMENTO DO RESULTADO DO BANCO PARA MODEL
    private final RowMapper<Estabelecimento> mapper = (rs, rowNum) ->
            Estabelecimento.builder()
                    .cdEstabelecimento(rs.getLong("CD_ESTABELECIMENTO"))
                    .cdTipoEstabelecimento(rs.getLong("CD_TIPO_ESTABELECIMENTO"))
                    .nrCnpj(rs.getString("NR_CNPJ"))
                    .nmRazaoSocial(rs.getString("NM_RAZAO_SOCIAL"))
                    .nmFantasia(rs.getString("NM_FANTASIA"))
                    .nrCep(rs.getString("NR_CEP"))
                    .nmLogradouro(rs.getString("NM_LOGRADOURO"))
                    .nrEndereco(rs.getString("NR_ENDERECO"))
                    .nmBairro(rs.getString("NM_BAIRRO"))
                    .nmCidade(rs.getString("NM_CIDADE"))
                    .sgUf(rs.getString("SG_UF"))
                    .nrLatitude(rs.getBigDecimal("NR_LATITUDE"))
                    .nrLongitude(rs.getBigDecimal("NR_LONGITUDE"))
                    .stEstabelecimento(rs.getString("ST_ESTABELECIMENTO"))
                    .dtCadastro(rs.getTimestamp("DT_CADASTRO").toLocalDateTime())
                    .build();

    // COLUNAS UTILIZADAS NAS CONSULTAS
    private static final String COLUNAS = """
            CD_ESTABELECIMENTO,
            CD_TIPO_ESTABELECIMENTO,
            NR_CNPJ,
            NM_RAZAO_SOCIAL,
            NM_FANTASIA,
            NR_CEP,
            NM_LOGRADOURO,
            NR_ENDERECO,
            NM_BAIRRO,
            NM_CIDADE,
            SG_UF,
            NR_LATITUDE,
            NR_LONGITUDE,
            ST_ESTABELECIMENTO,
            DT_CADASTRO
            """;

    // CADASTRAR ESTABELECIMENTO
    @Override
    public int criar(Estabelecimento estabelecimento) {

        String sql = """
                INSERT INTO T_ARI_ESTABELECIMENTO (
                    CD_TIPO_ESTABELECIMENTO,
                    NR_CNPJ,
                    NM_RAZAO_SOCIAL,
                    NM_FANTASIA,
                    NR_CEP,
                    NM_LOGRADOURO,
                    NR_ENDERECO,
                    NM_BAIRRO,
                    NM_CIDADE,
                    SG_UF,
                    NR_LATITUDE,
                    NR_LONGITUDE,
                    ST_ESTABELECIMENTO
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                estabelecimento.getCdTipoEstabelecimento(),
                estabelecimento.getNrCnpj(),
                estabelecimento.getNmRazaoSocial(),
                estabelecimento.getNmFantasia(),
                estabelecimento.getNrCep(),
                estabelecimento.getNmLogradouro(),
                estabelecimento.getNrEndereco(),
                estabelecimento.getNmBairro(),
                estabelecimento.getNmCidade(),
                estabelecimento.getSgUf(),
                estabelecimento.getNrLatitude(),
                estabelecimento.getNrLongitude(),
                estabelecimento.getStEstabelecimento()
        );
    }

    // BUSCAR ESTABELECIMENTO POR ID
    @Override
    public Optional<Estabelecimento> buscarPorId(Long id) {

        String sql = "SELECT " + COLUNAS + """
                FROM T_ARI_ESTABELECIMENTO
                WHERE CD_ESTABELECIMENTO = ?
                """;

        return jdbcTemplate.query(sql, mapper, id)
                .stream()
                .findFirst();
    }

    // LISTAR TODOS OS ESTABELECIMENTOS
    public List<Estabelecimento> buscarTodos() {

        String sql = "SELECT " + COLUNAS + """
                FROM T_ARI_ESTABELECIMENTO
                ORDER BY CD_ESTABELECIMENTO
                """;

        return jdbcTemplate.query(sql, mapper);
    }

    // ATUALIZAR ESTABELECIMENTO
    @Override
    public int atualizar(Estabelecimento estabelecimento) {

        String sql = """
                UPDATE T_ARI_ESTABELECIMENTO
                SET
                    NM_FANTASIA = ?,
                    NR_CEP = ?,
                    NM_LOGRADOURO = ?,
                    NR_ENDERECO = ?,
                    NM_BAIRRO = ?,
                    NM_CIDADE = ?,
                    SG_UF = ?,
                    NR_LATITUDE = ?,
                    NR_LONGITUDE = ?,
                    ST_ESTABELECIMENTO = ?
                WHERE CD_ESTABELECIMENTO = ?
                """;

        return jdbcTemplate.update(
                sql,
                estabelecimento.getNmFantasia(),
                estabelecimento.getNrCep(),
                estabelecimento.getNmLogradouro(),
                estabelecimento.getNrEndereco(),
                estabelecimento.getNmBairro(),
                estabelecimento.getNmCidade(),
                estabelecimento.getSgUf(),
                estabelecimento.getNrLatitude(),
                estabelecimento.getNrLongitude(),
                estabelecimento.getStEstabelecimento(),
                estabelecimento.getCdEstabelecimento()
        );
    }

    // DESATIVAR ESTABELECIMENTO
    @Override
    public int excluir(Long id) {

        String sql = """
                UPDATE T_ARI_ESTABELECIMENTO
                SET ST_ESTABELECIMENTO = 'SUSPENSO'
                WHERE CD_ESTABELECIMENTO = ?
                """;

        return jdbcTemplate.update(sql, id);
    }

    // BUSCAR ESTABELECIMENTOS POR CEP
    public List<Estabelecimento> buscarPorCep(String cep) {

        String sql = "SELECT " + COLUNAS + """
                FROM T_ARI_ESTABELECIMENTO
                WHERE NR_CEP = ?
                AND ST_ESTABELECIMENTO = 'ATIVO'
                ORDER BY NM_FANTASIA
                """;

        return jdbcTemplate.query(sql, mapper, cep);
    }

    // BUSCAR ESTABELECIMENTOS PRÓXIMOS
    public List<Estabelecimento> buscarProximos(Double latitude, Double longitude) {

        String sql = "SELECT " + COLUNAS + """
                FROM T_ARI_ESTABELECIMENTO
                WHERE ST_ESTABELECIMENTO = 'ATIVO'
                AND NR_LATITUDE IS NOT NULL
                AND NR_LONGITUDE IS NOT NULL
                ORDER BY
                    POWER(NR_LATITUDE - ?, 2) +
                    POWER(NR_LONGITUDE - ?, 2)
                """;

        return jdbcTemplate.query(sql, mapper, latitude, longitude);
    }

    public Optional<Estabelecimento> buscarPorCnpj(String cnpj) {

        String sql = "SELECT " + COLUNAS + """
            FROM T_ARI_ESTABELECIMENTO
            WHERE NR_CNPJ = ?
            """;

        return jdbcTemplate.query(sql, mapper, cnpj)
                .stream()
                .findFirst();
    }
}
