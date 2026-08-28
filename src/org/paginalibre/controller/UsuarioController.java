package org.paginalibre.controller;

import org.paginalibre.dao.UsuarioDAO;
import org.paginalibre.dao.impl.UsuarioDAOImpl;
import org.paginalibre.model.Usuario;
import org.paginalibre.view.UsuarioConsoleView;

import java.util.List;

public class UsuarioController {

    private final UsuarioDAO dao;
    private final UsuarioConsoleView vista;

    public UsuarioController() {
        this.dao = new UsuarioDAOImpl();
        this.vista = new UsuarioConsoleView();
    }

    public void iniciar() {
        int opcion;
        do {
            opcion = vista.mostrarMenu();
            switch (opcion) {
                case 1 -> registrar();
                case 2 -> listar();
                case 3 -> buscar();
                case 4 -> actualizar();
                case 5 -> eliminar();
                case 6 -> vista.mostrarMensaje("Regresando al menú principal...");
                default -> vista.mostrarMensaje("Opción no válida.");
            }
        } while (opcion != 6);
    }

    private void registrar() {
        String username = vista.solicitarUsuario();
        String clave = vista.solicitarClave();
        String rol = vista.solicitarRol();
        String nombre = vista.solicitarTextoOpcional("Nombre", "");
        String apellido = vista.solicitarTextoOpcional("Apellido", "");
        String correo = vista.solicitarTextoOpcional("Correo", "");

        Usuario nuevo = new Usuario(0, username, clave, rol, nombre, apellido, correo, true);
        if (dao.insertar(nuevo)) {
            vista.mostrarMensaje(" Usuario registrado con éxito.");
        } else {
            vista.mostrarMensaje(" Error al registrar el usuario en la base de datos.");
        }
    }

    private void listar() {
        List<Usuario> lista = dao.listar();
        if (lista.isEmpty()) {
            vista.mostrarMensaje("No hay usuarios registrados.");
        } else {
            vista.desplegarLista(lista);
        }
    }

    private void buscar() {
        int id = vista.solicitarId();
        Usuario u = dao.buscar(id);
        if (u != null) {
            vista.desplegarUsuario(u);
        } else {
            vista.mostrarMensaje(" Usuario no encontrado con el ID: " + id);
        }
    }

    private void actualizar() {
        int id = vista.solicitarId();
        Usuario existente = dao.buscar(id);

        if (existente == null) {
            vista.mostrarMensaje(" Usuario no encontrado.");
            return;
        }

        // Limpiar buffer oculto de la consola
        vista.solicitarTextoOpcional("", "");

        String username = vista.solicitarTextoOpcional("Nuevo Username", existente.getUsername());
        if (!username.trim().isEmpty()) {
            existente.setUsername(username);
        }

        String clave = vista.solicitarTextoOpcional("Nueva Clave", existente.getPasswordHash());
        if (!clave.trim().isEmpty()) {
            existente.setPasswordHash(clave);
        }

        String rol = vista.solicitarTextoOpcional("Nuevo Rol", existente.getRol());
        if (!rol.trim().isEmpty()) {
            existente.setRol(rol);
        }

        String nombre = vista.solicitarTextoOpcional("Nuevo Nombre", existente.getNombre());
        if (!nombre.trim().isEmpty()) {
            existente.setNombre(nombre);
        }

        String apellido = vista.solicitarTextoOpcional("Nuevo Apellido", existente.getApellido());
        if (!apellido.trim().isEmpty()) {
            existente.setApellido(apellido);
        }

        String correo = vista.solicitarTextoOpcional("Nuevo Correo", existente.getCorreo());
        if (!correo.trim().isEmpty()) {
            existente.setCorreo(correo);
        }

        if (dao.actualizar(existente)) {
            vista.mostrarMensaje(" Usuario actualizado exitosamente.");
        } else {
            vista.mostrarMensaje(" Error al actualizar el registro.");
        }
    }

    private void eliminar() {
        int id = vista.solicitarId();
        if (dao.eliminar(id)) {
            vista.mostrarMensaje("Usuario desactivado o eliminado de la base de datos.");
        } else {
            vista.mostrarMensaje(" Error al eliminar el usuario.");
        }
    }
}