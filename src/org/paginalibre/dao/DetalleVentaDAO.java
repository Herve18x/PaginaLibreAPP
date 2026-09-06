package org.paginalibre.dao;

import org.paginalibre.model.DetalleVenta;
import java.sql.Connection;
import java.util.List;

public interface DetalleVentaDAO {
    boolean registrarDetalle(DetalleVenta detalle, Connection conn) throws Exception;
    List<DetalleVenta> obtenerDetallesPorVenta(int idVenta) throws Exception;
}