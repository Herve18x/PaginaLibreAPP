package org.paginalibre.dao.impl;

import org.paginalibre.dao.DetalleVentaDAO;
import org.paginalibre.model.DetalleVenta;
import org.paginalibre.util.Conexion;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DetalleVentaDAOImpl implements DetalleVentaDAO {

    @Override
    public boolean registrarDetalle(DetalleVenta detalle, Connection conn) throws Exception {
        // Se puede usar la llamada al procedimiento sp_agregardetalleventa o un INSERT directo
        String sql = "INSERT INTO detalle_venta (id_venta, isbn, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detalle.getIdVenta());
            ps.setString(2, detalle.getIsbn());
            ps.setInt(3, detalle.getCantidad());
            ps.setDouble(4, detalle.getPrecioUnitario());
            ps.setDouble(5, detalle.getSubtotal());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<DetalleVenta> obtenerDetallesPorVenta(int idVenta) throws Exception {
        List<DetalleVenta> lista = new ArrayList<>();
        // Uso del procedimiento almacenado existente en la base de datos
        String sql = "{call sp_listardetalleventa(?)}";
        
        // CORRECCIÓN: Se usa Conexion.getInstancia().conectar() para solicitar la conexión fresca
        try (Connection conn = Conexion.getInstancia().conectar();
             CallableStatement cs = conn.prepareCall(sql)) {
            
            cs.setInt(1, idVenta);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    DetalleVenta detalle = new DetalleVenta(
                        rs.getInt("id_detalle"),
                        rs.getInt("id_venta"),
                        rs.getString("isbn"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precio_unitario"),
                        rs.getDouble("subtotal")
                    );
                    lista.add(detalle);
                }
            }
        }
        return lista;
    }
}