package org.paginalibre.system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.net.URL;
import org.paginalibre.model.Usuario;

public class Main extends Application {

    private static Stage escenarioPrincipal;
    private static Usuario usuarioSesion;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage escenarioPrincipal) throws Exception {
        Main.escenarioPrincipal = escenarioPrincipal;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/paginalibre/view/login.fxml"));
        Parent raiz = loader.load();
        Scene escena = new Scene(raiz);

        escenarioPrincipal.setTitle("Pagina Libre - Login");
        escenarioPrincipal.setScene(escena);
        escenarioPrincipal.show();
    }

    public static Object cambiarVista(String fxmlPath) throws Exception {
        if (!fxmlPath.startsWith("/")) {
            fxmlPath = "/org/paginalibre/view/" + fxmlPath;
        }

        URL location = Main.class.getResource(fxmlPath);
        if (location == null) {
            throw new IllegalArgumentException("No se encontró el archivo FXML en la ruta: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(location);
        Parent root = loader.load();

        if (escenarioPrincipal != null) {
            escenarioPrincipal.setScene(new Scene(root));
            escenarioPrincipal.centerOnScreen();
        }

        return loader.getController();
    }

    public static Object cargarVistaEnContenedor(String fxmlPath, Pane contenedor) throws Exception {
        if (!fxmlPath.startsWith("/")) {
            fxmlPath = "/org/paginalibre/view/" + fxmlPath;
        }

        URL location = Main.class.getResource(fxmlPath);
        if (location == null) {
            throw new IllegalArgumentException("No se encontró el archivo FXML en la ruta: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(location);
        Node vista = loader.load();

        contenedor.getChildren().clear();
        contenedor.getChildren().add(vista);

        if (contenedor instanceof javafx.scene.layout.AnchorPane) {
            javafx.scene.layout.AnchorPane.setTopAnchor(vista, 0.0);
            javafx.scene.layout.AnchorPane.setBottomAnchor(vista, 0.0);
            javafx.scene.layout.AnchorPane.setLeftAnchor(vista, 0.0);
            javafx.scene.layout.AnchorPane.setRightAnchor(vista, 0.0);
        }

        return loader.getController();
    }

    public static Stage getEscenarioPrincipal() {
        return escenarioPrincipal;
    }

    public static void establecerUsuarioSesion(Usuario usuario) {
        usuarioSesion = usuario;
    }

    public static Usuario getUsuarioSesion() {
        return usuarioSesion;
    }

    public static void cerrarSesion() {
        usuarioSesion = null;
    }
}