package org.paginalibre.dao;

import java.util.List;

public interface CRUD<T, ID> {
    public boolean insertar(T objeto);
    public List<T> listar();
    public T buscar(ID id);
    public boolean actualizar(T objeto);
    public boolean eliminar(ID id);
}