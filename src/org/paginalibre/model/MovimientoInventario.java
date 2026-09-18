package org.paginalibre.model;

import java.sql.Timestamp;

public class MovimientoInventario {

    private int idMovimiento;
    private String isbn;
    private int idTipoMovimiento;
    private int idUsuario;
    private int cantidad;
    private String motivo;
    private Timestamp fechaMovimiento;

    public MovimientoInventario() {
    }

    public MovimientoInventario(int idMovimiento, String isbn, int idTipoMovimiento, int idUsuario, int cantidad, String motivo, Timestamp fechaMovimiento) {
        this.idMovimiento = idMovimiento;
        this.isbn = isbn;
        this.idTipoMovimiento = idTipoMovimiento;
        this.idUsuario = idUsuario;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.fechaMovimiento = fechaMovimiento;
    }

    // Constructores auxiliares
    public MovimientoInventario(String isbn, int idTipoMovimiento, int idUsuario, int cantidad, String motivo) {
        this.isbn = isbn;
        this.idTipoMovimiento = idTipoMovimiento;
        this.idUsuario = idUsuario;
        this.cantidad = cantidad;
        this.motivo = motivo;
    }

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getIdTipoMovimiento() {
        return idTipoMovimiento;
    }

    public void setIdTipoMovimiento(int idTipoMovimiento) {
        this.idTipoMovimiento = idTipoMovimiento;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Timestamp getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(Timestamp fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }
}
