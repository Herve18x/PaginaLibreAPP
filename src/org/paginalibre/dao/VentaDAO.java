package org.paginalibre.dao;

import org.paginalibre.model.Venta;

public interface VentaDAO extends CRUD<Venta, Integer> {
    int agregarConId(Venta venta);
    boolean anularVenta(int idVenta, int idUsuarioAnulacion, String motivo);
}