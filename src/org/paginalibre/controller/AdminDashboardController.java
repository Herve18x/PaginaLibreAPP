package org.paginalibre.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import org.paginalibre.model.Usuario;

public class AdminDashboardController implements BaseDashboardController {

    @FXML private Label lblUsuario;
    @FXML private Label lblRol;

    @Override
    public void iniciarUsuario(Usuario usuario) {
        if (lblUsuario != null && usuario != null) {
            lblUsuario.setText(usuario.getNombre());
        }
        if (lblRol != null && usuario != null) {
            lblRol.setText("Rol: " + usuario.getRol());
        }
    }

    @FXML
    private void handleMenuAction(ActionEvent event) {
    }

    @FXML
    private void handleMousePressed(MouseEvent event) {
    }

    @FXML
    private void handleMouseReleased(MouseEvent event) {
    }
}