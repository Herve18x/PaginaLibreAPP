package org.paginalibre.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.paginalibre.dao.MovimientoInventarioDAO;
import org.paginalibre.model.MovimientoInventario;
import org.paginalibre.util.Conexion;

public class MovimientoInventarioDAOImpl implements MovimientoInventarioDAO {

    @Override
    public boolean insertar(MovimientoInventario movimiento) {
        // Llamada al procedimiento almacenado de MySQL
        String consulta = "{call sp_registrar_movimiento_inventario(?, ?, ?, ?, ?)}";
        
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consultaCall = conexion.prepareCall(consulta)) {

            consultaCall.setString(1, movimiento.getIsbn());
            consultaCall.setInt(2, movimiento.getIdTipoMovimiento());
            consultaCall.setInt(3, movimiento.getCantidad());  // _cantidad
            consultaCall.setInt(4, movimiento.getIdUsuario()); // _id_usuario
            consultaCall.setString(5, movimiento.getMotivo());  // _observacion

            return consultaCall.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<MovimientoInventario> listar() {
        List<MovimientoInventario> lista = new ArrayList<>();
        String consultaSQL = "SELECT id_movimiento, isbn, id_tipo_movimiento, id_usuario, cantidad, observacion, fecha_movimiento FROM movimientos_inventario ORDER BY id_movimiento DESC";

        try (Connection conexion = Conexion.getInstancia().conectar();
             PreparedStatement ps = conexion.prepareStatement(consultaSQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                MovimientoInventario m = new MovimientoInventario(
                    rs.getInt("id_movimiento"),
                    rs.getString("isbn"),
                    rs.getInt("id_tipo_movimiento"),
                    rs.getInt("id_usuario"),
                    rs.getInt("cantidad"),
                    rs.getString("observacion"),
                    rs.getTimestamp("fecha_movimiento")
                );
                lista.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public MovimientoInventario buscar(Integer id) {
        MovimientoInventario movimiento = null;
        String consultaSQL = "SELECT id_movimiento, isbn, id_tipo_movimiento, id_usuario, cantidad, observacion, fecha_movimiento FROM movimientos_inventario WHERE id_movimiento = ?";

        try (Connection conexion = Conexion.getInstancia().conectar();
             PreparedStatement ps = conexion.prepareStatement(consultaSQL)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    movimiento = new MovimientoInventario(
                        rs.getInt("id_movimiento"),
                        rs.getString("isbn"),
                        rs.getInt("id_tipo_movimiento"),
                        rs.getInt("id_usuario"),
                        rs.getInt("cantidad"),
                        rs.getString("observacion"),
                        rs.getTimestamp("fecha_movimiento")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movimiento;
    }

    @Override
    public boolean actualizar(MovimientoInventario objeto) {
        return false;
    }

    @Override
    public boolean eliminar(Integer id) {
        return false;
    }
}