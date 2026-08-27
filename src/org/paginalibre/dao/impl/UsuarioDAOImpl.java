package org.paginalibre.dao.impl;

import org.paginalibre.util.Conexion;
import org.paginalibre.model.Usuario;
import org.paginalibre.dao.UsuarioDAO;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOImpl implements UsuarioDAO {

    @Override
    public boolean insertar(Usuario objeto) {
        String sql = "{call sp_insertarusuario(?, ?, ?)}";
        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, objeto.getUsuario());
            cs.setString(2, objeto.getClave());
            cs.setString(3, objeto.getRol());
            return cs.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error [Insertar Usuario]: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "{call sp_listarusuarios()}";
        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                lista.add(new Usuario(
                    rs.getInt("usuario_id"),
                    rs.getString("usuario"),
                    rs.getString("clave"),
                    rs.getString("rol")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error [Listar Usuarios]: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public Usuario buscar(Integer id) {
        String sql = "{call sp_buscarusuario(?)}";
        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                        rs.getInt("usuario_id"),
                        rs.getString("usuario"),
                        rs.getString("clave"),
                        rs.getString("rol")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error [Buscar Usuario]: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean actualizar(Usuario objeto) {
        String sql = "{call sp_actualizarusuario(?, ?, ?, ?)}";
        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, objeto.getUsuarioId());
            cs.setString(2, objeto.getUsuario());
            cs.setString(3, objeto.getClave());
            cs.setString(4, objeto.getRol());
            return cs.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error [Actualizar Usuario]: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        String sql = "{call sp_eliminarusuario(?)}";
        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, id);
            return cs.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error [Eliminar Usuario]: " + e.getMessage());
            return false;
        }
    }
}