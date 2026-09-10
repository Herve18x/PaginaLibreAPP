package org.paginalibre.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.paginalibre.model.Usuario;
import org.paginalibre.system.Main;

public class AdminDashboardController implements BaseDashboardController {

    @FXML private Label lblUsuario;
    @FXML private Label lblRol;
    @FXML private StackPane contenidoDinamico;

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
private void mostrarGestionUsuarios(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/paginalibre/view/UsuarioView.fxml"));
        Parent root = loader.load();

        Stage mainStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        mainStage.hide();

        Stage usuariosStage = new Stage();
        usuariosStage.setTitle("Gestión de Usuarios - Página Libre");
        usuariosStage.setResizable(false);
        usuariosStage.setScene(new Scene(root));

        usuariosStage.setOnHidden(e -> mainStage.show());

        usuariosStage.show();

    } catch (Exception e) {
        System.err.println("Error al abrir la ventana de Usuarios: " + e.getMessage());
        e.printStackTrace();
    }
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
        try {
            Main.cambiarVista("/org/paginalibre/view/login.fxml");
        } catch (Exception e) {
            System.err.println("Error al cerrar sesión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void handleMenuAction(ActionEvent event) {}
    @FXML private void handleMousePressed(MouseEvent event) {}
    @FXML private void handleMouseReleased(MouseEvent event) {}
}