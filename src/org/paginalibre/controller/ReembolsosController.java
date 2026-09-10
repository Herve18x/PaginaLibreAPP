package org.paginalibre.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.paginalibre.dao.VentaDAO;
import org.paginalibre.dao.impl.VentaDAOImpl;
import org.paginalibre.model.Venta;
import org.paginalibre.system.Main;

public class ReembolsosController implements Initializable {

    @FXML
    private TableView<Venta> tblVentas;
    @FXML
    private TableColumn<Venta, Integer> colIdVenta;
    @FXML
    private TableColumn<Venta, Object> colFecha;
    @FXML
    private TableColumn<Venta, Double> colTotal;
    @FXML
    private TableColumn<Venta, String> colEstado;
    @FXML
    private TableColumn<Venta, Long> colCliente;
    @FXML
    private TableColumn<Venta, Integer> colUsuario;
    @FXML
    private TextField txtBuscar;

    private final VentaDAO ventaDAO = new VentaDAOImpl();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colIdVenta.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaVenta"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("cuiCliente"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));

        cargarVentas();
    }

    private void cargarVentas() {
        ObservableList<Venta> listaVentas = FXCollections.observableArrayList(ventaDAO.listar());
        tblVentas.setItems(listaVentas);
    }

    @FXML
    private void procesarReembolso(ActionEvent event) {
        Venta seleccionada = tblVentas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Atención", "Selecciona una venta para reembolsar.", Alert.AlertType.WARNING);
            return;
        }

        if ("DEVUELTA".equalsIgnoreCase(seleccionada.getEstado()) || "ANULADA".equalsIgnoreCase(seleccionada.getEstado())) {
            mostrarAlerta("Aviso", "Esta venta ya fue devuelta o anulada.", Alert.AlertType.INFORMATION);
            return;
        }

        boolean exito = ventaDAO.eliminar(seleccionada.getIdVenta());
        if (exito) {
            mostrarAlerta("Éxito", "La venta #" + seleccionada.getIdVenta() + " ha sido reembolsada.", Alert.AlertType.INFORMATION);
            cargarVentas();
        } else {
            mostrarAlerta("Error", "No se pudo procesar el reembolso de la venta.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void regresarDashboard(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/CajeroDashboardView.fxml");
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