package org.paginalibre.model;

public class Cliente {
    private Long cui;
    private String nombre;
    private String apellido;
    private String correoElectronico;

    public Cliente() {
    }

    public Cliente(Long cui, String nombre, String apellido, String correoElectronico) {
        this.cui = cui;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correoElectronico = correoElectronico;
    }

    public Long getCui() {
        return cui;
    }

    public void setCui(Long cui) {
        this.cui = cui;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + cui + ")";
    }
}