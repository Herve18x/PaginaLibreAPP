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
        String consulta = "{call sp_registrar_movimiento_inventario(?, ?, ?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consultaCall = conexion.prepareCall(consulta)) {

            consultaCall.setString(1, movimiento.getIsbn());
            consultaCall.setInt(2, movimiento.getIdTipoMovimiento());
            consultaCall.setInt(3, movimiento.getIdUsuario());
            consultaCall.setInt(4, movimiento.getCantidad());
            consultaCall.setString(5, movimiento.getMotivo());

            return consultaCall.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<MovimientoInventario> listar() {
        List<MovimientoInventario> lista = new ArrayList<>();
        // Se cambia 'motivo' por 'observacion'
        String consultaSQL = "SELECT id_movimiento, isbn, id_tipo_movimiento, id_usuario, cantidad, observacion, fecha_movimiento FROM movimientos_inventario ORDER BY fecha_movimiento DESC";

        try (Connection conexion = Conexion.getInstancia().conectar();
             PreparedStatement ps = conexion.prepareStatement(consultaSQL);
             ResultSet tablaResultado = ps.executeQuery()) {

            while (tablaResultado.next()) {
                MovimientoInventario movimiento = new MovimientoInventario(
                    tablaResultado.getInt("id_movimiento"),
                    tablaResultado.getString("isbn"),
                    tablaResultado.getInt("id_tipo_movimiento"),
                    tablaResultado.getInt("id_usuario"),
                    tablaResultado.getInt("cantidad"),
                    tablaResultado.getString("observacion"),
                    tablaResultado.getTimestamp("fecha_movimiento")
                );
                lista.add(movimiento);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public MovimientoInventario buscar(Integer id) {
        MovimientoInventario movimiento = null;
        // Se cambia 'motivo' por 'observacion'
        String consultaSQL = "SELECT id_movimiento, isbn, id_tipo_movimiento, id_usuario, cantidad, observacion, fecha_movimiento FROM movimientos_inventario WHERE id_movimiento = ?";

        try (Connection conexion = Conexion.getInstancia().conectar();
             PreparedStatement ps = conexion.prepareStatement(consultaSQL)) {

            ps.setInt(1, id);
            try (ResultSet tablaResultado = ps.executeQuery()) {
                if (tablaResultado.next()) {
                    movimiento = new MovimientoInventario(
                        tablaResultado.getInt("id_movimiento"),
                        tablaResultado.getString("isbn"),
                        tablaResultado.getInt("id_tipo_movimiento"),
                        tablaResultado.getInt("id_usuario"),
                        tablaResultado.getInt("cantidad"),
                        tablaResultado.getString("observacion"),
                        tablaResultado.getTimestamp("fecha_movimiento")
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