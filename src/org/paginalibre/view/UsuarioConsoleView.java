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
        System.out.println("5. Eliminar Usuario");
        System.out.println("6. Volver al menú principal");
        System.out.print("Seleccione una opción: ");
        
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String solicitarUsuario() {
        System.out.print("Ingrese el nombre de usuario: ");
        return scanner.nextLine();
    }

    public String solicitarClave() {
        System.out.print("Ingrese la contraseña: ");
        return scanner.nextLine();
    }

    public String solicitarRol() {
        System.out.print("Ingrese el rol (ej. Admin, Cliente): ");
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
        System.out.printf("%-5s | %-20s | %-15s | %-10s%n", "ID", "USUARIO", "CLAVE", "ROL");
        System.out.println("---------------------------------------------------------");
        for (Usuario u : usuarios) {
            System.out.printf("%-5d | %-20s | %-15s | %-10s%n", 
                    u.getUsuarioId(), u.getUsuario(), u.getClave(), u.getRol());
        }
    }

    public void desplegarUsuario(Usuario u) {
        System.out.println("\n--- DETALLE DE USUARIO ---");
        System.out.println("ID: " + u.getUsuarioId());
        System.out.println("Usuario: " + u.getUsuario());
        System.out.println("Clave: " + u.getClave());
        System.out.println("Rol: " + u.getRol());
    }

    public void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }
}