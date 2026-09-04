package org.paginalibre.dao;

import java.util.List;
import org.paginalibre.model.Autor;

public interface AutorDAO extends CRUD<Autor, Integer> {
    List<Autor> buscarPorNombre(String nombre);
}
