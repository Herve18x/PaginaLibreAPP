package org.paginalibre.dao.impl;

import org.paginalibre.util.Conexion;
import org.paginalibre.model.Usuario;
import org.paginalibre.dao.UsuarioDAO;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOImpl implements UsuarioDAO {

    // Convierte la contraseña en hash SHA-256 para coincidir con la BD y el Login
    private String encriptarSHA256(String password) {
        if (password == null || password.trim().isEmpty()) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error [SHA-256]: " + e.getMessage());
            return password;
        }
    }

    @Override
    public boolean insertar(Usuario objeto) {
        String sql = "{call sp_registrar_usuario(?, ?, ?, ?, ?, ?)}";
        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, objeto.getUsername());
            cs.setString(2, encriptarSHA256(objeto.getPasswordHash()));
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
        // Si el usuario ingresó una nueva contraseña en la vista
        if (objeto.getPasswordHash() != null && !objeto.getPasswordHash().trim().isEmpty()) {
            String sql = "UPDATE usuarios SET username = ?, password_hash = ?, rol = ?, nombre = ?, apellido = ?, correo = ?, activo = ? WHERE id = ?";
            try (Connection con = Conexion.getInstancia().conectar();
                 var ps = con.prepareStatement(sql)) {
                ps.setString(1, objeto.getUsername());
                ps.setString(2, encriptarSHA256(objeto.getPasswordHash()));
                ps.setString(3, objeto.getRol());
                ps.setString(4, objeto.getNombre());
                ps.setString(5, objeto.getApellido());
                ps.setString(6, objeto.getCorreo());
                ps.setBoolean(7, objeto.isActivo());
                ps.setInt(8, objeto.getId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("Error [Actualizar Usuario con Password]: " + e.getMessage());
                return false;
            }
        } else {
            // Si el campo de contraseña quedó vacío, se mantienen el password_hash actual sin sobreescribirlo
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