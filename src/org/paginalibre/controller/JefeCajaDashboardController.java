package org.paginalibre.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.paginalibre.model.Usuario;

public class JefeCajaDashboardController implements BaseDashboardController {
    @FXML private Label lblUsuario;

    @Override
    public void iniciarUsuario(Usuario usuario) {
        if (lblUsuario != null) lblUsuario.setText(usuario.getNombre() + " (Jefe de Caja)");
    }
}