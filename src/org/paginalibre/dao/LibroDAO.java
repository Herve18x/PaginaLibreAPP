package org.paginalibre.dao;

import java.util.List;
import org.paginalibre.model.Libro;

public interface LibroDAO extends CRUD<Libro, String> {
    
    List<Libro> listarLibros();
    List<Libro> listarLibrosBajoStock();
}