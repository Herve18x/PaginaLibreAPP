package org.paginalibre.model;

import java.time.LocalDate;
import java.sql.Timestamp;

/**
 * Modelo para representar la entidad Libro
 */
public class Libro {
    private String isbn;
    private String titulo;
    private LocalDate fechaPublicacion;
    private double precio;
    private int idCategoria;
    private String nombreCategoria; // Atributo para mostrar la categoría en el TableView
    private String nitEditorial;
    private String nombreEditorial;
    private String autores;
    private int stockActual;
    private int stockMinimo;
    private boolean activo; 
    private Timestamp fechaActualizacion;

    // Constructor vacío
    public Libro() {
    }

    // Constructor completo
    public Libro(String isbn, String titulo, LocalDate fechaPublicacion, double precio, int idCategoria, 
                 String nombreCategoria, String nitEditorial, int stockActual, int stockMinimo, 
                 boolean activo, Timestamp fechaActualizacion) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.fechaPublicacion = fechaPublicacion;
        this.precio = precio;
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
        this.nitEditorial = nitEditorial;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.activo = activo;
        this.fechaActualizacion = fechaActualizacion;
    }

    // Getters y Setters
    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDate fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getNombreEditorial() {
        return nombreEditorial;
    }

    public void setNombreEditorial(String nombreEditorial) {
        this.nombreEditorial = nombreEditorial;
    }

    public String getAutores() {
        return autores;
    }

    public void setAutores(String autores) {
        this.autores = autores;
    }

    public String getNitEditorial() {
        return nitEditorial;
    }

    public void setNitEditorial(String nitEditorial) {
        this.nitEditorial = nitEditorial;
    }

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Timestamp getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(Timestamp fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}