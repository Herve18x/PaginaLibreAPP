package org.paginalibre.dao.impl;
 
import java.sql.Connection;

import java.sql.Date;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.sql.Timestamp;

import java.time.LocalDate;

import java.util.ArrayList;

import java.util.List;
 
import org.paginalibre.dao.AdminReportesDAO;

import org.paginalibre.dao.AdminReportesDAO.DashboardKPI;

import org.paginalibre.dao.AdminReportesDAO.VentaReporte;

import org.paginalibre.dao.AdminReportesDAO.LibroReporte;

import org.paginalibre.util.Conexion;
 
public class AdminReportesDAOImpl implements AdminReportesDAO {
 
    @Override

    public DashboardKPI obtenerKPI() {
 
        String sql =

                "SELECT " +

                "COALESCE((SELECT SUM(total) " +

                "FROM ventas WHERE estado = 'COMPLETADA'), 0), " +

                "(SELECT COUNT(*) FROM libros WHERE estado = 1), " +

                "(SELECT COUNT(*) FROM usuarios WHERE activo = 1)";
 
        try (

            Connection conexion = Conexion.getInstancia().conectar();

            PreparedStatement ps = conexion.prepareStatement(sql);

            ResultSet rs = ps.executeQuery()

        ) {
 
            if (rs.next()) {

                return new DashboardKPI(

                        rs.getDouble(1),

                        rs.getInt(2),

                        rs.getInt(3)

                );

            }
 
        } catch (SQLException e) {

            e.printStackTrace();

        }
 
        return new DashboardKPI(0, 0, 0);

    }
 
    @Override

    public List<VentaReporte> ventas(LocalDate desde, LocalDate hasta) {
 
        List<VentaReporte> resultado = new ArrayList<>();
 
        String sql =

                "SELECT " +

                "v.id_venta, " +

                "v.fecha_venta, " +

                "v.subtotal, " +

                "v.descuento, " +

                "v.total, " +

                "v.estado, " +

                "COALESCE(CONCAT(c.nombre_cliente, ' ', c.apellido_cliente), 'Consumidor Final'), " +

                "u.username " +

                "FROM ventas v " +

                "LEFT JOIN clientes c ON c.cui = v.cui_cliente " +

                "INNER JOIN usuarios u ON u.id = v.id_usuario " +

                "WHERE v.estado = 'COMPLETADA' " +
                "AND DATE(v.fecha_venta) BETWEEN ? AND ? " +

                "ORDER BY v.fecha_venta DESC";
 
        try (

            Connection conexion = Conexion.getInstancia().conectar();

            PreparedStatement ps = conexion.prepareStatement(sql)

        ) {
 
            ps.setDate(1, Date.valueOf(desde));

            ps.setDate(2, Date.valueOf(hasta));
 
            try (ResultSet rs = ps.executeQuery()) {
 
                while (rs.next()) {
 
                    Timestamp fecha = rs.getTimestamp("fecha_venta");
 
                    resultado.add(

                        new VentaReporte(

                            rs.getInt("id_venta"),

                            fecha,

                            rs.getDouble("subtotal"),

                            rs.getDouble("descuento"),

                            rs.getDouble("total"),

                            rs.getString("estado"),

                            rs.getString(7),

                            rs.getString("username")

                        )

                    );

                }

            }
 
        } catch (SQLException e) {

            e.printStackTrace();

        }
 
        return resultado;

    }
 
    @Override

    public List<LibroReporte> masVendidos(

            LocalDate desde,

            LocalDate hasta) {
 
        List<LibroReporte> resultado = new ArrayList<>();
 
        String sql =

                "SELECT " +

                "l.isbn, " +

                "l.titulo, " +

                "COALESCE(SUM(dv.cantidad), 0) AS cantidad, " +

                "l.stock_actual, " +

                "(l.stock_actual * l.precio) AS valor " +

                "FROM detalle_venta dv " +

                "INNER JOIN ventas v " +

                "ON v.id_venta = dv.id_venta " +

                "INNER JOIN libros l " +

                "ON l.isbn = dv.isbn " +

                "WHERE v.estado = 'COMPLETADA' " +

                "AND DATE(v.fecha_venta) BETWEEN ? AND ? " +

                "GROUP BY l.isbn, l.titulo, l.stock_actual, l.precio " +

                "ORDER BY cantidad DESC";
 
        try (

            Connection conexion = Conexion.getInstancia().conectar();

            PreparedStatement ps = conexion.prepareStatement(sql)

        ) {
 
            ps.setDate(1, Date.valueOf(desde));

            ps.setDate(2, Date.valueOf(hasta));
 
            try (ResultSet rs = ps.executeQuery()) {
 
                while (rs.next()) {
 
                    resultado.add(

                        new LibroReporte(

                            rs.getString("isbn"),

                            rs.getString("titulo"),

                            rs.getInt("cantidad"),

                            rs.getInt("stock_actual"),

                            rs.getDouble("valor")

                        )

                    );

                }

            }
 
        } catch (SQLException e) {

            e.printStackTrace();

        }
 
        return resultado;

    }
 
    @Override

    public List<LibroReporte> stockValorizado() {
 
        List<LibroReporte> resultado = new ArrayList<>();
 
        String sql =

                "SELECT " +

                "isbn, " +

                "titulo, " +

                "stock_actual, " +

                "(stock_actual * precio) AS valor " +

                "FROM libros " +

                "WHERE estado = 1 " +

                "ORDER BY valor DESC";
 
        try (

            Connection conexion = Conexion.getInstancia().conectar();

            PreparedStatement ps = conexion.prepareStatement(sql);

            ResultSet rs = ps.executeQuery()

        ) {
 
            while (rs.next()) {
 
                resultado.add(

                    new LibroReporte(

                        rs.getString("isbn"),

                        rs.getString("titulo"),

                        0,

                        rs.getInt("stock_actual"),

                        rs.getDouble("valor")

                    )

                );

            }
 
        } catch (SQLException e) {

            e.printStackTrace();

        }
 
        return resultado;

    }

}

 