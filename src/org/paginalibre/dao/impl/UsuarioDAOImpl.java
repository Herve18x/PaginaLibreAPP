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
        String sql = "{call sp_registrar_usuario(?, ?, ?, ?, ?, ?)}";
        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, objeto.getUsername());
            cs.setString(2, objeto.getPasswordHash());
            cs.setString(3, objeto.getRol());
            cs.setString(4, objeto.getNombre());
            cs.setString(5, objeto.getApellido());
            cs.setString(6, objeto.getCorreo());
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
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setRol(rs.getString("rol"));
                u.setNombre(rs.getString("nombre"));
                u.setApellido(rs.getString("apellido"));
                u.setCorreo(rs.getString("correo"));
                u.setActivo(rs.getBoolean("activo"));
                lista.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Error [Listar Usuarios]: " + e.getMessage());
        }
        return lista;
    }

    // Método utilizado específicamente para el Inicio de Sesión
    public Usuario buscarPorUsername(String username) {
        String sql = "{call sp_iniciar_sesion(?)}";
        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, username);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setRol(rs.getString("rol"));
                    u.setActivo(rs.getBoolean("activo"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error [Buscar Usuario por Username]: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Usuario buscar(Integer id) {
        // Si necesitas buscar por ID en el CRUD, usa una consulta SELECT estándar
        String sql = "SELECT id, username, password_hash, rol, nombre, apellido, correo, activo FROM usuarios WHERE id = ?";
        try (Connection con = Conexion.getInstancia().conectar();
             var ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setRol(rs.getString("rol"));
                    u.setNombre(rs.getString("nombre"));
                    u.setApellido(rs.getString("apellido"));
                    u.setCorreo(rs.getString("correo"));
                    u.setActivo(rs.getBoolean("activo"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error [Buscar Usuario por ID]: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean actualizar(Usuario objeto) {
        String sql = "UPDATE usuarios SET username = ?, rol = ?, nombre = ?, apellido = ?, correo = ?, activo = ? WHERE id = ?";
        try (Connection con = Conexion.getInstancia().conectar();
             var ps = con.prepareStatement(sql)) {
            ps.setString(1, objeto.getUsername());
            ps.setString(2, objeto.getRol());
            ps.setString(3, objeto.getNombre());
            ps.setString(4, objeto.getApellido());
            ps.setString(5, objeto.getCorreo());
            ps.setBoolean(6, objeto.isActivo());
            ps.setInt(7, objeto.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error [Actualizar Usuario]: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        String sql = "UPDATE usuarios SET activo = false WHERE id = ?";
        try (Connection con = Conexion.getInstancia().conectar();
             var ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error [Eliminar Usuario]: " + e.getMessage());
            return false;
        }
    }
}