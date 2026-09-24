package org.paginalibre.controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import org.paginalibre.system.Main;
import org.paginalibre.util.Conexion;

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

    private final ObservableList<MovimientoView> listaMovimientos = FXCollections.observableArrayList();
    private final ObservableList<TipoMovimientoCombo> listaTipos = FXCollections.observableArrayList();
    private String isbnLibroSeleccionado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        cargarComboTiposMovimiento();
        cargarMovimientos();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(data -> data.getValue().getIdProperty().asObject());
        colLibro.setCellValueFactory(data -> data.getValue().getLibroProperty());
        colTipo.setCellValueFactory(data -> data.getValue().getTipoProperty());
        colCantidad.setCellValueFactory(data -> data.getValue().getCantidadProperty().asObject());
        colFecha.setCellValueFactory(data -> data.getValue().getFechaProperty());
    }

    @FXML
    private void buscarLibroManual(ActionEvent event) {
        String criterio = txtBuscarLibro.getText() == null ? "" : txtBuscarLibro.getText().trim();

        if (criterio.isEmpty()) {
            mostrarAlerta("Atención", "Ingrese un ISBN o nombre para buscar.",
                    Alert.AlertType.WARNING);
            return;
        }

        String sql = "SELECT isbn, titulo, stock_actual FROM libros " +
                "WHERE estado = 1 AND (isbn = ? OR titulo LIKE ?) LIMIT 1";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, criterio);
            ps.setString(2, "%" + criterio + "%");

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    isbnLibroSeleccionado = null;
                    lblLibroSeleccionado.setText("Ningún libro seleccionado");
                    lblStockDisponible.setText("Stock disponible: 0");
                    mostrarAlerta("Sin resultados", "No se encontró el libro.",
                            Alert.AlertType.INFORMATION);
                    return;
                }

                isbnLibroSeleccionado = rs.getString("isbn");
                lblLibroSeleccionado.setText("Libro: " + rs.getString("titulo")
                        + " (ISBN: " + isbnLibroSeleccionado + ")");
                lblStockDisponible.setText("Stock disponible: " + rs.getInt("stock_actual"));
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo buscar el libro: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void cargarComboTiposMovimiento() {
        listaTipos.clear();
        String sql = "SELECT id_tipo_movimiento, nombre_tipo, operacion " +
                "FROM tipos_movimiento " +
                "WHERE operacion = 'RESTAR' ORDER BY nombre_tipo";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                listaTipos.add(new TipoMovimientoCombo(
                        rs.getInt("id_tipo_movimiento"),
                        rs.getString("nombre_tipo"),
                        rs.getString("operacion")));
            }
            cbTipoSalida.setItems(listaTipos);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los tipos de salida: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void cargarMovimientos() {
        listaMovimientos.clear();

        String sql = "SELECT mi.id_movimiento, l.titulo AS libro, tm.nombre_tipo AS tipo, " +
                "mi.cantidad, DATE_FORMAT(mi.fecha_movimiento, '%d/%m/%Y %H:%i') AS fecha " +
                "FROM movimientos_inventario mi " +
                "INNER JOIN libros l ON l.isbn = mi.isbn " +
                "INNER JOIN tipos_movimiento tm ON tm.id_tipo_movimiento = mi.id_tipo_movimiento " +
                "WHERE tm.operacion = 'RESTAR' " +
                "ORDER BY mi.fecha_movimiento DESC, mi.id_movimiento DESC";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                listaMovimientos.add(new MovimientoView(
                        rs.getInt("id_movimiento"),
                        rs.getString("libro"),
                        rs.getString("tipo"),
                        rs.getInt("cantidad"),
                        rs.getString("fecha")));
            }
            tblMovimientos.setItems(listaMovimientos);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los movimientos: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void registrarSalida(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }

        TipoMovimientoCombo tipo = cbTipoSalida.getValue();
        int cantidad = Integer.parseInt(txtCantidad.getText().trim());

        org.paginalibre.model.Usuario usuario = Main.getUsuarioSesion();
        if (usuario == null || usuario.getId() <= 0) {
            mostrarAlerta("Sesión inválida", "No hay un usuario autenticado.",
                    Alert.AlertType.ERROR);
            return;
        }

        String observacion = txtObservacion.getText() == null
                ? "" : txtObservacion.getText().trim();

        String sqlStock = "SELECT stock_actual FROM libros WHERE isbn = ? AND estado = 1";
        String call = "{CALL sp_registrar_movimiento_inventario(?, ?, ?, ?, ?)}";

        try (Connection conn = Conexion.getInstancia().conectar()) {
            if ("RESTAR".equalsIgnoreCase(tipo.getOperacion())) {
                try (PreparedStatement ps = conn.prepareStatement(sqlStock)) {
                    ps.setString(1, isbnLibroSeleccionado);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || rs.getInt(1) < cantidad) {
                            mostrarAlerta("Stock insuficiente", "La cantidad supera el stock disponible.",
                                    Alert.AlertType.WARNING);
                            return;
                        }
                    }
                }
            }

            try (java.sql.CallableStatement cs = conn.prepareCall(call)) {
                cs.setString(1, isbnLibroSeleccionado);
                cs.setInt(2, tipo.getIdTipo());
                cs.setInt(3, cantidad);
                cs.setInt(4, usuario.getId());
                cs.setString(5, observacion);
                cs.executeUpdate();
            }

            mostrarAlerta("Éxito", "Salida de inventario registrada correctamente.",
                    Alert.AlertType.INFORMATION);
            limpiarCampos();
            cargarMovimientos();
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo registrar la salida: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void regresarDashboard(ActionEvent event) {
        try {
            Main.regresarAnterior();
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo regresar: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void limpiarCampos() {
        isbnLibroSeleccionado = null;
        txtBuscarLibro.clear();
        lblLibroSeleccionado.setText("Ningún libro seleccionado");
        lblStockDisponible.setText("Stock disponible: 0");
        cbTipoSalida.getSelectionModel().clearSelection();
        txtCantidad.clear();
        txtObservacion.clear();
    }

    private boolean validarCampos() {
        if (isbnLibroSeleccionado == null || isbnLibroSeleccionado.isBlank()) {
            mostrarAlerta("Campo requerido", "Debe buscar un libro válido.",
                    Alert.AlertType.WARNING);
            return false;
        }
        if (cbTipoSalida.getValue() == null) {
            mostrarAlerta("Campo requerido", "Seleccione el tipo de salida.",
                    Alert.AlertType.WARNING);
            return false;
        }
        try {
            if (Integer.parseInt(txtCantidad.getText().trim()) <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Cantidad inválida", "La cantidad debe ser un entero mayor que cero.",
                    Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    public static class TipoMovimientoCombo {
        private final int idTipo;
        private final String nombre;
        private final String operacion;

        public TipoMovimientoCombo(int idTipo, String nombre, String operacion) {
            this.idTipo = idTipo;
            this.nombre = nombre;
            this.operacion = operacion;
        }

        public int getIdTipo() { return idTipo; }
        public String getOperacion() { return operacion; }

        @Override
        public String toString() { return nombre; }
    }

    public static class MovimientoView {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty libro;
        private final SimpleStringProperty tipo;
        private final SimpleIntegerProperty cantidad;
        private final SimpleStringProperty fecha;

        public MovimientoView(int id, String libro, String tipo, int cantidad, String fecha) {
            this.id = new SimpleIntegerProperty(id);
            this.libro = new SimpleStringProperty(libro);
            this.tipo = new SimpleStringProperty(tipo);
            this.cantidad = new SimpleIntegerProperty(cantidad);
            this.fecha = new SimpleStringProperty(fecha);
        }

        public SimpleIntegerProperty getIdProperty() { return id; }
        public SimpleStringProperty getLibroProperty() { return libro; }
        public SimpleStringProperty getTipoProperty() { return tipo; }
        public SimpleIntegerProperty getCantidadProperty() { return cantidad; }
        public SimpleStringProperty getFechaProperty() { return fecha; }
    }
}
