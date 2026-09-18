package org.paginalibre.dao.impl;

import org.paginalibre.dao.LibroDAO;
import org.paginalibre.model.Libro;
import org.paginalibre.util.Conexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LibroDAOImpl implements LibroDAO {

    @Override
    public List<Libro> listarLibrosBajoStock() {
        List<Libro> lista = new ArrayList<>();
        // Cambiado "libro" por "libros"
        String sql = "SELECT l.*, c.nombre_categoria AS nombre_categoria, "
                   + "e.nombre_editorial AS nombre_editorial, "
                   + "COALESCE((SELECT GROUP_CONCAT(CONCAT(a.nombre_autor, ' ', a.apellido_autor) "
                   + "ORDER BY a.apellido_autor SEPARATOR ', ') FROM autores_libro al "
                   + "INNER JOIN autores a ON a.id_autor = al.id_autor WHERE al.isbn = l.isbn), '') AS autores "
                   + "FROM libros l "
                   + "INNER JOIN categoria c ON l.categoria_id = c.categoria_id "
                   + "LEFT JOIN editoriales e ON l.nit_editorial = e.nit "
                   + "WHERE l.stock_actual <= l.stock_minimo AND l.estado = 1";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearLibro(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar libros bajo stock: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public List<Libro> listarLibros() {
        return listar();
    }

    @Override
    public List<Libro> listar() {
        List<Libro> lista = new ArrayList<>();
        // Cambiado "libro" por "libros"
        String sql = "SELECT l.*, c.nombre_categoria AS nombre_categoria, "
                   + "e.nombre_editorial AS nombre_editorial, "
                   + "COALESCE((SELECT GROUP_CONCAT(CONCAT(a.nombre_autor, ' ', a.apellido_autor) "
                   + "ORDER BY a.apellido_autor SEPARATOR ', ') FROM autores_libro al "
                   + "INNER JOIN autores a ON a.id_autor = al.id_autor WHERE al.isbn = l.isbn), '') AS autores "
                   + "FROM libros l "
                   + "INNER JOIN categoria c ON l.categoria_id = c.categoria_id "
                   + "LEFT JOIN editoriales e ON l.nit_editorial = e.nit "
                   + "WHERE l.estado = 1";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearLibro(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar libros: " + e.getMessage());
        }
        return lista;
    }

    public Libro buscar(String isbn) {
        Libro libro = null;
        // Cambiado "libro" por "libros"
        String sql = "SELECT l.*, c.nombre_categoria AS nombre_categoria, "
                   + "e.nombre_editorial AS nombre_editorial, "
                   + "COALESCE((SELECT GROUP_CONCAT(CONCAT(a.nombre_autor, ' ', a.apellido_autor) "
                   + "ORDER BY a.apellido_autor SEPARATOR ', ') FROM autores_libro al "
                   + "INNER JOIN autores a ON a.id_autor = al.id_autor WHERE al.isbn = l.isbn), '') AS autores "
                   + "FROM libros l "
                   + "INNER JOIN categoria c ON l.categoria_id = c.categoria_id "
                   + "LEFT JOIN editoriales e ON l.nit_editorial = e.nit "
                   + "WHERE l.isbn = ? AND l.estado = 1";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    libro = mapearLibro(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar libro por ISBN: " + e.getMessage());
        }
        return libro;
    }

    @Override
    public boolean insertar(Libro libro) {
        // Cambiado "libro" por "libros"
        String sql = "INSERT INTO libros (isbn, titulo, precio, stock_actual, stock_minimo, categoria_id, nit_editorial, fecha_publicacion, estado) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, libro.getIsbn());
            stmt.setString(2, libro.getTitulo());
            stmt.setDouble(3, libro.getPrecio());
            stmt.setInt(4, libro.getStockActual());
            stmt.setInt(5, libro.getStockMinimo());
            stmt.setInt(6, libro.getIdCategoria());
            stmt.setString(7, libro.getNitEditorial());
            stmt.setDate(8, libro.getFechaPublicacion() != null ? Date.valueOf(libro.getFechaPublicacion()) : null);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar libro: " + e.getMessage());
        }
        return false;
    }

    public boolean agregar(Libro libro) {
        return insertar(libro);
    }

    @Override
    public boolean actualizar(Libro libro) {
        // Cambiado "libro" por "libros"
        String sql = "UPDATE libros SET titulo = ?, precio = ?, stock_actual = ?, stock_minimo = ?, "
                   + "categoria_id = ?, nit_editorial = ?, fecha_publicacion = ? WHERE isbn = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, libro.getTitulo());
            stmt.setDouble(2, libro.getPrecio());
            stmt.setInt(3, libro.getStockActual());
            stmt.setInt(4, libro.getStockMinimo());
            stmt.setInt(5, libro.getIdCategoria());
            stmt.setString(6, libro.getNitEditorial());
            stmt.setDate(7, libro.getFechaPublicacion() != null ? Date.valueOf(libro.getFechaPublicacion()) : null);
            stmt.setString(8, libro.getIsbn());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar libro: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean eliminar(String isbn) {
        // Cambiado "libro" por "libros"
        String sql = "UPDATE libros SET estado = 0 WHERE isbn = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar libro: " + e.getMessage());
        }
        return false;
    }

    private Libro mapearLibro(ResultSet rs) throws SQLException {
        Libro libro = new Libro();
        libro.setIsbn(rs.getString("isbn"));
        libro.setTitulo(rs.getString("titulo"));
        libro.setPrecio(rs.getDouble("precio"));
        libro.setStockActual(rs.getInt("stock_actual"));
        libro.setStockMinimo(rs.getInt("stock_minimo"));
        libro.setIdCategoria(rs.getInt("categoria_id"));
        libro.setNombreCategoria(rs.getString("nombre_categoria"));
        libro.setNitEditorial(rs.getString("nit_editorial"));
        libro.setNombreEditorial(rs.getString("nombre_editorial"));
        libro.setAutores(rs.getString("autores"));
        
        Date fechaSql = rs.getDate("fecha_publicacion");
        if (fechaSql != null) {
            libro.setFechaPublicacion(fechaSql.toLocalDate());
        }
        
        try {
            libro.setActivo(rs.getBoolean("estado"));
        } catch (SQLException e) {
            libro.setActivo(true);
        }

        try {
            libro.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion"));
        } catch (SQLException e) {
            libro.setFechaActualizacion(null);
        }
        
        return libro;
    }
}