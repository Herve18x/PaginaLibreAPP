package org.paginalibre.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.paginalibre.model.Usuario;
import org.paginalibre.system.Main;

public class AdminDashboardController implements BaseDashboardController {

    @FXML
    private Label lblUsuario;

    @FXML
    private Label lblRol;

    @Override
    public void iniciarUsuario(Usuario usuario) {
        if (usuario != null) {
            lblUsuario.setText(usuario.getNombre() + " " + usuario.getApellido());
            lblRol.setText("Rol: " + usuario.getRol());
        }
    }

    private void abrirVista(String ruta, String titulo) {
        try {
            Main.cambiarVista(ruta);

            Stage stage = Main.getEscenarioPrincipal();

            if (stage != null) {
                stage.setTitle(titulo + " - Página Libre");
            }

        } catch (Exception e) {
            mostrarAlerta(
                    "Error",
                    "No se pudo abrir el módulo: " + e.getMessage(),
                    Alert.AlertType.ERROR
            );
        }
    }

    @FXML
    private void abrirUsuarios(ActionEvent event) {
        abrirVista(
                "/org/paginalibre/view/UsuarioView.fxml",
                "Gestión de Usuarios"
        );
    }

    @FXML
    private void abrirInventario(ActionEvent event) {
        abrirVista(
                "/org/paginalibre/view/InventarioBodegaView.fxml",
                "Gestión de Inventario"
        );
    }

    @FXML
    private void abrirReportes(ActionEvent event) {
        abrirVista(
                "/org/paginalibre/view/ReportesBodegaView.fxml",
                "Reportes y Movimientos"
        );
    }

    @FXML
    private void abrirNuevaVenta(ActionEvent event) {
        abrirVista(
                "/org/paginalibre/view/ventasventas.fxml",
                "Nueva Venta"
        );
    }

    @FXML
    private void abrirVentas(ActionEvent event) {
        abrirVista(
                "/org/paginalibre/view/HistorialVentasView.fxml",
                "Historial de Ventas"
        );
    }

    @FXML
    private void abrirReembolsos(ActionEvent event) {
        abrirVista(
                "/org/paginalibre/view/ReembolsosView.fxml",
                "Reembolsos"
        );
    }

    @FXML
    private void abrirLibros(ActionEvent event) {
        abrirVista(
                "/org/paginalibre/view/InventarioLibrosView.fxml",
                "Catálogo de Libros"
        );
    }

    @FXML
    private void abrirBodega(ActionEvent event) {
        abrirVista(
                "/org/paginalibre/view/IngresoInventario.fxml",
                "Ingreso de Inventario"
        );
    }

    @FXML
    private void abrirSalidas(ActionEvent event) {
        abrirVista(
                "/org/paginalibre/view/SalidaInventarioView.fxml",
                "Salidas de Inventario"
        );
    }

    @FXML
    private void abrirNuevoLibro(ActionEvent event) {
        abrirVista(
                "/org/paginalibre/view/NuevoLibroView.fxml",
                "Nuevo Libro"
        );
    }

    @FXML
    private void abrirClientes(ActionEvent event) {
        abrirVista(
                "/org/paginalibre/view/SeleccionarClienteView.fxml",
                "Clientes"
        );
    }

    @FXML
    private void mostrarGestionLibros(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/Libro.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    @FXML
    private void cerrarSesion(ActionEvent event) {
        Main.cerrarSesion();

        try {
            Main.cambiarVista("/org/paginalibre/view/login.fxml");
        } catch (Exception e) {
            mostrarAlerta(
                    "Error",
                    "No se pudo cerrar sesión: " + e.getMessage(),
                    Alert.AlertType.ERROR
            );
        }
    }

    @FXML
    private void handleMenuAction(ActionEvent event) {
    }

    private void mostrarAlerta(
            String titulo,
            String contenido,
            Alert.AlertType tipo
    ) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}