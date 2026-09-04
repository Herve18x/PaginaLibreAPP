package org.paginalibre.system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage escenarioPrincipal;

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
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath));
        Parent root = loader.load();

        if (escenarioPrincipal != null) {
            escenarioPrincipal.setScene(new Scene(root));
            escenarioPrincipal.centerOnScreen();
        }
        
        return loader.getController();
    }


    public static Object cargarVistaEnContenedor(String fxmlPath, Pane contenedor) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath));
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
}