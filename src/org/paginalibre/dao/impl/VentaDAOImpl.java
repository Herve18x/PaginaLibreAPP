package org.paginalibre.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.paginalibre.dao.DetalleVentaDAO;
import org.paginalibre.dao.VentaDAO;
import org.paginalibre.model.DetalleVenta;
import org.paginalibre.model.Venta;
import org.paginalibre.util.Conexion;

public class VentaDAOImpl implements VentaDAO {

    private final DetalleVentaDAO detalleVentaDAO = new DetalleVentaDAOImpl();

    @Override
    public boolean insertar(Venta objeto) {
        return false;
    }

    public boolean guardarVentaConDetalles(Venta venta, List<DetalleVenta> detalles) {
        String sqlVenta = "INSERT INTO ventas (fecha_venta, total, estado, cui_cliente, id_usuario) VALUES (NOW(), ?, ?, ?, ?)";
        
        Connection conexion = null;
        try {
            conexion = Conexion.getInstancia().conectar();
            conexion.setAutoCommit(false);

            try (PreparedStatement ps = conexion.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDouble(1, venta.getTotal());
                ps.setString(2, venta.getEstado());
                ps.setLong(3, venta.getCuiCliente());
                ps.setInt(4, venta.getIdUsuario());

                int filas = ps.executeUpdate();
                if (filas == 0) {
                    conexion.rollback();
                    return false;
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        venta.setIdVenta(rs.getInt(1));
                    }
                }
            }

            for (DetalleVenta detalle : detalles) {
                detalle.setIdVenta(venta.getIdVenta());
                boolean exitoDetalle = detalleVentaDAO.registrarDetalle(detalle, conexion);
                if (!exitoDetalle) {
                    conexion.rollback();
                    return false;
                }
            }

            conexion.commit();
            return true;

        } catch (Exception e) {
            if (conexion != null) {
                try {
                    conexion.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Error al guardar venta y detalles: " + e.getMessage());
            return false;
        } finally {
            if (conexion != null) {
                try {
                    conexion.setAutoCommit(true);
                    conexion.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public List<Venta> listar() {
        List<Venta> lista = new ArrayList<>();
        String sql = "{call sp_listarventas()}";

        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement cs = conexion.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                Venta venta = new Venta();
                venta.setIdVenta(rs.getInt("id_venta"));
                venta.setFechaVenta(rs.getTimestamp("fecha_venta"));
                venta.setSubtotal(rs.getDouble("subtotal"));
                venta.setDescuento(rs.getDouble("descuento"));
                venta.setTotal(rs.getDouble("total"));
                venta.setEstado(rs.getString("estado"));
                venta.setCuiCliente(rs.getLong("cui_cliente"));
                venta.setIdUsuario(rs.getInt("id_usuario"));

                lista.add(venta);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar ventas: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public Venta buscar(Integer id) {
        return null;
    }

    @Override
    public boolean actualizar(Venta objeto) {
        return false;
    }

   @Override
public boolean eliminar(Integer id) {
    String sql = "UPDATE ventas SET estado = 'DEVUELTA' WHERE id_venta = ? AND estado = 'COMPLETADA'";
    try (Connection conexion = Conexion.getInstancia().conectar();
         PreparedStatement ps = conexion.prepareStatement(sql)) {

        ps.setInt(1, id);
        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        System.err.println("Error al reembolsar venta: " + e.getMessage());
        return false;
    }
}
}