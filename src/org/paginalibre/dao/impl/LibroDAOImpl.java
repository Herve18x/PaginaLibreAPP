package org.paginalibre.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.paginalibre.dao.LibroDAO;
import org.paginalibre.model.Libro;
import org.paginalibre.util.Conexion;

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
            consultaCall.setInt(5, libros.getIdCategoria());
            consultaCall.setString(6, libros.getNitEditorial());
            consultaCall.setInt(7, libros.getStockActual());
            consultaCall.setInt(8, libros.getStockMinimo());

            return consultaCall.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar Libro: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Libro> listar() {
        List<Libro> lista = new ArrayList<>();
        String consultaSQL = "SELECT isbn, titulo, fecha_publicacion, precio, id_categoria, nit_editorial, stock_actual, stock_minimo, activo, fecha_actualizacion FROM libros WHERE activo = 1";

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
                        tablaResultado.getInt("id_categoria"),
                        tablaResultado.getString("nit_editorial"),
                        tablaResultado.getInt("stock_actual"),
                        tablaResultado.getInt("stock_minimo"),
                        tablaResultado.getBoolean("activo"),
                        tablaResultado.getTimestamp("fecha_actualizacion")
                );
                lista.add(libro);
            }
        } catch (SQLException e) {
            System.err.println("ERROR al listar Libros: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public Libro buscar(String isbn) {
        Libro libro = null;
        String consultaSQL = "SELECT isbn, titulo, fecha_publicacion, precio, id_categoria, nit_editorial, stock_actual, stock_minimo, activo, fecha_actualizacion FROM libros WHERE isbn = ?";
        
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
                            tablaResultado.getInt("id_categoria"),
                            tablaResultado.getString("nit_editorial"),
                            tablaResultado.getInt("stock_actual"),
                            tablaResultado.getInt("stock_minimo"),
                            tablaResultado.getBoolean("activo"),
                            tablaResultado.getTimestamp("fecha_actualizacion")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar Libro por ISBN (" + isbn + "): " + e.getMessage());
            e.printStackTrace();
        }
        return libro;
    }

    @Override
    public boolean actualizar(Libro libro) {
        String consultaSQL = "UPDATE libros SET titulo = ?, fecha_publicacion = ?, precio = ?, id_categoria = ?, nit_editorial = ?, stock_actual = ?, stock_minimo = ?, activo = ? WHERE isbn = ?";
        try (Connection conexion = Conexion.getInstancia().conectar();
             PreparedStatement ps = conexion.prepareStatement(consultaSQL)) {

            ps.setString(1, libro.getTitulo());
            ps.setDate(2, libro.getFechaPublicacion() != null ? java.sql.Date.valueOf(libro.getFechaPublicacion()) : null);
            ps.setDouble(3, libro.getPrecio());
            ps.setInt(4, libro.getIdCategoria());
            ps.setString(5, libro.getNitEditorial());
            ps.setInt(6, libro.getStockActual());
            ps.setInt(7, libro.getStockMinimo());
            ps.setBoolean(8, libro.isActivo());
            ps.setString(9, libro.getIsbn());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar Libro: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(String isbn) {
        String consultaSQL = "UPDATE libros SET activo = 0 WHERE isbn = ?";
        try (Connection conexion = Conexion.getInstancia().conectar();
             PreparedStatement ps = conexion.prepareStatement(consultaSQL)) {

            ps.setString(1, isbn);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar Libro (desactivar): " + e.getMessage());
            return false;
        }
    }
}