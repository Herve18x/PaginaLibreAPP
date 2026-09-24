package org.paginalibre.controller;

import java.net.URL;
import java.sql.Timestamp;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.paginalibre.dao.LibroDAO;
import org.paginalibre.dao.MovimientoInventarioDAO;
import org.paginalibre.dao.impl.LibroDAOImpl;
import org.paginalibre.dao.impl.MovimientoInventarioDAOImpl;
import org.paginalibre.model.Libro;
import org.paginalibre.model.MovimientoInventario;
import org.paginalibre.model.Usuario;
import org.paginalibre.system.Main;

public class IngresoInventarioController implements Initializable {

    @FXML
    private TextField txtIsbn;

    @FXML
    private TextField txtTituloLibro;

    @FXML
    private TextField txtCantidad;

    @FXML
    private TextArea txtMotivo;

    @FXML
    private TableView<MovimientoInventario> tblMovimientos;

    @FXML
    private TableColumn<MovimientoInventario, Integer> colId;

    @FXML
    private TableColumn<MovimientoInventario, String> colIsbn;

    @FXML
    private TableColumn<MovimientoInventario, Integer> colCantidad;

    @FXML
    private TableColumn<MovimientoInventario, Timestamp> colFecha;

    @FXML
    private TableColumn<MovimientoInventario, String> colMotivo;

    private MovimientoInventarioDAO movimientoDAO;
    private LibroDAO libroDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        movimientoDAO = new MovimientoInventarioDAOImpl();
        libroDAO = new LibroDAOImpl();

        colId.setCellValueFactory(new PropertyValueFactory<>("idMovimiento"));
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaMovimiento"));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));

        cargarHistorial();
    }

    @FXML
    private void handleRegresar(ActionEvent event) {
        try {
            Main.regresarAnterior();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBuscarLibro() {
        String isbn = txtIsbn.getText().trim();

        if (isbn.isEmpty()) {
            txtTituloLibro.setText("");
            return;
        }

        Libro libro = libroDAO.buscar(isbn);

        if (libro != null) {
            txtTituloLibro.setText(libro.getTitulo());
        } else {
            txtTituloLibro.setText("Libro no encontrado");
        }
    }

    @FXML
    private void handleGuardarIngreso() {
        String isbn = txtIsbn.getText().trim();
        String cantidadStr = txtCantidad.getText().trim();
        String motivo = txtMotivo.getText().trim();

        if (isbn.isEmpty() || cantidadStr.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Advertencia", "Ingrese el ISBN y la cantidad.");
            return;
        }

        Usuario usuarioSesion = Main.getUsuarioSesion();

        if (usuarioSesion == null || usuarioSesion.getId() <= 0) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se encontró el usuario de la sesión actual.");
            return;
        }

        if (libroDAO.buscar(isbn) == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "El libro indicado no existe.");
            return;
        }

        try {
            int cantidad = Integer.parseInt(cantidadStr);

            if (cantidad <= 0) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "La cantidad debe ser mayor a 0.");
                return;
            }

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setIsbn(isbn);
            movimiento.setIdTipoMovimiento(1);
            movimiento.setIdUsuario(usuarioSesion.getId());
            movimiento.setCantidad(cantidad);
            movimiento.setMotivo(motivo);

            if (movimientoDAO.insertar(movimiento)) {
                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Éxito",
                        "Movimiento de inventario registrado correctamente."
                );

                handleLimpiar();
                cargarHistorial();
            } else {
                mostrarAlerta(
                        Alert.AlertType.ERROR,
                        "Error",
                        "No se pudo registrar el movimiento en la base de datos."
                );
            }

        } catch (NumberFormatException e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "La cantidad debe ser un número entero válido."
            );
        }
    }

    @FXML
    private void handleLimpiar() {
        txtIsbn.clear();
        txtTituloLibro.clear();
        txtCantidad.clear();
        txtMotivo.clear();
    }

    private void cargarHistorial() {
        ObservableList<MovimientoInventario> listaMovimientos
                = FXCollections.observableArrayList(movimientoDAO.listar());

        tblMovimientos.setItems(listaMovimientos);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
