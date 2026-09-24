package org.paginalibre.controller;

import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.paginalibre.dao.*;
import org.paginalibre.dao.impl.*;
import org.paginalibre.model.*;
import org.paginalibre.system.Main;

public class GestionAdministrativaController {

    @FXML
    private TableView<Categoria> tblCategorias;
    @FXML
    private TableColumn<Categoria, Integer> cCatId;
    @FXML
    private TableColumn<Categoria, String> cCatNombre;
    @FXML
    private TextField txtCatNombre;
    @FXML
    private TableView<Proveedor> tblProveedores;
    @FXML
    private TableColumn<Proveedor, Integer> cProvId;
    @FXML
    private TableColumn<Proveedor, String> cProvNombre, cProvTel, cProvDir, cProvCorreo;
    @FXML
    private TextField txtProvNombre, txtProvTel, txtProvDir, txtProvCorreo;
    @FXML
    private TextField txtIsbn, txtNuevoPrecio;

    private final CategoriaDAO catDAO = new CategoriaDAOImpl();
    private final ProveedorDAO provDAO = new ProveedorDAOImpl();
    private final LibroDAO libroDAO = new LibroDAOImpl();

    @FXML
    public void initialize() {
        cCatId.setCellValueFactory(new PropertyValueFactory<>("id"));
        cCatNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        cProvId.setCellValueFactory(new PropertyValueFactory<>("id"));
        cProvNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        cProvTel.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        cProvDir.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        cProvCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        cargar();
    }

    private void cargar() {
        tblCategorias.setItems(FXCollections.observableArrayList(catDAO.listar()));
        tblProveedores.setItems(FXCollections.observableArrayList(provDAO.listar()));
    }

    @FXML
    private void guardarCategoria() {
        String n = txtCatNombre.getText().trim();
        if (n.isEmpty()) {
            alert("Validación", "El nombre es obligatorio.", Alert.AlertType.WARNING);
            return;
        }
        Categoria c = tblCategorias.getSelectionModel().getSelectedItem();
        boolean ok;
        if (c == null) {
            ok = catDAO.insertar(new Categoria(0, n));
        } else {
            c.setNombre(n);
            ok = catDAO.actualizar(c);
        }
        if (ok) {
            txtCatNombre.clear();
            cargar();
        } else {
            alert("Error", "No se pudo guardar la categoría.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarCategoria() {
        Categoria c = tblCategorias.getSelectionModel().getSelectedItem();
        if (c != null && !catDAO.eliminar(c.getId())) {
            alert("Error", "No se puede eliminar la categoría si está siendo utilizada.", Alert.AlertType.ERROR);
        }
        cargar();
    }

    @FXML
    private void seleccionarCategoria() {
        Categoria c = tblCategorias.getSelectionModel().getSelectedItem();
        if (c != null) {
            txtCatNombre.setText(c.getNombre());
        }
    }

    @FXML
    private void guardarProveedor() {
        String n = txtProvNombre.getText().trim();
        if (n.isEmpty()) {
            alert("Validación", "El nombre es obligatorio.", Alert.AlertType.WARNING);
            return;
        }
        Proveedor p = tblProveedores.getSelectionModel().getSelectedItem();
        boolean ok;
        if (p == null) {
            ok = provDAO.insertar(new Proveedor(0, n, txtProvTel.getText().trim(), txtProvDir.getText().trim(), txtProvCorreo.getText().trim()));
        } else {
            p.setNombre(n);
            p.setTelefono(txtProvTel.getText().trim());
            p.setDireccion(txtProvDir.getText().trim());
            p.setCorreo(txtProvCorreo.getText().trim());
            ok = provDAO.actualizar(p);
        }
        if (ok) {
            limpiarProveedor();
            cargar();
        } else {
            alert("Error", "No se pudo guardar el proveedor.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void seleccionarProveedor() {
        Proveedor p = tblProveedores.getSelectionModel().getSelectedItem();
        if (p != null) {
            txtProvNombre.setText(p.getNombre());
            txtProvTel.setText(p.getTelefono());
            txtProvDir.setText(p.getDireccion());
            txtProvCorreo.setText(p.getCorreo());
        }
    }

    @FXML
    private void eliminarProveedor() {
        Proveedor p = tblProveedores.getSelectionModel().getSelectedItem();
        if (p != null && !provDAO.eliminar(p.getId())) {
            alert("Error", "No se pudo eliminar el proveedor.", Alert.AlertType.ERROR);
        }
        limpiarProveedor();
        cargar();
    }

    @FXML
    private void actualizarPrecio() {
        String isbn = txtIsbn.getText().trim();
        if (isbn.isEmpty()) {
            alert("Validación", "Ingrese el ISBN.", Alert.AlertType.WARNING);
            return;
        }
        try {
            double precio = Double.parseDouble(txtNuevoPrecio.getText().replace(",", "."));
            if (precio <= 0) {
                throw new NumberFormatException();
            }
            Libro l = libroDAO.buscar(isbn);
            if (l == null) {
                alert("No encontrado", "No existe un libro con ese ISBN.", Alert.AlertType.WARNING);
                return;
            }
            l.setPrecio(precio);
            if (libroDAO.actualizar(l)) {
                alert("Éxito", "Precio actualizado correctamente.", Alert.AlertType.INFORMATION);
                txtIsbn.clear();
                txtNuevoPrecio.clear();
            } else {
                alert("Error", "No se pudo actualizar el precio.", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            alert("Validación", "El precio debe ser numérico y mayor que 0.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void nuevoProveedor() {
        limpiarProveedor();
    }

    @FXML
    private void limpiarProveedor() {
        tblProveedores.getSelectionModel().clearSelection();
        txtProvNombre.clear();
        txtProvTel.clear();
        txtProvDir.clear();
        txtProvCorreo.clear();
        txtProvNombre.requestFocus();
    }

    @FXML
    private void regresar(ActionEvent e) {
        try {
            Main.regresarAnterior();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void alert(String t, String m, Alert.AlertType a) {
        Alert x = new Alert(a);
        x.setTitle(t);
        x.setHeaderText(null);
        x.setContentText(m);
        x.showAndWait();
    }
}
