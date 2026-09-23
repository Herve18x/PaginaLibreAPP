package org.paginalibre.model;
public class Proveedor {
    private int id; private String nombre, telefono, direccion, correo;
    public Proveedor() {}
    public Proveedor(int id,String nombre,String telefono,String direccion,String correo){this.id=id;this.nombre=nombre;this.telefono=telefono;this.direccion=direccion;this.correo=correo;}
    public int getId(){return id;} public void setId(int id){this.id=id;}
    public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;}
    public String getTelefono(){return telefono;} public void setTelefono(String v){telefono=v;}
    public String getDireccion(){return direccion;} public void setDireccion(String v){direccion=v;}
    public String getCorreo(){return correo;} public void setCorreo(String v){correo=v;}
    @Override public String toString(){return nombre;}
}
