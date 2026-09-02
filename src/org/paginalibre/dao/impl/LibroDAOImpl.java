package org.paginalibre.dao.impl;

import java.security.Timestamp;
import org.paginalibre.util.Conexion;
import org.paginalibre.model.Libro;
import org.paginalibre.dao.LibroDAO;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
//import java.sql.Timestamp;


public class LibroDAOImpl implements LibroDAO {

    @Override
    public boolean insertar(Libro libros) {
        String consulta = "{call sp_insertarlibro(?, ?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar(); CallableStatement consultaCall = conexion.prepareCall(consulta)) {
            consultaCall.setString(1, libros.getIsbn());
            consultaCall.setString(2, libros.getTitulo());
            consultaCall.setDate(3, java.sql.Date.valueOf(libros.getFechaPublicacion()));
            consultaCall.setDouble(4, libros.getPrecio());
            consultaCall.setInt(5, libros.getIdCategoria());
            consultaCall.setString(6, libros.getNitEditorial());
            consultaCall.setInt(7, libros.getStockActual());
            consultaCall.setInt(8, libros.getStockMinimo());
            consultaCall.setBoolean(9, libros.isActivo());
            consultaCall.setTimestamp(10, libros.getFechaActualizacion());

            return consultaCall.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.print("Error al crear Libro: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Libro> listar() {
        List<Libro> lista = new ArrayList<>();
        String consulta = "{call sp_listarlibro()}";

        try (Connection conexion = Conexion.getInstancia().conectar(); CallableStatement consultaCall = conexion.prepareCall(consulta); ResultSet tablaResultado = consultaCall.executeQuery();) {

            while (tablaResultado.next()) {
                ///
                String textoFecha = tablaResultado.getDate("fecha_publicacion").toString();
                DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate fecha = LocalDate.parse(textoFecha, formato);
                lista.add(new Libro(
                        tablaResultado.getString("isbn"),
                        tablaResultado.getString("titulo"),
                        //LocalDate.parse(tablaResultado.getDate("fecha_publicacion").toString()),
                        fecha,
                        tablaResultado.getDouble("precio"),
                        tablaResultado.getInt("id_editorial"),
                        tablaResultado.getString("nit_editorial"),
                        tablaResultado.getInt("stock_actual"),
                        tablaResultado.getInt("stock_minimo"),
                        tablaResultado.getBoolean("activo"),
                        tablaResultado.getTimestamp("fecha_actualizacion")
                        ));
            }
        } catch (SQLException e) {
            System.err.println("ERROR al listar Autores:" + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public Autores buscarPorId(int id_autor) {
        Autores autor = new Autores();
        String consultaSQL = "{call sp_buscarautor(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar(); CallableStatement consultaCall = conexion.prepareCall(consultaSQL);) {
            consultaCall.setInt(1, id_autor);
            try (ResultSet tablaResultado = consultaCall.executeQuery()) {
                if (tablaResultado.next()) {
                    autor.setIdAutor(tablaResultado.getInt("id_autor"));
                    autor.setNombreAutor(tablaResultado.getString("nombre_autor"));
                    autor.setApellidoAutor(tablaResultado.getString("apellido_autor"));
                    autor.setNacionalidad(tablaResultado.getString("nacionalidad"));
                    autor.setBiografia(tablaResultado.getString("biografia"));
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.print("Error al buscar Autor: " + e.getMessage());
            e.printStackTrace();
        }
        return autor;
    }

    @Override
    public boolean actualizar(Autores autores) {
        return false;
    }

    @Override
    public boolean eliminar(int idAutores) {
        return false;
    }

    @Override
    public Libro buscar(String id) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean eliminar(String id) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
