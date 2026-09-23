package org.paginalibre.dao;
import java.time.LocalDate; import java.util.List;
public interface AdminReportesDAO {
    DashboardKPI obtenerKPI();
    List<VentaReporte> ventas(LocalDate desde, LocalDate hasta);
    List<LibroReporte> masVendidos(LocalDate desde, LocalDate hasta);
    List<LibroReporte> stockValorizado();
    record DashboardKPI(double ventasTotales,int libros,int usuariosActivos){}
    record VentaReporte(int id, java.sql.Timestamp fecha,double subtotal,double descuento,double total,String estado,String cliente,String cajero){}
    record LibroReporte(String isbn,String titulo,int cantidad,int stock,double valor){}
}
