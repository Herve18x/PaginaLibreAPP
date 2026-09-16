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
    @FXML private ComboBox<String> cbTipoMovimiento; // Componente para seleccionar Entrada/Salida

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

        // Cargar opciones del ComboBox (Tipos de movimiento de la BD)
        if (cbTipoMovimiento != null) {
            cbTipoMovimiento.setItems(FXCollections.observableArrayList("INGRESO", "MERMA", "TRASLADO", "AJUSTE"));
            cbTipoMovimiento.getSelectionModel().selectFirst();
        }

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
        String tipoSeleccionado = (cbTipoMovimiento != null) ? cbTipoMovimiento.getValue() : "INGRESO";

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

            // Validar existencia del libro
            Libro libro = libroDAO.buscar(isbn);
            if (libro == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "El ISBN ingresado no existe en el catálogo.");
                return;
            }

            // Mapeo de IDs según la tabla tipos_movimiento de MySQL
            int idTipoMovimiento = 1; // 1 = INGRESO (SUMAR)
            boolean esSalida = false;

            switch (tipoSeleccionado) {
                case "MERMA":
                    idTipoMovimiento = 3; // RESTAR
                    esSalida = true;
                    break;
                case "TRASLADO":
                    idTipoMovimiento = 4; // RESTAR
                    esSalida = true;
                    break;
                case "AJUSTE":
                    idTipoMovimiento = 6; // RESTAR
                    esSalida = true;
                    break;
                default:
                    idTipoMovimiento = 1; // INGRESO
                    break;
            }

            // Regla de Negocio US-3.2: Validar que no se retire más stock del disponible
            if (esSalida && cantidad > libro.getStockActual()) {
                mostrarAlerta(Alert.AlertType.ERROR, "Stock Insuficiente", 
                    "No se puede registrar la salida. Stock actual: " + libro.getStockActual() + ", solicitado: " + cantidad);
                return;
            }

            int idUsuarioValido = (this.usuarioSesion != null) ? this.usuarioSesion.getId() : 4;

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setIsbn(isbn);
            movimiento.setIdTipoMovimiento(idTipoMovimiento);
            movimiento.setIdUsuario(idUsuarioValido);
            movimiento.setCantidad(cantidad);
            movimiento.setMotivo(motivo);

            if (movimientoDAO.insertar(movimiento)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Movimiento de inventario registrado correctamente.");
                handleLimpiar();
                cargarHistorial();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo registrar el movimiento en la base de datos.");
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
        if (cbTipoMovimiento != null) {
            cbTipoMovimiento.getSelectionModel().selectFirst();
        }
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