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

    private static final String SELECT_BASE =
            "SELECT l.isbn, l.titulo, l.fecha_publicacion, l.precio, " +
            "l.categoria_id, c.nombre_categoria, l.nit_editorial, e.nombre_editorial, " +
            "l.stock_actual, l.stock_minimo, l.estado, l.fecha_actualizacion, " +
            "COALESCE(GROUP_CONCAT(DISTINCT CONCAT(a.nombre_autor, ' ', a.apellido_autor) " +
            "ORDER BY a.apellido_autor, a.nombre_autor SEPARATOR ', '), '') AS autores " +
            "FROM libros l " +
            "LEFT JOIN categoria c ON c.categoria_id = l.categoria_id " +
            "LEFT JOIN editoriales e ON e.nit = l.nit_editorial " +
            "LEFT JOIN autores_libro al ON al.isbn = l.isbn " +
            "LEFT JOIN autores a ON a.id_autor = al.id_autor ";

    @Override
    public List<Libro> listar() {
        return consultar(SELECT_BASE +
                "WHERE l.estado = 1 " +
                "GROUP BY l.isbn, l.titulo, l.fecha_publicacion, l.precio, l.categoria_id, " +
                "c.nombre_categoria, l.nit_editorial, e.nombre_editorial, l.stock_actual, " +
                "l.stock_minimo, l.estado, l.fecha_actualizacion " +
                "ORDER BY l.titulo");
    }

    @Override
    public List<Libro> listarLibros() {
        return consultar(SELECT_BASE +
                "GROUP BY l.isbn, l.titulo, l.fecha_publicacion, l.precio, l.categoria_id, " +
                "c.nombre_categoria, l.nit_editorial, e.nombre_editorial, l.stock_actual, " +
                "l.stock_minimo, l.estado, l.fecha_actualizacion " +
                "ORDER BY l.titulo");
    }

    @Override
    public List<Libro> listarLibrosBajoStock() {
        return consultar(SELECT_BASE +
                "WHERE l.estado = 1 AND l.stock_actual <= l.stock_minimo " +
                "GROUP BY l.isbn, l.titulo, l.fecha_publicacion, l.precio, l.categoria_id, " +
                "c.nombre_categoria, l.nit_editorial, e.nombre_editorial, l.stock_actual, " +
                "l.stock_minimo, l.estado, l.fecha_actualizacion " +
                "ORDER BY l.stock_actual ASC, l.titulo");
    }

    @Override
    public Libro buscar(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return null;
        }

        String sql = SELECT_BASE +
                "WHERE l.isbn = ? " +
                "GROUP BY l.isbn, l.titulo, l.fecha_publicacion, l.precio, l.categoria_id, " +
                "c.nombre_categoria, l.nit_editorial, e.nombre_editorial, l.stock_actual, " +
                "l.stock_minimo, l.estado, l.fecha_actualizacion";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearLibro(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar libro: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean insertar(Libro libro) {
        if (libro == null || libro.getIsbn() == null || libro.getIsbn().isBlank()
                || libro.getTitulo() == null || libro.getTitulo().isBlank()) {
            return false;
        }

        String sql = "INSERT INTO libros " +
                "(isbn, titulo, fecha_publicacion, precio, categoria_id, nit_editorial, " +
                "stock_actual, stock_minimo, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, libro.getIsbn().trim());
            ps.setString(2, libro.getTitulo().trim());
            if (libro.getFechaPublicacion() == null) {
                ps.setNull(3, java.sql.Types.DATE);
            } else {
                ps.setDate(3, Date.valueOf(libro.getFechaPublicacion()));
            }
            ps.setDouble(4, libro.getPrecio());
            ps.setInt(5, libro.getCategoriaId());
            ps.setString(6, libro.getNitEditorial());
            ps.setInt(7, libro.getStockActual());
            ps.setInt(8, libro.getStockMinimo());
            ps.setBoolean(9, libro.isEstado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar libro: " + e.getMessage());
            return false;
        }
    }

    public boolean agregar(Libro libro) {
        return insertar(libro);
    }

    @Override
    public boolean actualizar(Libro libro) {
        if (libro == null || libro.getIsbn() == null || libro.getIsbn().isBlank()) {
            return false;
        }

        String sql = "UPDATE libros SET titulo = ?, fecha_publicacion = ?, precio = ?, " +
                "categoria_id = ?, nit_editorial = ?, stock_actual = ?, stock_minimo = ?, " +
                "estado = ? WHERE isbn = ?";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, libro.getTitulo());
            if (libro.getFechaPublicacion() == null) {
                ps.setNull(2, java.sql.Types.DATE);
            } else {
                ps.setDate(2, Date.valueOf(libro.getFechaPublicacion()));
            }
            ps.setDouble(3, libro.getPrecio());
            ps.setInt(4, libro.getCategoriaId());
            ps.setString(5, libro.getNitEditorial());
            ps.setInt(6, libro.getStockActual());
            ps.setInt(7, libro.getStockMinimo());
            ps.setBoolean(8, libro.isEstado());
            ps.setString(9, libro.getIsbn());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar libro: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return false;
        }

        String sql = "UPDATE libros SET estado = 0 WHERE isbn = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al desactivar libro: " + e.getMessage());
            return false;
        }
    }

    private List<Libro> consultar(String sql) {
        List<Libro> lista = new ArrayList<>();
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearLibro(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar libros: " + e.getMessage());
        }
        return lista;
    }

    private Libro mapearLibro(ResultSet rs) throws SQLException {
        Libro libro = new Libro();
        libro.setIsbn(rs.getString("isbn"));
        libro.setTitulo(rs.getString("titulo"));
        Date fecha = rs.getDate("fecha_publicacion");
        libro.setFechaPublicacion(fecha == null ? null : fecha.toLocalDate());
        libro.setPrecio(rs.getDouble("precio"));
        libro.setCategoriaId(rs.getInt("categoria_id"));
        libro.setNombreCategoria(rs.getString("nombre_categoria"));
        libro.setNitEditorial(rs.getString("nit_editorial"));
        libro.setNombreEditorial(rs.getString("nombre_editorial"));
        libro.setStockActual(rs.getInt("stock_actual"));
        libro.setStockMinimo(rs.getInt("stock_minimo"));
        libro.setEstado(rs.getBoolean("estado"));
        libro.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion"));
        libro.setAutores(rs.getString("autores"));
        return libro;
    }
}
