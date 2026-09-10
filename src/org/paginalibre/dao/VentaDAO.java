package org.paginalibre.dao;

import java.util.List;
import org.paginalibre.model.Venta;

public interface VentaDAO extends CRUD<Venta, Integer> {
    List<Venta> listar();
}
