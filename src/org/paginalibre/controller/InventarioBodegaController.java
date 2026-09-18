package org.paginalibre.controller;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.paginalibre.dao.LibroDAO;
import org.paginalibre.dao.impl.LibroDAOImpl;
import org.paginalibre.model.Libro;
import org.paginalibre.system.Main;

public class InventarioBodegaController implements Initializable {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Libro> tablaLibros;
    @FXML private TableColumn<Libro, String> colIsbn;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, Double> colPrecio;
    @FXML private TableColumn<Libro, Integer> colStock;
    @FXML private TableColumn<Libro, String> colEditorial;
    @FXML private TableColumn<Libro, LocalDate> colFecha;

    // Campos del formulario de edición
    @FXML private TextField txtIsbn;
    @FXML private TextField txtTitulo;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;
    @FXML private TextField txtStockMinimo;
    @FXML private TextField txtCategoria;
    @FXML private TextField txtEditorial;
    @FXML private DatePicker dpFechaPub;
    @FXML private Label lblAutoresInfo;
    @FXML private Label lblEditorialInfo;
    @FXML private Label lblStockMinimoAyuda;

    private LibroDAO libroDAO;
    private ObservableList<Libro> listaLibros;
    private FilteredList<Libro> listaFiltrada;
    private Libro libroSeleccionado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        libroDAO = new LibroDAOImpl();
        listaLibros = FXCollections.observableArrayList();

        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        colEditorial.setCellValueFactory(new PropertyValueFactory<>("nombreEditorial"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaPublicacion"));

        cargarDatos();
        configurarFiltroBusqueda();
        configurarSeleccionTabla();
        configurarValidaciones();
        boolean esAdmin = Main.getUsuarioSesion() != null
                && "admin".equalsIgnoreCase(Main.getUsuarioSesion().getRol());
        txtStockMinimo.setDisable(!esAdmin);
        txtStockMinimo.setEditable(esAdmin);
        if (lblStockMinimoAyuda != null) {
            lblStockMinimoAyuda.setText(esAdmin ? "Admin puede modificarlo" : "Predeterminado: 5 (solo admin puede cambiarlo)");
        }
    }

    private void configurarValidaciones() {
        txtPrecio.setTextFormatter(new TextFormatter<String>(change ->
            change.getControlNewText().matches("\\d{0,6}([.,]\\d{0,2})?") ? change : null));
        txtStock.setTextFormatter(new TextFormatter<String>(change ->
            change.getControlNewText().matches("\\d{0,10}") ? change : null));
        txtStockMinimo.setTextFormatter(new TextFormatter<String>(change ->
            change.getControlNewText().matches("\\d{0,10}") ? change : null));
        txtCategoria.setTextFormatter(new TextFormatter<String>(change ->
            change.getControlNewText().matches("\\d{0,10}") ? change : null));
        txtTitulo.setTextFormatter(new TextFormatter<String>(change ->
            change.getControlNewText().matches("[\\p{L}\\p{N}\\s.,:;!?¿¡()'\"-]{0,100}") ? change : null));
        txtEditorial.setTextFormatter(new TextFormatter<String>(change ->
            change.getControlNewText().matches("[A-Za-z0-9-]{0,20}") ? change : null));
    }

    private void cargarDatos() {
        listaLibros.clear();
        List<Libro> libros = libroDAO.listar();
        if (libros != null) {
            listaLibros.addAll(libros);
        }
        listaFiltrada = new FilteredList<>(listaLibros, p -> true);
        tablaLibros.setItems(listaFiltrada);
    }

    private void configurarFiltroBusqueda() {
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            listaFiltrada.setPredicate(libro -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String filtro = newValue.toLowerCase().trim();
                return (libro.getIsbn() != null && libro.getIsbn().toLowerCase().contains(filtro))
                        || (libro.getTitulo() != null && libro.getTitulo().toLowerCase().contains(filtro));
            });
        });
    }

    private void configurarSeleccionTabla() {
        tablaLibros.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                libroSeleccionado = newSelection;
                txtIsbn.setText(libroSeleccionado.getIsbn());
                txtTitulo.setText(libroSeleccionado.getTitulo());
                txtPrecio.setText(String.valueOf(libroSeleccionado.getPrecio()));
                txtStock.setText(String.valueOf(libroSeleccionado.getStockActual()));
                txtStockMinimo.setText(String.valueOf(libroSeleccionado.getStockMinimo()));
                txtCategoria.setText(String.valueOf(libroSeleccionado.getCategoriaId()));
                txtEditorial.setText(libroSeleccionado.getNitEditorial());
                dpFechaPub.setValue(libroSeleccionado.getFechaPublicacion());
                lblAutoresInfo.setText((libroSeleccionado.getAutores() == null || libroSeleccionado.getAutores().isBlank())
                        ? "Sin autor registrado" : libroSeleccionado.getAutores());
                lblEditorialInfo.setText((libroSeleccionado.getNombreEditorial() == null || libroSeleccionado.getNombreEditorial().isBlank())
                        ? "Sin editorial registrada" : libroSeleccionado.getNombreEditorial()
                        + " (NIT: " + libroSeleccionado.getNitEditorial() + ")");

            }
        });
    }

    @FXML
    private void guardarCambios(ActionEvent event) {
        if (libroSeleccionado == null) {
            mostrarAlerta("Advertencia", "Por favor, selecciona un libro de la tabla para editar.", Alert.AlertType.WARNING);
            return;
        }

        try {
            libroSeleccionado.setTitulo(txtTitulo.getText());
            libroSeleccionado.setPrecio(Double.parseDouble(txtPrecio.getText()));
            libroSeleccionado.setStockActual(Integer.parseInt(txtStock.getText()));
            boolean esAdmin = Main.getUsuarioSesion() != null
                    && "admin".equalsIgnoreCase(Main.getUsuarioSesion().getRol());
            if (esAdmin) {
                String min = txtStockMinimo.getText().trim();
                libroSeleccionado.setStockMinimo(min.isEmpty() ? 5 : Integer.parseInt(min));
            } else {
                libroSeleccionado.setStockMinimo(5);
            }
            libroSeleccionado.setCategoriaId(Integer.parseInt(txtCategoria.getText()));
            libroSeleccionado.setNitEditorial(txtEditorial.getText());
            libroSeleccionado.setFechaPublicacion(dpFechaPub.getValue());

            boolean exito = libroDAO.actualizar(libroSeleccionado);

            if (exito) {
                mostrarAlerta("Éxito", "El libro ha sido actualizado correctamente.", Alert.AlertType.INFORMATION);
                tablaLibros.refresh();
                limpiarFormulario(null);
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el libro en la base de datos.", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error de Formato", "Asegúrate de ingresar valores numéricos válidos en Precio, Stock y Categoría.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void limpiarFormulario(ActionEvent event) {
        libroSeleccionado = null;
        txtIsbn.clear();
        txtTitulo.clear();
        txtPrecio.clear();
        txtStock.clear();
        txtStockMinimo.clear();
        txtCategoria.clear();
        txtEditorial.clear();
        dpFechaPub.setValue(null);
        lblAutoresInfo.setText("Seleccione un libro");
        lblEditorialInfo.setText("Seleccione un libro");
        tablaLibros.getSelectionModel().clearSelection();
    }

    @FXML
    private void regresarDashboard(ActionEvent event) {
        try {
            Main.regresarAnterior();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}