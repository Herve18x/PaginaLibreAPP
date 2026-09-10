package org.paginalibre.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.paginalibre.dao.impl.VentaDAOImpl;
import org.paginalibre.model.Venta;
import org.paginalibre.system.Main;

public class HistorialVentasController implements Initializable {

    @FXML
    private TableView<Venta> tablaVentas;
    @FXML
    private TableColumn<Venta, Integer> colIdVenta;
    @FXML
    private TableColumn<Venta, Object> colFechaVenta;
    @FXML
    private TableColumn<Venta, Double> colTotal;
    @FXML
    private TableColumn<Venta, String> colEstado;
    @FXML
    private TableColumn<Venta, Long> colCuiCliente;
    @FXML
    private TableColumn<Venta, Integer> colIdUsuario;

    @FXML
    private TextField txtBuscar;

    private VentaDAOImpl ventaDAO;
    private ObservableList<Venta> listaVentas;
    private FilteredList<Venta> listaFiltrada;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ventaDAO = new VentaDAOImpl();
        listaVentas = FXCollections.observableArrayList();

        configurarTabla();
        cargarDatos();
        configurarFiltro();
    }

    private void configurarTabla() {
        colIdVenta.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        colFechaVenta.setCellValueFactory(new PropertyValueFactory<>("fechaVenta"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colCuiCliente.setCellValueFactory(new PropertyValueFactory<>("cuiCliente"));
        colIdUsuario.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
    }

    private void cargarDatos() {
        listaVentas.clear();
        List<Venta> ventas = ventaDAO.listar();
        if (ventas != null) {
            listaVentas.addAll(ventas);
        }
        listaFiltrada = new FilteredList<>(listaVentas, p -> true);
        tablaVentas.setItems(listaFiltrada);
    }

    private void configurarFiltro() {
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            listaFiltrada.setPredicate(venta -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }

                String filtro = newValue.toLowerCase().trim();

                return String.valueOf(venta.getIdVenta()).contains(filtro)
                    || String.valueOf(venta.getCuiCliente()).contains(filtro)
                    || String.valueOf(venta.getIdUsuario()).contains(filtro);
            });
        });
    }

    @FXML
    private void regresarDashboard(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/CajeroDashboardView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}