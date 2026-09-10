package org.paginalibre.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.paginalibre.dao.ClienteDAO;
import org.paginalibre.model.Cliente;
import org.paginalibre.util.Conexion;

public class ClienteDAOImpl implements ClienteDAO {

    @Override
    public boolean insertar(Cliente cliente) {
        String sql = "{call sp_insertarcliente(?, ?, ?, ?)}";
        try (Connection conn = Conexion.getInstancia().conectar();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setLong(1, cliente.getCui());
            cs.setString(2, cliente.getNombre());
            cs.setString(3, cliente.getApellido());
            cs.setString(4, cliente.getCorreoElectronico());

            return cs.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Cliente> listar() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "{call sp_listarclientes()}";
        try (Connection conn = Conexion.getInstancia().conectar();
             CallableStatement cs = conn.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setCui(rs.getLong("cui"));
                cliente.setNombre(rs.getString("nombre_cliente"));
                cliente.setApellido(rs.getString("apellido_cliente"));
                cliente.setCorreoElectronico(rs.getString("correo_electronico"));
                clientes.add(cliente);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clientes;
    }

    @Override
    public Cliente buscar(Long cui) {
        Cliente cliente = null;
        String sql = "{call sp_buscarcliente(?)}";
        try (Connection conn = Conexion.getInstancia().conectar();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setLong(1, cui);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    cliente = new Cliente();
                    cliente.setCui(rs.getLong("cui"));
                    cliente.setNombre(rs.getString("nombre_cliente"));
                    cliente.setApellido(rs.getString("apellido_cliente"));
                    cliente.setCorreoElectronico(rs.getString("correo_electronico"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cliente;
    }

    @Override
    public boolean actualizar(Cliente cliente) {
        String sql = "{call sp_actualizarcliente(?, ?, ?, ?)}";
        try (Connection conn = Conexion.getInstancia().conectar();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setLong(1, cliente.getCui());
            cs.setString(2, cliente.getNombre());
            cs.setString(3, cliente.getApellido());
            cs.setString(4, cliente.getCorreoElectronico());

            return cs.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminar(Long cui) {
        String sql = "{call sp_eliminarcliente(?)}";
        try (Connection conn = Conexion.getInstancia().conectar();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setLong(1, cui);
            return cs.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}