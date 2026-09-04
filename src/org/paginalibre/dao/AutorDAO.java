package org.paginalibre.dao;

import java.util.List;
import org.paginalibre.model.Autor;

public interface AutorDAO extends CRUD<Autor, Integer> {
    // Consulta personalizada para buscar autores por coincidencia en el nombre
    List<Autor> buscarPorNombre(String nombre);
}