package org.paginalibre.model;

import java.sql.Timestamp;

public class Venta {
    private int idVenta;
    private Timestamp fechaVenta;
    private double subtotal;
    private double descuento;
    private double total;
    private String estado;
    private long cuiCliente;
    private int idUsuario;

    public Venta() {}

    public Venta(int idVenta, Timestamp fechaVenta, double subtotal, double descuento, double total, String estado, long cuiCliente, int idUsuario) {
        this.idVenta = idVenta;
        this.fechaVenta = fechaVenta;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.total = total;
        this.estado = estado;
        this.cuiCliente = cuiCliente;
        this.idUsuario = idUsuario;
    }

    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }

    public Timestamp getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(Timestamp fechaVenta) { this.fechaVenta = fechaVenta; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getDescuento() { return descuento; }
    public void setDescuento(double descuento) { this.descuento = descuento; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public long getCuiCliente() { return cuiCliente; }
    public void setCuiCliente(long cuiCliente) { this.cuiCliente = cuiCliente; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
}
