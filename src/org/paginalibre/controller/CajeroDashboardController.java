package org.paginalibre.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import org.paginalibre.model.Usuario;
import org.paginalibre.system.Main;

public class CajeroDashboardController implements BaseDashboardController {

    @FXML
    private Label lblUsuario;

    @FXML
    private Label lblRol;

    @FXML
    private ToggleButton btnNuevaVenta;

    @FXML
    private ToggleButton btnLibros;

    @FXML
    private ToggleButton btnVentas;
    @FXML
    private ToggleButton btnReembolsos;

    @Override
    public void iniciarUsuario(Usuario usuario) {
        if (usuario != null) {
            if (lblUsuario != null) {
                lblUsuario.setText(usuario.getNombre());
            }
            if (lblRol != null) {
                lblRol.setText(usuario.getRol());
            }
        }
    }

    @FXML
    private void abrirNuevaVenta(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/ventasventas.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirLibros(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/InventarioLibrosView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirHistorialVentas(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/HistorialVentasView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cerrarSesion(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/login.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    @FXML
    private void abrirReembolsos(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/ReembolsosView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
