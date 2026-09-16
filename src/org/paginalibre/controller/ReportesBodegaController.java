package org.paginalibre.controller;

import java.net.URL;
import java.sql.Timestamp;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.paginalibre.dao.MovimientoInventarioDAO;
import org.paginalibre.dao.impl.MovimientoInventarioDAOImpl;
import org.paginalibre.model.MovimientoInventario;
import org.paginalibre.system.Main;

public class ReportesBodegaController implements Initializable {

    @FXML private TableView<MovimientoInventario> tblReportes;
    @FXML private TableColumn<MovimientoInventario, Integer> colId;
    @FXML private TableColumn<MovimientoInventario, String> colIsbn;
    @FXML private TableColumn<MovimientoInventario, String> colTipo;
    @FXML private TableColumn<MovimientoInventario, Integer> colCantidad;
    @FXML private TableColumn<MovimientoInventario, Timestamp> colFecha;
    @FXML private TableColumn<MovimientoInventario, String> colObservacion;

    private MovimientoInventarioDAO movimientoDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        movimientoDAO = new MovimientoInventarioDAOImpl();

        colId.setCellValueFactory(new PropertyValueFactory<>("idMovimiento"));
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaMovimiento"));
        colObservacion.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("idTipoMovimiento"));

        cargarReporte();
    }

    private void cargarReporte() {
        if (tblReportes != null) {
            ObservableList<MovimientoInventario> lista = FXCollections.observableArrayList(movimientoDAO.listar());
            tblReportes.setItems(lista);
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
}