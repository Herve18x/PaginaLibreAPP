package org.paginalibre.model;

public class Usuario {

    private int usuarioId;
    private String usuario;
    private String clave;
    private String rol;

    public Usuario() {
    }

    public Usuario(int usuarioId, String usuario, String clave, String rol) {
        this.usuarioId = usuarioId;
        this.usuario = usuario;
        this.clave = clave;
        this.rol = rol;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
