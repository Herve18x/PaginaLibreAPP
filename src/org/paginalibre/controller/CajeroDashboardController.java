package org.paginalibre.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.paginalibre.model.Usuario;

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
}