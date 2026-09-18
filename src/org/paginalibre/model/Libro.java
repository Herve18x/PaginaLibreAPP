package org.paginalibre.model;

import java.sql.Timestamp;
import java.time.LocalDate;

public class Libro {
    private String isbn;
    private String titulo;
    private LocalDate fechaPublicacion;
    private double precio;
    private int categoriaId;
    private String nombreCategoria;
    private String nitEditorial;
    private String nombreEditorial;
    private String autores;
    private int stockActual;
    private int stockMinimo;
    private boolean estado;
    private Timestamp fechaActualizacion;

    public Libro() {
    }

    public Libro(String isbn, String titulo, LocalDate fechaPublicacion, double precio,
                 int categoriaId, String nitEditorial, int stockActual, int stockMinimo,
                 boolean estado, Timestamp fechaActualizacion) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.fechaPublicacion = fechaPublicacion;
        this.precio = precio;
        this.categoriaId = categoriaId;
        this.nitEditorial = nitEditorial;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.estado = estado;
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public LocalDate getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDate fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getCategoriaId() { return categoriaId; }
    public void setCategoriaId(int categoriaId) { this.categoriaId = categoriaId; }

    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }

    public String getNitEditorial() { return nitEditorial; }
    public void setNitEditorial(String nitEditorial) { this.nitEditorial = nitEditorial; }

    public String getNombreEditorial() { return nombreEditorial; }
    public void setNombreEditorial(String nombreEditorial) { this.nombreEditorial = nombreEditorial; }

    public String getAutores() { return autores; }
    public void setAutores(String autores) { this.autores = autores; }

    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public boolean isEstado() { return estado; }
    public boolean isActivo() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }
    public void setActivo(boolean activo) { this.estado = activo; }

    public Timestamp getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Timestamp fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
