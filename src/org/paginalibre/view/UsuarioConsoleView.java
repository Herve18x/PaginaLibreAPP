package org.paginalibre.view;

import java.util.List;
import java.util.Scanner;
import org.paginalibre.model.Usuario;

public class UsuarioConsoleView {

    private final Scanner scanner;

    public UsuarioConsoleView() {
        this.scanner = new Scanner(System.in);
    }

    public int mostrarMenu() {
        System.out.println("\n=== MENÚ GESTIÓN DE USUARIOS ===");
        System.out.println("1. Registrar Usuario");
        System.out.println("2. Listar Usuarios");
        System.out.println("3. Buscar Usuario por ID");
        System.out.println("4. Actualizar Usuario");
        System.out.println("5. Eliminar (Desactivar) Usuario");
        System.out.println("6. Volver al menú principal");
        System.out.print("Seleccione una opción: ");
        
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String solicitarUsuario() {
        System.out.print("Ingrese el nombre de usuario (username): ");
        return scanner.nextLine();
    }

    public String solicitarClave() {
        System.out.print("Ingrese la contraseña: ");
        return scanner.nextLine();
    }

    public String solicitarRol() {
        System.out.print("Ingrese el rol (admin, bodega, cajero): ");
        return scanner.nextLine();
    }

    public int solicitarId() {
        System.out.print("Ingrese el ID del usuario: ");
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String solicitarTextoOpcional(String mensaje, String valorActual) {
        if (mensaje.isEmpty()) {
            return ""; // Para limpiar buffer si se requiere
        }
        System.out.print(mensaje + " [" + valorActual + "]: ");
        return scanner.nextLine();
    }

    public void desplegarLista(List<Usuario> usuarios) {
        System.out.println("\n--- LISTA DE USUARIOS ---");
        System.out.printf("%-5s | %-15s | %-10s | %-20s | %-25s | %-8s%n", 
                "ID", "USERNAME", "ROL", "NOMBRE COMPLETO", "CORREO", "ACTIVO");
        System.out.println("---------------------------------------------------------------------------------------------------");
        for (Usuario u : usuarios) {
            String nombreCompleto = (u.getNombre() != null ? u.getNombre() : "") + " " + (u.getApellido() != null ? u.getApellido() : "");
            System.out.printf("%-5d | %-15s | %-10s | %-20s | %-25s | %-8s%n", 
                u.getId(), 
                u.getUsername(), 
                u.getRol(), 
                nombreCompleto.trim(), 
                u.getCorreo() != null ? u.getCorreo() : "N/A", 
                u.isActivo() ? "Sí" : "No");
        }
    }

    public void desplegarUsuario(Usuario u) {
        System.out.println("\n--- DETALLE DE USUARIO ---");
        System.out.println("ID: " + u.getId());
        System.out.println("Username: " + u.getUsername());
        System.out.println("Rol: " + u.getRol());
        System.out.println("Nombre: " + u.getNombre());
        System.out.println("Apellido: " + u.getApellido());
        System.out.println("Correo: " + u.getCorreo());
        System.out.println("Estado Activo: " + (u.isActivo() ? "Sí" : "No"));
    }

    public void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }
}
