package org.paginalibre.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.paginalibre.model.Usuario;
import org.paginalibre.system.Main;

public class BodegaDashboardController implements BaseDashboardController {

    @FXML
    private Label lblUsuario;

    @FXML
    private Label lblRol;

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
    private void abrirInventario(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/InventarioBodegaView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirMovimientos(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/IngresoInventario.fxml");
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
}