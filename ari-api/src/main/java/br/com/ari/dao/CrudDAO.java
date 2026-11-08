package br.com.ari.dao;

import java.util.Optional;

public interface CrudDAO<T, ID> {

    int criar(T objeto);

    Optional<T> buscarPorId(ID id);

    int atualizar(T objeto);

    int excluir(ID id);
}