package org.paginalibre.dao.impl;

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
    public boolean insertar(Venta venta) {
        if (venta == null || venta.getIdUsuario() <= 0) {
            return false;
        }

        String sql = "INSERT INTO ventas(subtotal, descuento, total, estado, cui_cliente, id_usuario) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, venta.getSubtotal());
            ps.setDouble(2, venta.getDescuento());
            ps.setDouble(3, venta.getTotal());
            ps.setString(4, venta.getEstado() == null ? "COMPLETADA" : venta.getEstado());
            ps.setLong(5, venta.getCuiCliente());
            ps.setInt(6, venta.getIdUsuario());

            if (ps.executeUpdate() == 0) {
                return false;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    venta.setIdVenta(rs.getInt(1));
                }
            }
            return venta.getIdVenta() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar venta: " + e.getMessage());
            return false;
        }
    }

    public boolean guardarVentaConDetalles(Venta venta, List<DetalleVenta> detalles) {
        if (venta == null || detalles == null || detalles.isEmpty()
                || venta.getIdUsuario() <= 0) {
            return false;
        }

        long cuiCliente = venta.getCuiCliente() <= 0 ? 0L : venta.getCuiCliente();
        Connection conn = null;

        try {
            conn = Conexion.getInstancia().conectar();
            conn.setAutoCommit(false);

            String insertVenta = "INSERT INTO ventas(subtotal, descuento, total, estado, cui_cliente, id_usuario) " +
                    "VALUES (0, 0, 0, 'COMPLETADA', ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(insertVenta, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, cuiCliente);
                ps.setInt(2, venta.getIdUsuario());
                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    venta.setIdVenta(rs.getInt(1));
                }
            }

            double subtotal = 0.0;

            for (DetalleVenta detalle : detalles) {
                if (detalle == null || detalle.getIsbn() == null
                        || detalle.getIsbn().isBlank() || detalle.getCantidad() <= 0) {
                    conn.rollback();
                    return false;
                }

                double precio;
                int stock;

                String selectLibro = "SELECT precio, stock_actual, estado FROM libros WHERE isbn = ? FOR UPDATE";
                try (PreparedStatement ps = conn.prepareStatement(selectLibro)) {
                    ps.setString(1, detalle.getIsbn());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !rs.getBoolean("estado")) {
                            conn.rollback();
                            return false;
                        }
                        precio = rs.getDouble("precio");
                        stock = rs.getInt("stock_actual");
                    }
                }

                if (stock < detalle.getCantidad()) {
                    conn.rollback();
                    return false;
                }

                double subtotalLinea = precio * detalle.getCantidad();
                subtotal += subtotalLinea;

                detalle.setIdVenta(venta.getIdVenta());
                detalle.setPrecioUnitario(precio);
                detalle.setSubtotal(subtotalLinea);

                if (!detalleVentaDAO.registrarDetalle(detalle, conn)) {
                    conn.rollback();
                    return false;
                }

                String updateStock = "UPDATE libros SET stock_actual = stock_actual - ? WHERE isbn = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateStock)) {
                    ps.setInt(1, detalle.getCantidad());
                    ps.setString(2, detalle.getIsbn());
                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                String insertMovimiento = "INSERT INTO movimientos_inventario" +
                        "(isbn, id_tipo_movimiento, cantidad, id_usuario, observacion) " +
                        "VALUES (?, 2, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertMovimiento)) {
                    ps.setString(1, detalle.getIsbn());
                    ps.setInt(2, detalle.getCantidad());
                    ps.setInt(3, venta.getIdUsuario());
                    ps.setString(4, "Venta #" + venta.getIdVenta());
                    ps.executeUpdate();
                }
            }

            double descuento = Math.max(0.0, venta.getDescuento());
            if (descuento > subtotal) {
                descuento = subtotal;
            }

            double total = subtotal - descuento;

            String updateVenta = "UPDATE ventas SET subtotal = ?, descuento = ?, total = ?, " +
                    "usuario_autoriza_descuento = CASE WHEN ? > 0 THEN ? ELSE NULL END " +
                    "WHERE id_venta = ?";

            try (PreparedStatement ps = conn.prepareStatement(updateVenta)) {
                ps.setDouble(1, subtotal);
                ps.setDouble(2, descuento);
                ps.setDouble(3, total);
                ps.setDouble(4, descuento);
                ps.setInt(5, venta.getIdUsuario());
                ps.setInt(6, venta.getIdVenta());
                ps.executeUpdate();
            }

            venta.setCuiCliente(cuiCliente);
            venta.setSubtotal(subtotal);
            venta.setDescuento(descuento);
            venta.setTotal(total);
            venta.setEstado("COMPLETADA");

            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            System.err.println("Error al guardar venta: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    @Override
    public List<Venta> listar() {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT id_venta, fecha_venta, subtotal, descuento, total, estado, " +
                "cui_cliente, id_usuario, usuario_autoriza_descuento, fecha_anulacion, " +
                "usuario_anulacion, motivo_anulacion FROM ventas ORDER BY fecha_venta DESC, id_venta DESC";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearVenta(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar ventas: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public Venta buscar(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }

        String sql = "SELECT id_venta, fecha_venta, subtotal, descuento, total, estado, " +
                "cui_cliente, id_usuario, usuario_autoriza_descuento, fecha_anulacion, " +
                "usuario_anulacion, motivo_anulacion FROM ventas WHERE id_venta = ?";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearVenta(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar venta: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean actualizar(Venta venta) {
        if (venta == null || venta.getIdVenta() <= 0) {
            return false;
        }

        String sql = "UPDATE ventas SET descuento = ?, total = ?, estado = ? WHERE id_venta = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, venta.getDescuento());
            ps.setDouble(2, venta.getTotal());
            ps.setString(3, venta.getEstado());
            ps.setInt(4, venta.getIdVenta());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar venta: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        if (id == null || id <= 0) {
            return false;
        }

        Connection conn = null;
        try {
            conn = Conexion.getInstancia().conectar();
            conn.setAutoCommit(false);

            String estado;
            String select = "SELECT estado FROM ventas WHERE id_venta = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(select)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    estado = rs.getString("estado");
                }
            }

            if (!"COMPLETADA".equalsIgnoreCase(estado)) {
                conn.rollback();
                return false;
            }

            String detalles = "SELECT isbn, cantidad FROM detalle_venta WHERE id_venta = ?";
            try (PreparedStatement ps = conn.prepareStatement(detalles)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String isbn = rs.getString("isbn");
                        int cantidad = rs.getInt("cantidad");

                        try (PreparedStatement update = conn.prepareStatement(
                                "UPDATE libros SET stock_actual = stock_actual + ? WHERE isbn = ?")) {
                            update.setInt(1, cantidad);
                            update.setString(2, isbn);
                            if (update.executeUpdate() == 0) {
                                conn.rollback();
                                return false;
                            }
                        }

                        try (PreparedStatement movimiento = conn.prepareStatement(
                                "INSERT INTO movimientos_inventario" +
                                "(isbn, id_tipo_movimiento, cantidad, id_usuario, observacion) " +
                                "VALUES (?, 5, ?, ?, ?)")) {
                            movimiento.setString(1, isbn);
                            movimiento.setInt(2, cantidad);
                            movimiento.setInt(3, obtenerUsuarioAnulacion(conn, id));
                            movimiento.setString(4, "Reembolso venta #" + id);
                            movimiento.executeUpdate();
                        }
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE ventas SET estado = 'DEVUELTA', fecha_anulacion = CURRENT_TIMESTAMP, " +
                    "usuario_anulacion = ?, motivo_anulacion = ? WHERE id_venta = ?")) {
                ps.setInt(1, obtenerUsuarioAnulacion(conn, id));
                ps.setString(2, "Reembolso de venta #" + id);
                ps.setInt(3, id);
                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("Error al reembolsar venta: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    private int obtenerUsuarioAnulacion(Connection conn, int idVenta) throws SQLException {
        String sql = "SELECT id_usuario FROM ventas WHERE id_venta = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 1;
    }

    private Venta mapearVenta(ResultSet rs) throws SQLException {
        Venta venta = new Venta();
        venta.setIdVenta(rs.getInt("id_venta"));
        venta.setFechaVenta(rs.getTimestamp("fecha_venta"));
        venta.setSubtotal(rs.getDouble("subtotal"));
        venta.setDescuento(rs.getDouble("descuento"));
        venta.setTotal(rs.getDouble("total"));
        venta.setEstado(rs.getString("estado"));
        venta.setCuiCliente(rs.getLong("cui_cliente"));
        venta.setIdUsuario(rs.getInt("id_usuario"));
        venta.setUsuarioAutorizaDescuento(rs.getInt("usuario_autoriza_descuento"));
        venta.setFechaAnulacion(rs.getTimestamp("fecha_anulacion"));
        venta.setUsuarioAnulacion(rs.getInt("usuario_anulacion"));
        venta.setMotivoAnulacion(rs.getString("motivo_anulacion"));
        return venta;
    }
}
