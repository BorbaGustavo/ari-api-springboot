package br.com.ari.dao;

import br.com.ari.model.Estabelecimento;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class EstabelecimentoDAO implements CrudDAO<Estabelecimento, Long> {


        private final JdbcTemplate jdbcTemplate;

        public EstabelecimentoDAO(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }


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


        @Override
        public Optional<Estabelecimento> buscarPorId(Long id) {

            String sql = """
            SELECT *
            FROM T_ARI_ESTABELECIMENTO
            WHERE CD_ESTABELECIMENTO = ?
            """;

            RowMapper<Estabelecimento> mapper = (rs, rowNum) ->
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

            return jdbcTemplate.query(sql, mapper, id)
                    .stream()
                    .findFirst();
        }


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


        @Override
        public int excluir(Long id) {

            String sql = """
            DELETE FROM T_ARI_ESTABELECIMENTO
            WHERE CD_ESTABELECIMENTO = ?
            """;

            return jdbcTemplate.update(sql, id);
        }
}

