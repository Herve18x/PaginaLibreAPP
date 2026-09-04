package org.paginalibre.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.paginalibre.dao.AutorDAO;
import org.paginalibre.model.Autor;
import org.paginalibre.util.Conexion;

public class AutorDAOImpl implements AutorDAO {

    private Connection getConection() throws SQLException {
        return Conexion.getInstancia().conectar(); 
    }

    @Override
    public boolean insertar(Autor autor) {
        String sql = "INSERT INTO autores (nombre_autor, apellido_autor, nacionalidad, biografia) VALUES (?, ?, ?, ?)";
        try (Connection con = getConection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, autor.getNombreAutor());
            ps.setString(2, autor.getApellidoAutor());
            ps.setString(3, autor.getNacionalidad());
            ps.setString(4, autor.getBiografia());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Autor> listar() {
        List<Autor> lista = new ArrayList<>();
        String sql = "SELECT * FROM autores";
        try (Connection con = getConection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Autor autor = new Autor(
                        rs.getInt("id_autor"),
                        rs.getString("nombre_autor"),
                        rs.getString("apellido_autor"),
                        rs.getString("nacionalidad"),
                        rs.getString("biografia")
                );
                lista.add(autor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public Autor buscar(Integer id) {
        Autor autor = null;
        String sql = "SELECT * FROM autores WHERE id_autor = ?";
        try (Connection con = getConection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    autor = new Autor(
                            rs.getInt("id_autor"),
                            rs.getString("nombre_autor"),
                            rs.getString("apellido_autor"),
                            rs.getString("nacionalidad"),
                            rs.getString("biografia")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return autor;
    }

    @Override
    public boolean actualizar(Autor autor) {
        String sql = "UPDATE autores SET nombre_autor = ?, apellido_autor = ?, nacionalidad = ?, biografia = ? WHERE id_autor = ?";
        try (Connection con = getConection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, autor.getNombreAutor());
            ps.setString(2, autor.getApellidoAutor());
            ps.setString(3, autor.getNacionalidad());
            ps.setString(4, autor.getBiografia());
            ps.setInt(5, autor.getIdAutor());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        String sql = "DELETE FROM autores WHERE id_autor = ?";
        try (Connection con = getConection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Autor> buscarPorNombre(String nombre) {
        List<Autor> lista = new ArrayList<>();
        String sql = "SELECT * FROM autores WHERE nombre_autor LIKE ? OR apellido_autor LIKE ?";
        try (Connection con = getConection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + nombre + "%");
            ps.setString(2, "%" + nombre + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Autor autor = new Autor(
                            rs.getInt("id_autor"),
                            rs.getString("nombre_autor"),
                            rs.getString("apellido_autor"),
                            rs.getString("nacionalidad"),
                            rs.getString("biografia")
                    );
                    lista.add(autor);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}