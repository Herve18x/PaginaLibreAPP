package org.paginalibre.system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.paginalibre.model.Usuario;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;

public class Main extends Application {

private static Stage escenarioPrincipal;
private static Usuario usuarioSesion;
private static String vistaActual;
private static final Deque<String> historialVistas = new ArrayDeque<>();

@Override
public void start(Stage escenarioPrincipal) throws Exception {
    Main.escenarioPrincipal = escenarioPrincipal;
    historialVistas.clear();
    usuarioSesion = null;
    cargarVistaInicial();
    escenarioPrincipal.show();
}

private void cargarVistaInicial() throws Exception {
    FXMLLoader loader = new FXMLLoader(
            Main.class.getResource("/org/paginalibre/view/login.fxml")
    );

    Parent raiz = loader.load();

    vistaActual = "/org/paginalibre/view/login.fxml";

    escenarioPrincipal.setTitle("Pagina Libre - Login");
    escenarioPrincipal.setScene(new Scene(raiz));
    escenarioPrincipal.centerOnScreen();
}

public static void main(String[] args) {
    launch(args);
}

public static Object cambiarVista(String fxmlPath) throws Exception {
    String ruta = normalizarRuta(fxmlPath);

    URL location = Main.class.getResource(ruta);

    if (location == null) {
        throw new IllegalArgumentException(
                "No se encontró el archivo FXML: " + ruta
        );
    }

    FXMLLoader loader = new FXMLLoader(location);
    Parent contenido = loader.load();

    if (escenarioPrincipal == null) {
        return loader.getController();
    }

    if (ruta.equals("/org/paginalibre/view/login.fxml")) {
        historialVistas.clear();
    } else if (vistaActual != null
            && !vistaActual.equals(ruta)
            && !vistaActual.equals("/org/paginalibre/view/login.fxml")) {
        historialVistas.push(vistaActual);
    }

    vistaActual = ruta;
    mostrarVista(contenido, ruta);

    return loader.getController();
}

private static String normalizarRuta(String fxmlPath) {
    if (fxmlPath == null || fxmlPath.trim().isEmpty()) {
        throw new IllegalArgumentException("La ruta FXML no puede estar vacía.");
    }

    String ruta = fxmlPath.trim();

    if (!ruta.startsWith("/")) {
        ruta = "/org/paginalibre/view/" + ruta;
    }

    if (ruta.equals("/org/paginalibre/view/Libro.fxml")) {
        ruta = "/org/paginalibre/view/libro.fxml";
    }

    if (ruta.equals("/org/paginalibre/view/LibroView.fxml")) {
        ruta = "/org/paginalibre/view/libro.fxml";
    }

    return ruta;
}

private static void mostrarVista(Parent contenido, String fxmlPath) {
    if (escenarioPrincipal == null) {
        return;
    }

    StackPane raiz = new StackPane();
    raiz.getChildren().add(contenido);

    if (!fxmlPath.equals("/org/paginalibre/view/login.fxml") && !esDashboard(fxmlPath)) {
        ocultarBotonesRegreso(contenido);

        Button btnRegresar = new Button("<- Volver");
        btnRegresar.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #333;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: #aaa;" +
                "-fx-cursor: hand;"
        );

        btnRegresar.setOnAction(event -> {
            try {
                regresarAnterior();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        StackPane.setAlignment(
                btnRegresar,
                javafx.geometry.Pos.TOP_RIGHT
        );

        StackPane.setMargin(
                btnRegresar,
                new javafx.geometry.Insets(15, 15, 0, 0)
        );

        raiz.getChildren().add(btnRegresar);
    }

    Scene escena = escenarioPrincipal.getScene();

    if (escena == null) {
        escenarioPrincipal.setScene(new Scene(raiz));
    } else {
        escena.setRoot(raiz);
    }

    escenarioPrincipal.sizeToScene();
    escenarioPrincipal.centerOnScreen();
}

private static void ocultarBotonesRegreso(Node node) {
    if (node instanceof Button) {
        Button button = (Button) node;
        String texto = button.getText();

        if (texto != null) {
            String t = texto.toLowerCase();

            if (t.contains("regresar")
                    || t.contains("volver")
                    || t.contains("atrás")
                    || t.contains("atras")) {
                button.setVisible(false);
                button.setManaged(false);
                return;
            }
        }
    }

    if (node instanceof javafx.scene.Parent) {
        for (Node hijo :
                ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
            ocultarBotonesRegreso(hijo);
        }
    }
}

public static Object cargarVistaEnContenedor(
        String fxmlPath,
        Pane contenedor) throws Exception {

    if (contenedor == null) {
        throw new IllegalArgumentException(
                "El contenedor no puede ser null."
        );
    }

    String ruta = normalizarRuta(fxmlPath);

    URL location = Main.class.getResource(ruta);

    if (location == null) {
        throw new IllegalArgumentException(
                "No se encontró el archivo FXML: " + ruta
        );
    }

    FXMLLoader loader = new FXMLLoader(location);
    Node vista = loader.load();

    contenedor.getChildren().clear();
    contenedor.getChildren().add(vista);

    if (vista instanceof javafx.scene.layout.Region) {
        javafx.scene.layout.Region region =
                (javafx.scene.layout.Region) vista;

        region.setMaxWidth(Double.MAX_VALUE);
        region.setMaxHeight(Double.MAX_VALUE);

        if (contenedor instanceof javafx.scene.layout.AnchorPane) {
            javafx.scene.layout.AnchorPane.setTopAnchor(vista, 0.0);
            javafx.scene.layout.AnchorPane.setBottomAnchor(vista, 0.0);
            javafx.scene.layout.AnchorPane.setLeftAnchor(vista, 0.0);
            javafx.scene.layout.AnchorPane.setRightAnchor(vista, 0.0);
        }
    }

    return loader.getController();
}

public static void regresarAnterior() throws Exception {
    if (escenarioPrincipal == null) {
        return;
    }

    if (!historialVistas.isEmpty()) {
        String anterior = historialVistas.pop();

        URL location = Main.class.getResource(anterior);

        if (location != null) {
            FXMLLoader loader = new FXMLLoader(location);
            Parent contenido = loader.load();

            vistaActual = anterior;
            mostrarVista(contenido, anterior);
            return;
        }
    }

    if (usuarioSesion != null
            && vistaActual != null
            && !esDashboard(vistaActual)) {
        regresarDashboard();
    }
}

private static boolean esDashboard(String ruta) {
    return ruta.endsWith("DashboardView.fxml");
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

public static void regresarDashboard() throws Exception {
    if (usuarioSesion == null) {
        cambiarVista("/org/paginalibre/view/login.fxml");
        return;
    }

    String rol = usuarioSesion.getRol() == null
            ? ""
            : usuarioSesion.getRol().trim().toLowerCase();

    switch (rol) {
        case "admin":
            cambiarVista(
                    "/org/paginalibre/view/AdminDashboardView.fxml"
            );
            break;

        case "cajero":
            cambiarVista(
                    "/org/paginalibre/view/CajeroDashboardView.fxml"
            );
            break;

        case "bodega":
        case "bodegacajero":
            cambiarVista(
                    "/org/paginalibre/view/BodegaDashboardView.fxml"
            );
            break;

        case "jefecaja":
            cambiarVista(
                    "/org/paginalibre/view/JefeCajaDashboardView.fxml"
            );
            break;

        case "jefebodega":
            cambiarVista(
                    "/org/paginalibre/view/JefeBodegaDashboardView.fxml"
            );
            break;

        default:
            cambiarVista("/org/paginalibre/view/login.fxml");
            break;
    }
}

public static void cerrarSesion() {
    usuarioSesion = null;
    historialVistas.clear();
    vistaActual = "/org/paginalibre/view/login.fxml";
}

}
