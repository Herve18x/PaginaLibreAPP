package org.paginalibre.system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage escenarioPrincipal;

    public static void main(String[] args) {
        // CORRECCIÓN: Inicia el ciclo de vida de la aplicación JavaFX
        launch(args);
    }

    @Override
    public void start(Stage escenarioPrincipal) throws Exception {
        Main.escenarioPrincipal = escenarioPrincipal;

        // Carga de la vista de inicio de sesión
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/paginalibre/view/login.fxml"));
        Parent raiz = loader.load();
        Scene escena = new Scene(raiz);

        escenarioPrincipal.setTitle("login");
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
        
        // Retorna el controlador de la vista cargada para manipularlo si es necesario
        return loader.getController();
    }

    public static Stage getEscenarioPrincipal() {
        return escenarioPrincipal;
    }
}