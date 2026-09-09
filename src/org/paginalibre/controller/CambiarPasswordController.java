package org.paginalibre.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.paginalibre.dao.UsuarioDAO;
import org.paginalibre.dao.impl.UsuarioDAOImpl;
import org.paginalibre.model.Usuario;
import org.paginalibre.util.SecurityUtil;

public class CambiarPasswordController {

    @FXML private PasswordField txtPasswordActual;
    @FXML private PasswordField txtNuevaPassword;
    @FXML private PasswordField txtConfirmarPassword;
    @FXML private Label lblMensaje;
    @FXML private Button btnGuardar;

    private Usuario usuarioActual;
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    @FXML
    private void eventoGuardar() {
        String actual = txtPasswordActual.getText().trim();
        String nueva = txtNuevaPassword.getText().trim();
        String confirmacion = txtConfirmarPassword.getText().trim();

        if (actual.isEmpty() || nueva.isEmpty() || confirmacion.isEmpty()) {
            lblMensaje.setText("Debe llenar todos los campos.");
            return;
        }

        // Validación de la contraseña actual del usuario
        String hashActual = SecurityUtil.hashSHA256(actual);
        if (usuarioActual.getPasswordHash() != null && 
           !usuarioActual.getPasswordHash().equals(hashActual) && 
           !usuarioActual.getPasswordHash().equals(actual)) {
            lblMensaje.setText("La contraseña actual es incorrecta.");
            return;
        }

        if (!nueva.equals(confirmacion)) {
            lblMensaje.setText("Las nuevas contraseñas no coinciden.");
            return;
        }

        // Se asigna la nueva contraseña en texto plano para que el DAO la encripté UNA SOLA VEZ
        usuarioActual.setPasswordHash(nueva);

        if (usuarioDAO.actualizar(usuarioActual)) {
            cerrarVentana();
        } else {
            lblMensaje.setText("Error al actualizar la contraseña en la BD.");
        }
    }

    @FXML
    private void eventoCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}