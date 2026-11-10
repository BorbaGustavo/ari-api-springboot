
package br.com.ari.dao;

import java.util.Optional;

public interface CrudDAO<T, ID> {

    // CADASTRAR REGISTRO
    int criar(T objeto);

    // BUSCAR REGISTRO PELO IDENTIFICADOR
    Optional<T> buscarPorId(ID id);

    // ATUALIZAR REGISTRO
    int atualizar(T objeto);

    // EXCLUIR OU DESATIVAR REGISTRO,
    // CONFORME AS REGRAS DE CADA ENTIDADE
    int excluir(ID id);
}
