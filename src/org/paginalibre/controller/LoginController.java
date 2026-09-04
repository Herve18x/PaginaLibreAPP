package org.paginalibre.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.paginalibre.dao.UsuarioDAO;
import org.paginalibre.dao.impl.UsuarioDAOImpl;
import org.paginalibre.model.Usuario;
import org.paginalibre.util.SecurityUtil;
import org.paginalibre.system.Main;

public class LoginController implements Initializable {

    @FXML
    private TextField txtUsuario;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private PasswordField txtConfirmarPassword;
    @FXML
    private Button btnIniciarSesion;
    @FXML
    private Label lblMensaje;

    private UsuarioDAO usuarioDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usuarioDAO = new UsuarioDAOImpl();
        if (lblMensaje != null) {
            lblMensaje.setText("");
        }
    }

    private void handleNoDisponible() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Módulo no disponible");
        alert.setHeaderText(null);
        alert.setContentText("Este módulo no está disponible aún.");
        alert.showAndWait();
    }

    @FXML
    public void eventoInicioSesion(ActionEvent evento) {
        String usuarioIngresado = txtUsuario != null ? txtUsuario.getText().trim() : "";
        String passwordIngresada = txtPassword != null ? txtPassword.getText().trim() : "";
        String confirmarPasswordIngresada = txtConfirmarPassword != null ? txtConfirmarPassword.getText().trim() : "";

        // 1. Validar que no haya campos vacíos
        if (usuarioIngresado.isEmpty() || passwordIngresada.isEmpty() || confirmarPasswordIngresada.isEmpty()) {
            if (lblMensaje != null) {
                lblMensaje.setText("Por favor, complete todos los campos.");
            }
            return;
        }

        // 2. Validar que la contraseña y la confirmación coincidan
        if (!passwordIngresada.equals(confirmarPasswordIngresada)) {
            if (lblMensaje != null) {
                lblMensaje.setText("Las contraseñas no coinciden.");
            }
            return;
        }

        String passwordHash = SecurityUtil.hashSHA256(passwordIngresada);
        Usuario usuarioEncontrado = null;

        List<Usuario> usuarios = usuarioDAO.listar();

        if (usuarios != null) {
            for (Usuario u : usuarios) {
                if (u.getUsername() != null && u.getUsername().equalsIgnoreCase(usuarioIngresado)) {
                    if (u.getPasswordHash() != null && 
                       (u.getPasswordHash().equals(passwordHash) || u.getPasswordHash().equals(passwordIngresada))) {
                        usuarioEncontrado = u;
                        break;
                    }
                }
            }
        }

        if (usuarioEncontrado != null) {
            if (!usuarioEncontrado.isActivo()) {
                if (lblMensaje != null) {
                    lblMensaje.setText("El usuario se encuentra inactivo.");
                }
                return;
            }

            if (lblMensaje != null) {
                lblMensaje.setText("Inicio correcto");
            }
            abrirDashboard(usuarioEncontrado);
        } else {
            if (lblMensaje != null) {
                lblMensaje.setText("Usuario o contraseña incorrectos");
            }
        }
    }

    private void abrirDashboard(Usuario usuario) {
        if (usuario == null || usuario.getRol() == null) {
            if (lblMensaje != null) {
                lblMensaje.setText("Error: Rol de usuario no válido.");
            }
            return;
        }

        String rutaFXML;
        String tituloDashboard;
        String rol = usuario.getRol().trim().toLowerCase();

        switch (rol) {
            case "admin":
                rutaFXML = "/org/paginalibre/view/AdminDashboardView.fxml";
                tituloDashboard = "Panel de Administración - Página Viva";
                break;

            case "jefecaja":
                rutaFXML = "/org/paginalibre/view/JefeCajaDashboardView.fxml";
                tituloDashboard = "Panel de Jefatura de Caja - Página Viva";
                break;

            case "cajero":
                rutaFXML = "/org/paginalibre/view/CajeroDashboardView.fxml";
                tituloDashboard = "Panel de Caja y Ventas - Página Viva";
                break;

            case "jefebodega":
                rutaFXML = "/org/paginalibre/view/JefeBodegaDashboardView.fxml";
                tituloDashboard = "Panel de Jefatura de Bodega - Página Viva";
                break;

            case "bodega":
            case "bodegacajero":
                rutaFXML = "/org/paginalibre/view/BodegaDashboardView.fxml";
                tituloDashboard = "Panel de Bodega e Inventario - Página Viva";
                break;

            default:
                if (lblMensaje != null) {
                    lblMensaje.setText("Rol no autorizado: " + usuario.getRol());
                }
                return;
        }

        try {
            FXMLLoader cargadorFXML = new FXMLLoader(getClass().getResource(rutaFXML));
            Parent raiz = cargadorFXML.load();
            Object controlador = cargadorFXML.getController();

            if (controlador instanceof BaseDashboardController) {
                ((BaseDashboardController) controlador).iniciarUsuario(usuario);
            }

            Stage escenarioPrincipal = Main.getEscenarioPrincipal();
            escenarioPrincipal.setScene(new Scene(raiz));
            escenarioPrincipal.setTitle(tituloDashboard);
            escenarioPrincipal.centerOnScreen();

        } catch (Exception e) {
            System.err.println("Error al cargar la vista: " + rutaFXML + " - " + e.getMessage());
            e.printStackTrace();
            if (lblMensaje != null) {
                lblMensaje.setText("Error interno al abrir la vista.");
            }
        }
    }
}