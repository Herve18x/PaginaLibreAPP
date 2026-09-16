package org.paginalibre.controller;

import java.net.URL;
import java.sql.Timestamp;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.paginalibre.dao.LibroDAO;
import org.paginalibre.dao.MovimientoInventarioDAO;
import org.paginalibre.dao.impl.LibroDAOImpl;
import org.paginalibre.dao.impl.MovimientoInventarioDAOImpl;
import org.paginalibre.model.Libro;
import org.paginalibre.model.MovimientoInventario;
import org.paginalibre.model.Usuario;
import org.paginalibre.system.Main;

public class IngresoInventarioController implements Initializable, BaseDashboardController {

    @FXML private TextField txtIsbn;
    @FXML private TextField txtTituloLibro;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtUsuario;
    @FXML private TextArea txtMotivo;

    @FXML private TableView<MovimientoInventario> tblMovimientos;
    @FXML private TableColumn<MovimientoInventario, Integer> colId;
    @FXML private TableColumn<MovimientoInventario, String> colIsbn;
    @FXML private TableColumn<MovimientoInventario, Integer> colCantidad;
    @FXML private TableColumn<MovimientoInventario, Integer> colUsuario;
    @FXML private TableColumn<MovimientoInventario, Timestamp> colFecha;
    @FXML private TableColumn<MovimientoInventario, String> colMotivo;

    private MovimientoInventarioDAO movimientoDAO;
    private LibroDAO libroDAO;
    private Usuario usuarioSesion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        movimientoDAO = new MovimientoInventarioDAOImpl();
        libroDAO = new LibroDAOImpl();

        colId.setCellValueFactory(new PropertyValueFactory<>("idMovimiento"));
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaMovimiento"));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));

        cargarHistorial();
    }

    @Override
    public void iniciarUsuario(Usuario usuario) {
        this.usuarioSesion = usuario;
        if (usuario != null && txtUsuario != null) {
            txtUsuario.setText(usuario.getNombre());
        }
    }

    @FXML
    private void handleRegresar(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/BodegaDashboardView.fxml");
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

        try {
            int cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "La cantidad debe ser mayor a 0.");
                return;
            }

            // Obtiene el ID del usuario en sesión; si no hay sesión iniciada, usa el ID 4 (bodega1)
            int idUsuarioValido = (this.usuarioSesion != null) ? this.usuarioSesion.getId() : 4;

            MovimientoInventario ingreso = new MovimientoInventario();
            ingreso.setIsbn(isbn);
            ingreso.setIdTipoMovimiento(1); // 1 = INGRESO
            ingreso.setIdUsuario(idUsuarioValido); // Cumple con la FK de MySQL
            ingreso.setCantidad(cantidad);
            ingreso.setMotivo(motivo); // Usar setMotivo en lugar de setObservacion

            if (movimientoDAO.insertar(ingreso)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Ingreso registrado correctamente.");
                handleLimpiar();
                cargarHistorial();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo registrar el ingreso. Revisa si el ISBN existe.");
            }

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "La cantidad debe ser un número entero válido.");
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
        ObservableList<MovimientoInventario> listaMovimientos = FXCollections.observableArrayList(movimientoDAO.listar());
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