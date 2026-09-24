package org.paginalibre.controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.paginalibre.util.Conexion;
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
import javafx.scene.layout.GridPane;
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
    @FXML private ComboBox<String> cmbAutor;
    private final Map<Integer, String> autoresDisponibles = new LinkedHashMap<>();
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

        cargarAutores();
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
                seleccionarAutorLibro(libroSeleccionado.getIsbn());
                lblAutoresInfo.setText((libroSeleccionado.getAutores() == null || libroSeleccionado.getAutores().isBlank())
                        ? "Sin autor registrado" : libroSeleccionado.getAutores());
                lblEditorialInfo.setText((libroSeleccionado.getNombreEditorial() == null || libroSeleccionado.getNombreEditorial().isBlank())
                        ? "Sin editorial registrada" : libroSeleccionado.getNombreEditorial()
                        + " (NIT: " + libroSeleccionado.getNitEditorial() + ")");

            }
        });
    }

    private void cargarAutores() {
        autoresDisponibles.clear();
        cmbAutor.getItems().clear();
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement("SELECT id_autor, nombre_autor, apellido_autor FROM autores ORDER BY apellido_autor, nombre_autor");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id_autor");
                String opcion = id + " - " + rs.getString("nombre_autor") + " " + rs.getString("apellido_autor");
                autoresDisponibles.put(id, opcion);
                cmbAutor.getItems().add(opcion);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los autores: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void seleccionarAutorLibro(String isbn) {
        cmbAutor.getSelectionModel().clearSelection();
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement("SELECT id_autor FROM autores_libro WHERE isbn = ? ORDER BY id_autor_libro LIMIT 1")) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) cmbAutor.getSelectionModel().select(autoresDisponibles.get(rs.getInt(1)));
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo cargar el autor del libro: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean guardarAutorLibro(String isbn) {
        String opcion = cmbAutor.getValue();
        if (opcion == null) {
            mostrarAlerta("Autor requerido", "Selecciona el autor del libro.", Alert.AlertType.WARNING);
            return false;
        }
        int id = Integer.parseInt(opcion.split(" - ", 2)[0]);
        try (Connection conn = Conexion.getInstancia().conectar()) {
            conn.setAutoCommit(false);
            try (PreparedStatement delete = conn.prepareStatement("DELETE FROM autores_libro WHERE isbn = ?");
                 PreparedStatement insert = conn.prepareStatement("INSERT INTO autores_libro(id_autor, isbn) VALUES (?, ?)")) {
                delete.setString(1, isbn);
                delete.executeUpdate();
                insert.setInt(1, id);
                insert.setString(2, isbn);
                insert.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo cambiar el autor del libro: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }

    @FXML
    private void editarAutor(ActionEvent event) {
        if (autoresDisponibles.isEmpty()) return;
        ChoiceDialog<String> selector = new ChoiceDialog<>(cmbAutor.getValue() == null ? autoresDisponibles.values().iterator().next() : cmbAutor.getValue(),
                new java.util.ArrayList<>(autoresDisponibles.values()));
        selector.setTitle("Editar autor");
        selector.setHeaderText("Selecciona el autor que deseas editar");
        selector.showAndWait().ifPresent(opcion -> {
            int id = Integer.parseInt(opcion.split(" - ", 2)[0]);
            try (Connection conn = Conexion.getInstancia().conectar();
                 PreparedStatement ps = conn.prepareStatement("SELECT nombre_autor, apellido_autor, nacionalidad, biografia FROM autores WHERE id_autor = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return;
                    TextField nombre = new TextField(rs.getString(1));
                    TextField apellido = new TextField(rs.getString(2));
                    TextField nacionalidad = new TextField(rs.getString(3) == null ? "" : rs.getString(3));
                    TextArea biografia = new TextArea(rs.getString(4) == null ? "" : rs.getString(4));
                    biografia.setPrefRowCount(3);
                    GridPane grid = new GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);
                    grid.addRow(0, new Label("Nombre *"), nombre);
                    grid.addRow(1, new Label("Apellido *"), apellido);
                    grid.addRow(2, new Label("Nacionalidad"), nacionalidad);
                    grid.addRow(3, new Label("Biografía"), biografia);
                    Dialog<ButtonType> dialog = new Dialog<>();
                    dialog.setTitle("Editar autor");
                    dialog.getDialogPane().setContent(grid);
                    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
                    dialog.setResultConverter(btn -> {
                        if (btn != ButtonType.OK) return null;
                        if (nombre.getText().trim().isEmpty() || apellido.getText().trim().isEmpty()) {
                            mostrarAlerta("Datos incompletos", "Nombre y apellido son obligatorios.", Alert.AlertType.WARNING);
                            return null;
                        }
                        try (Connection updateConn = Conexion.getInstancia().conectar();
                             PreparedStatement update = updateConn.prepareStatement("UPDATE autores SET nombre_autor = ?, apellido_autor = ?, nacionalidad = ?, biografia = ? WHERE id_autor = ?")) {
                            update.setString(1, nombre.getText().trim());
                            update.setString(2, apellido.getText().trim());
                            update.setString(3, nacionalidad.getText().trim().isEmpty() ? null : nacionalidad.getText().trim());
                            update.setString(4, biografia.getText().trim().isEmpty() ? null : biografia.getText().trim());
                            update.setInt(5, id);
                            update.executeUpdate();
                            return ButtonType.OK;
                        } catch (SQLException e) {
                            mostrarAlerta("Error", "No se pudo editar el autor: " + e.getMessage(), Alert.AlertType.ERROR);
                            return null;
                        }
                    });
                    dialog.showAndWait().ifPresent(resultado -> {
                        cargarAutores();
                        cmbAutor.getSelectionModel().select(autoresDisponibles.get(id));
                        cargarDatos();
                    });
                }
            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudo cargar el autor: " + e.getMessage(), Alert.AlertType.ERROR);
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
            if (cmbAutor.getValue() == null) {
                mostrarAlerta("Autor requerido", "Selecciona el autor del libro.", Alert.AlertType.WARNING);
                return;
            }
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
                if (!guardarAutorLibro(libroSeleccionado.getIsbn())) return;
                cargarDatos();
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
        cmbAutor.getSelectionModel().clearSelection();
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