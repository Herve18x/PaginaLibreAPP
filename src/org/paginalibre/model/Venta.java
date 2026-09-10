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
    private int usuarioAutorizaDescuento;
    private Timestamp fechaAnulacion;
    private int usuarioAnulacion;
    private String motivoAnulacion;

    public Venta() {
    }

    public Venta(int idVenta, Timestamp fechaVenta, double subtotal, double descuento, double total, String estado, long cuiCliente, int idUsuario, int usuarioAutorizaDescuento, Timestamp fechaAnulacion, int usuarioAnulacion, String motivoAnulacion) {
        this.idVenta = idVenta;
        this.fechaVenta = fechaVenta;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.total = total;
        this.estado = estado;
        this.cuiCliente = cuiCliente;
        this.idUsuario = idUsuario;
        this.usuarioAutorizaDescuento = usuarioAutorizaDescuento;
        this.fechaAnulacion = fechaAnulacion;
        this.usuarioAnulacion = usuarioAnulacion;
        this.motivoAnulacion = motivoAnulacion;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public Timestamp getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(Timestamp fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public long getCuiCliente() {
        return cuiCliente;
    }

    public void setCuiCliente(long cuiCliente) {
        this.cuiCliente = cuiCliente;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getUsuarioAutorizaDescuento() {
        return usuarioAutorizaDescuento;
    }

    public void setUsuarioAutorizaDescuento(int usuarioAutorizaDescuento) {
        this.usuarioAutorizaDescuento = usuarioAutorizaDescuento;
    }

    public Timestamp getFechaAnulacion() {
        return fechaAnulacion;
    }

    public void setFechaAnulacion(Timestamp fechaAnulacion) {
        this.fechaAnulacion = fechaAnulacion;
    }

    public int getUsuarioAnulacion() {
        return usuarioAnulacion;
    }

    public void setUsuarioAnulacion(int usuarioAnulacion) {
        this.usuarioAnulacion = usuarioAnulacion;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public void setMotivoAnulacion(String motivoAnulacion) {
        this.motivoAnulacion = motivoAnulacion;
    }
}