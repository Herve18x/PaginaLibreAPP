package org.paginalibre.controller;

import org.paginalibre.system.Main;
import org.paginalibre.util.Conexion;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class SalidaInventarioController implements Initializable {

    @FXML private TableView<MovimientoView> tblMovimientos;
    @FXML private TableColumn<MovimientoView, Integer> colId;
    @FXML private TableColumn<MovimientoView, String> colLibro;
    @FXML private TableColumn<MovimientoView, String> colTipo;
    @FXML private TableColumn<MovimientoView, Integer> colCantidad;
    @FXML private TableColumn<MovimientoView, String> colFecha;

    @FXML private TextField txtBuscarLibro;
    @FXML private Label lblLibroSeleccionado;
    @FXML private Label lblStockDisponible;
    @FXML private ComboBox<TipoMovimientoCombo> cbTipoSalida;
    @FXML private TextField txtCantidad;
    @FXML private TextArea txtObservacion;

    private ObservableList<MovimientoView> listaMovimientos;
    private ObservableList<TipoMovimientoCombo> listaTipos;

    private String isbnLibroSeleccionado = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        cargarComboTiposMovimiento();
        cargarMovimientos();
    }

    private void configurarTabla() {
        if (colId != null) {
            colId.setCellValueFactory(cellData ->
                    cellData.getValue().getIdProperty().asObject());
        }

        if (colLibro != null) {
            colLibro.setCellValueFactory(cellData ->
                    cellData.getValue().getLibroProperty());
        }

        if (colTipo != null) {
            colTipo.setCellValueFactory(cellData ->
                    cellData.getValue().getTipoProperty());
        }

        if (colCantidad != null) {
            colCantidad.setCellValueFactory(cellData ->
                    cellData.getValue().getCantidadProperty().asObject());
        }

        if (colFecha != null) {
            colFecha.setCellValueFactory(cellData ->
                    cellData.getValue().getFechaProperty());
        }
    }

    @FXML
    public void buscarLibroManual(ActionEvent event) {
        buscarLibroManual();
    }

    public void buscarLibroManual() {
        if (txtBuscarLibro == null ||
                txtBuscarLibro.getText().trim().isEmpty()) {

            mostrarAlerta(
                    "Atención",
                    "Ingrese un ISBN o nombre para buscar.",
                    Alert.AlertType.WARNING
            );
            return;
        }

        String criterio = txtBuscarLibro.getText().trim();

        String sql =
                "SELECT isbn, titulo, stock_actual " +
                "FROM libros " +
                "WHERE (isbn = ? OR titulo LIKE ?) " +
                "AND estado = 1 " +
                "LIMIT 1";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, criterio);
            ps.setString(2, "%" + criterio + "%");

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    isbnLibroSeleccionado = rs.getString("isbn");

                    String titulo = rs.getString("titulo");
                    int stock = rs.getInt("stock_actual");

                    if (lblLibroSeleccionado != null) {
                        lblLibroSeleccionado.setText(
                                "Libro: " + titulo +
                                " (ISBN: " + isbnLibroSeleccionado + ")"
                        );
                    }

                    if (lblStockDisponible != null) {
                        lblStockDisponible.setText(
                                "Stock disponible: " + stock
                        );
                    }

                } else {

                    isbnLibroSeleccionado = null;

                    if (lblLibroSeleccionado != null) {
                        lblLibroSeleccionado.setText(
                                "Ningún libro seleccionado"
                        );
                    }

                    if (lblStockDisponible != null) {
                        lblStockDisponible.setText(
                                "Stock disponible: 0"
                        );
                    }

                    mostrarAlerta(
                            "Sin Resultados",
                            "No se encontró ningún libro con el criterio ingresado.",
                            Alert.AlertType.INFORMATION
                    );
                }
            }

        } catch (SQLException e) {

            mostrarAlerta(
                    "Error de Búsqueda",
                    "Error al consultar libro: " + e.getMessage(),
                    Alert.AlertType.ERROR
            );
        }
    }

    public void cargarComboTiposMovimiento() {
        listaTipos = FXCollections.observableArrayList();

        String sql =
                "SELECT id_tipo_movimiento, nombre_tipo " +
                "FROM tipos_movimiento " +
                "WHERE nombre_tipo NOT IN ('AJUSTE', 'INGRESO')";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                listaTipos.add(
                        new TipoMovimientoCombo(
                                rs.getInt("id_tipo_movimiento"),
                                rs.getString("nombre_tipo")
                        )
                );
            }

            if (cbTipoSalida != null) {
                cbTipoSalida.setItems(listaTipos);
            }

        } catch (SQLException e) {

            mostrarAlerta(
                    "Error de Carga",
                    "No se pudieron cargar los tipos de movimiento: "
                            + e.getMessage(),
                    Alert.AlertType.ERROR
            );
        }
    }

    public void cargarMovimientos() {
        listaMovimientos = FXCollections.observableArrayList();

        String sql =
                "SELECT mi.id_movimiento, " +
                "l.titulo AS libro, " +
                "tm.nombre_tipo AS tipo, " +
                "mi.cantidad, " +
                "DATE_FORMAT(mi.fecha_movimiento, '%d/%m/%Y %H:%i') AS fecha " +
                "FROM movimientos_inventario mi " +
                "INNER JOIN libros l ON mi.isbn = l.isbn " +
                "INNER JOIN tipos_movimiento tm " +
                "ON mi.id_tipo_movimiento = tm.id_tipo_movimiento " +
                "WHERE tm.nombre_tipo <> 'INGRESO' " +
                "ORDER BY mi.fecha_movimiento DESC";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaMovimientos.add(
                        new MovimientoView(
                                rs.getInt("id_movimiento"),
                                rs.getString("libro"),
                                rs.getString("tipo"),
                                rs.getInt("cantidad"),
                                rs.getString("fecha")
                        )
                );
            }

            if (tblMovimientos != null) {
                tblMovimientos.setItems(listaMovimientos);
                tblMovimientos.refresh();
            }

        } catch (SQLException e) {
            mostrarAlerta(
                    "Error de Carga",
                    "No se pudieron cargar los movimientos: " + e.getMessage(),
                    Alert.AlertType.ERROR
            );
        }
    }

    @FXML
    public void handleRegistrarSalida() {
        registrarSalida();
    }

    @FXML
    public void registrarSalida() {
        if (!validarCampos()) {
            return;
        }

        TipoMovimientoCombo tipoSel =
                cbTipoSalida.getSelectionModel().getSelectedItem();

        int cantidad =
                Integer.parseInt(txtCantidad.getText().trim());

        int idUsuario = 1;

        String observacion =
                txtObservacion != null
                        ? txtObservacion.getText().trim()
                        : "";

        String callSp =
                "{CALL sp_registrar_movimiento_inventario(?, ?, ?, ?, ?)}";

        try (Connection conn = Conexion.getInstancia().conectar();
             CallableStatement cs = conn.prepareCall(callSp)) {

            cs.setString(1, isbnLibroSeleccionado);
            cs.setInt(2, tipoSel.getIdTipo());
            cs.setInt(3, cantidad);
            cs.setInt(4, idUsuario);
            cs.setString(5, observacion);

            cs.execute();

            mostrarAlerta(
                    "Éxito",
                    "Salida de inventario registrada correctamente.",
                    Alert.AlertType.INFORMATION
            );

            limpiarCampos();
            cargarMovimientos();

        } catch (SQLException e) {
            mostrarAlerta(
                    "Error de Registro",
                    "No se pudo registrar la salida: " + e.getMessage(),
                    Alert.AlertType.ERROR
            );
        }
    }

    @FXML
    public void regresarDashboard() {
        try {
            Main.regresarAnterior();
        } catch (Exception e) {
            mostrarAlerta(
                    "Error de Navegación",
                    "No se pudo regresar al dashboard: " + e.getMessage(),
                    Alert.AlertType.ERROR
            );
        }
    }

    @FXML
    public void limpiarCampos() {
        isbnLibroSeleccionado = null;

        if (txtBuscarLibro != null) {
            txtBuscarLibro.clear();
        }

        if (lblLibroSeleccionado != null) {
            lblLibroSeleccionado.setText(
                    "Ningún libro seleccionado"
            );
        }

        if (lblStockDisponible != null) {
            lblStockDisponible.setText(
                    "Stock disponible: 0"
            );
        }

        if (cbTipoSalida != null) {
            cbTipoSalida.getSelectionModel().clearSelection();
        }

        if (txtCantidad != null) {
            txtCantidad.clear();
        }

        if (txtObservacion != null) {
            txtObservacion.clear();
        }
    }

    private boolean validarCampos() {
        if (isbnLibroSeleccionado == null) {
            mostrarAlerta(
                    "Campo requerido",
                    "Debe buscar y seleccionar un libro válido.",
                    Alert.AlertType.WARNING
            );
            return false;
        }

        if (cbTipoSalida == null ||
                cbTipoSalida.getSelectionModel().getSelectedItem() == null) {

            mostrarAlerta(
                    "Campo requerido",
                    "Debe seleccionar un tipo de movimiento.",
                    Alert.AlertType.WARNING
            );
            return false;
        }

        if (txtCantidad == null ||
                txtCantidad.getText().trim().isEmpty()) {

            mostrarAlerta(
                    "Campo requerido",
                    "Debe ingresar una cantidad.",
                    Alert.AlertType.WARNING
            );
            return false;
        }

        try {
            int cant =
                    Integer.parseInt(txtCantidad.getText().trim());

            if (cant <= 0) {
                mostrarAlerta(
                        "Validación",
                        "La cantidad debe ser mayor a cero.",
                        Alert.AlertType.WARNING
                );
                return false;
            }

        } catch (NumberFormatException e) {
            mostrarAlerta(
                    "Validación",
                    "La cantidad debe ser un número entero válido.",
                    Alert.AlertType.WARNING
            );
            return false;
        }

        return true;
    }

    private void mostrarAlerta(
            String titulo,
            String contenido,
            Alert.AlertType tipo) {

        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    public static class TipoMovimientoCombo {

        private final int idTipo;
        private final String nombre;

        public TipoMovimientoCombo(int idTipo, String nombre) {
            this.idTipo = idTipo;
            this.nombre = nombre;
        }

        public int getIdTipo() {
            return idTipo;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }

    public static class MovimientoView {

        private final SimpleIntegerProperty id;
        private final SimpleStringProperty libro;
        private final SimpleStringProperty tipo;
        private final SimpleIntegerProperty cantidad;
        private final SimpleStringProperty fecha;

        public MovimientoView(
                int id,
                String libro,
                String tipo,
                int cantidad,
                String fecha) {

            this.id = new SimpleIntegerProperty(id);
            this.libro = new SimpleStringProperty(libro);
            this.tipo = new SimpleStringProperty(tipo);
            this.cantidad = new SimpleIntegerProperty(cantidad);
            this.fecha = new SimpleStringProperty(fecha);
        }

        public SimpleIntegerProperty getIdProperty() {
            return id;
        }

        public SimpleStringProperty getLibroProperty() {
            return libro;
        }

        public SimpleStringProperty getTipoProperty() {
            return tipo;
        }

        public SimpleIntegerProperty getCantidadProperty() {
            return cantidad;
        }

        public SimpleStringProperty getFechaProperty() {
            return fecha;
        }
    }
}