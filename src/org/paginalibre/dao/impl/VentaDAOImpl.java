package org.paginalibre.dao.impl;

import org.paginalibre.util.Conexion;
import org.paginalibre.model.Venta;
import org.paginalibre.dao.VentaDAO;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class VentaDAOImpl implements VentaDAO {

    @Override
    public int agregarConId(Venta venta) {
        String sql = "{call sp_insertarventa(?, ?, ?)}";
        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setLong(1, venta.getCuiCliente());
            cs.setInt(2, venta.getIdUsuario());
            cs.registerOutParameter(3, Types.INTEGER);
            
            cs.execute();
            return cs.getInt(3);
        } catch (SQLException e) {
            System.err.println("Error [Agregar Venta Con ID]: " + e.getMessage());
            return -1;
        }
    }

    @Override
    public boolean insertar(Venta objeto) {
        return agregarConId(objeto) > 0;
    }

    @Override
    public List<Venta> listar() {
        List<Venta> lista = new ArrayList<>();
        String sql = "{call sp_listarventas()}";
        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                Venta v = new Venta();
                v.setIdVenta(rs.getInt("id_venta"));
                v.setFechaVenta(rs.getTimestamp("fecha_venta"));
                v.setSubtotal(rs.getDouble("subtotal"));
                v.setDescuento(rs.getDouble("descuento"));
                v.setTotal(rs.getDouble("total"));
                v.setEstado(rs.getString("estado"));
                v.setCuiCliente(rs.getLong("cui_cliente"));
                v.setIdUsuario(rs.getInt("id_usuario"));
                lista.add(v);
            }
        } catch (SQLException e) {
            System.err.println("Error [Listar Ventas]: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public Venta buscar(Integer id) {
        String sql = "{call sp_buscarventa(?)}";
        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    Venta v = new Venta();
                    v.setIdVenta(rs.getInt("id_venta"));
                    v.setFechaVenta(rs.getTimestamp("fecha_venta"));
                    v.setSubtotal(rs.getDouble("subtotal"));
                    v.setDescuento(rs.getDouble("descuento"));
                    v.setTotal(rs.getDouble("total"));
                    v.setEstado(rs.getString("estado"));
                    v.setCuiCliente(rs.getLong("cui_cliente"));
                    v.setIdUsuario(rs.getInt("id_usuario"));
                    return v;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error [Buscar Venta por ID]: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean anularVenta(int idVenta, int idUsuarioAnulacion, String motivo) {
        String sql = "{call sp_anularventa(?, ?, ?)}";
        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idVenta);
            cs.setInt(2, idUsuarioAnulacion);
            cs.setString(3, motivo);
            return cs.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error [Anular Venta]: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean actualizar(Venta objeto) {
        // En el sistema de ventas los registros no se actualizan directamente
        return false;
    }

    @Override
    public boolean eliminar(Integer id) {
        // Las ventas se anulan con anularVenta, no se eliminan
        return false;
    }
}