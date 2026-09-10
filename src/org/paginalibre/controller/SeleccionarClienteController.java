package org.paginalibre.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.paginalibre.dao.ClienteDAO;
import org.paginalibre.dao.impl.ClienteDAOImpl;
import org.paginalibre.model.Cliente;

public class SeleccionarClienteController implements Initializable {

    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, Long> colCui;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colApellido;
    @FXML private TableColumn<Cliente, String> colCorreo;

    private final ClienteDAO clienteDAO = new ClienteDAOImpl();
    private Cliente clienteSeleccionado = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colCui.setCellValueFactory(new PropertyValueFactory<>("cui"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correoElectronico"));

        cargarClientes();
    }

    private void cargarClientes() {
        List<Cliente> clientes = clienteDAO.listar();
        if (clientes != null) {
            tablaClientes.setItems(FXCollections.observableArrayList(clientes));
        }
    }

    @FXML
    private void handleSeleccionar(ActionEvent event) {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            this.clienteSeleccionado = seleccionado;
            cerrarVentana();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atención");
            alert.setHeaderText(null);
            alert.setContentText("Debe seleccionar un cliente de la tabla.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        this.clienteSeleccionado = null;
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) tablaClientes.getScene().getWindow();
        stage.close();
    }

    public Cliente getClienteSeleccionado() {
        return clienteSeleccionado;
    }
}