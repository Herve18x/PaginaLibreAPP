package org.paginalibre.system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import static javafx.application.Application.launch;
import org.paginalibre.model.Usuario;

public class Main extends Application {

    private static Stage escenarioPrincipal;
    private static Usuario usuarioSesion;
    private static String vistaActual;
    private static final Deque<String> historialVistas = new ArrayDeque<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage escenarioPrincipal) throws Exception {
        Main.escenarioPrincipal = escenarioPrincipal;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/paginalibre/view/login.fxml"));
        Parent raiz = loader.load();
        Scene escena = new Scene(raiz);

        vistaActual = "/org/paginalibre/view/login.fxml";
        historialVistas.clear();
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
        Parent contenido = loader.load();

        if (escenarioPrincipal != null) {
            if (fxmlPath.equals("/org/paginalibre/view/login.fxml")) {
                historialVistas.clear();
            } else if (vistaActual != null
                    && !vistaActual.equals(fxmlPath)
                    && !vistaActual.equals("/org/paginalibre/view/login.fxml")) {
                historialVistas.push(vistaActual);
            }

            vistaActual = fxmlPath;
            mostrarVista(contenido, fxmlPath);
        }

        return loader.getController();
    }

    private static void mostrarVista(Parent contenido, String fxmlPath) {
        StackPane raiz = new StackPane(contenido);

        if (!fxmlPath.equals("/org/paginalibre/view/login.fxml")) {
            ocultarBotonesRegreso(contenido);

            Button btnRegresar = new Button("← Volver");
            btnRegresar.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-text-fill: #333;" +
                    "-fx-font-weight: bold;" +
                    "-fx-border-color: #aaa;" +
                    "-fx-cursor: hand;"
            );

            btnRegresar.setOnAction(e -> {
                try {
                    regresarAnterior();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            StackPane.setAlignment(btnRegresar, javafx.geometry.Pos.TOP_RIGHT);
            StackPane.setMargin(
                    btnRegresar,
                    new javafx.geometry.Insets(15, 15, 0, 0)
            );

            raiz.getChildren().add(btnRegresar);
        }

        escenarioPrincipal.setScene(new Scene(raiz));
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
            for (Node hijo : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                ocultarBotonesRegreso(hijo);
            }
        }
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

    public static void regresarAnterior() throws Exception {
        if (escenarioPrincipal == null) {
            return;
        }

        if (!historialVistas.isEmpty()) {
            String anterior = historialVistas.pop();

            FXMLLoader loader = new FXMLLoader(Main.class.getResource(anterior));
            Parent contenido = loader.load();

            vistaActual = anterior;
            mostrarVista(contenido, anterior);
            return;
        }

        if (usuarioSesion != null
                && vistaActual != null
                && !vistaActual.endsWith("DashboardView.fxml")) {
            regresarDashboard();
        }
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
                cambiarVista("/org/paginalibre/view/AdminDashboardView.fxml");
                break;

            case "cajero":
                cambiarVista("/org/paginalibre/view/CajeroDashboardView.fxml");
                break;

            case "bodega":
            case "bodegacajero":
                cambiarVista("/org/paginalibre/view/BodegaDashboardView.fxml");
                break;

            case "jefecaja":
                cambiarVista("/org/paginalibre/view/JefeCajaDashboardView.fxml");
                break;

            case "jefebodega":
                cambiarVista("/org/paginalibre/view/JefeBodegaDashboardView.fxml");
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