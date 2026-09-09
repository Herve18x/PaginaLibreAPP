package org.paginalibre.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.paginalibre.model.Usuario;
import org.paginalibre.system.Main;

public class CajeroDashboardController implements BaseDashboardController {

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
    private void abrirVentas(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/ventasventas.fxml");
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
