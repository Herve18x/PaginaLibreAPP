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
    public boolean insertar(Libro libros) {
        String consulta = "{call sp_insertarlibro(?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar(); 
             CallableStatement consultaCall = conexion.prepareCall(consulta)) {
            
            consultaCall.setString(1, libros.getIsbn());
            consultaCall.setString(2, libros.getTitulo());
            consultaCall.setDate(3, java.sql.Date.valueOf(libros.getFechaPublicacion()));
            consultaCall.setDouble(4, libros.getPrecio());
            consultaCall.setInt(5, libros.getCategoriaId());
            consultaCall.setString(6, libros.getNitEditorial());
            consultaCall.setInt(7, libros.getStockActual());
            consultaCall.setInt(8, libros.getStockMinimo());

            return consultaCall.executeUpdate() > 0;
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
        String consultaSQL = "SELECT isbn, titulo, fecha_publicacion, precio, categoria_id, nit_editorial, stock_actual, stock_minimo, estado, fecha_actualizacion FROM libros WHERE estado = 1";

        try (Connection conexion = Conexion.getInstancia().conectar(); 
             PreparedStatement ps = conexion.prepareStatement(consultaSQL); 
             ResultSet tablaResultado = ps.executeQuery()) {

            while (tablaResultado.next()) {
                java.sql.Date sqlFecha = tablaResultado.getDate("fecha_publicacion");
                LocalDate fechaPublicacion = (sqlFecha != null) ? sqlFecha.toLocalDate() : null;

                Libro libro = new Libro(
                        tablaResultado.getString("isbn"),
                        tablaResultado.getString("titulo"),
                        fechaPublicacion,
                        tablaResultado.getDouble("precio"),
                        tablaResultado.getInt("categoria_id"),
                        tablaResultado.getString("nit_editorial"),
                        tablaResultado.getInt("stock_actual"),
                        tablaResultado.getInt("stock_minimo"),
                        tablaResultado.getBoolean("estado"),
                        tablaResultado.getTimestamp("fecha_actualizacion")
                );
                lista.add(libro);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar libros: " + e.getMessage());
        }
        return lista;
    }

    public Libro buscar(String isbn) {
        Libro libro = null;
        String consultaSQL = "SELECT isbn, titulo, fecha_publicacion, precio, categoria_id, nit_editorial, stock_actual, stock_minimo, estado, fecha_actualizacion FROM libros WHERE isbn = ?";
        
        try (Connection conexion = Conexion.getInstancia().conectar(); 
             PreparedStatement ps = conexion.prepareStatement(consultaSQL)) {
            
            ps.setString(1, isbn);
            try (ResultSet tablaResultado = ps.executeQuery()) {
                if (tablaResultado.next()) {
                    java.sql.Date sqlFecha = tablaResultado.getDate("fecha_publicacion");
                    LocalDate fechaPublicacion = (sqlFecha != null) ? sqlFecha.toLocalDate() : null;

                    libro = new Libro(
                            tablaResultado.getString("isbn"),
                            tablaResultado.getString("titulo"),
                            fechaPublicacion,
                            tablaResultado.getDouble("precio"),
                            tablaResultado.getInt("categoria_id"),
                            tablaResultado.getString("nit_editorial"),
                            tablaResultado.getInt("stock_actual"),
                            tablaResultado.getInt("stock_minimo"),
                            tablaResultado.getBoolean("estado"),
                            tablaResultado.getTimestamp("fecha_actualizacion")
                    );
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
       String consultaSQL = "SELECT isbn, titulo, fecha_publicacion, precio, categoria_id, nit_editorial, stock_actual, stock_minimo, estado, fecha_actualizacion FROM libros WHERE isbn = ?";
        try (Connection conexion = Conexion.getInstancia().conectar();
             PreparedStatement ps = conexion.prepareStatement(consultaSQL)) {

            ps.setString(1, libro.getTitulo());
            ps.setDate(2, libro.getFechaPublicacion() != null ? java.sql.Date.valueOf(libro.getFechaPublicacion()) : null);
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
        }
        return false;
    }

    @Override
    public boolean eliminar(String isbn) {
        String consultaSQL = "UPDATE libros SET estado = 0 WHERE isbn = ?";
        try (Connection conexion = Conexion.getInstancia().conectar();
             PreparedStatement ps = conexion.prepareStatement(consultaSQL)) {

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