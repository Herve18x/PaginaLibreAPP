package org.paginalibre.controller;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.paginalibre.dao.impl.LibroDAOImpl;
import org.paginalibre.model.Libro;
import org.paginalibre.system.Main;

public class NuevoLibroController implements Initializable {

    @FXML private TextField txtIsbn;
    @FXML private TextField txtTitulo;
    @FXML private DatePicker dpFechaPublicacion;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtCategoria;
    @FXML private TextField txtEditorial;
    @FXML private TextField txtStockActual;
    @FXML private TextField txtStockMinimo;
    @FXML private CheckBox chkEstado;

    private final LibroDAOImpl libroDAO = new LibroDAOImpl();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chkEstado.setSelected(true);
    }

    @FXML
    private void guardarLibro(ActionEvent event) {
        try {
            String isbn = txtIsbn.getText().trim();
            String titulo = txtTitulo.getText().trim();
            String editorial = txtEditorial.getText().trim();

            if (isbn.isEmpty() || titulo.isEmpty() || txtPrecio.getText().trim().isEmpty()
                    || txtCategoria.getText().trim().isEmpty()
                    || txtStockActual.getText().trim().isEmpty()
                    || txtStockMinimo.getText().trim().isEmpty()) {
                mostrarAlerta("Campos incompletos", "Completa todos los campos obligatorios.", Alert.AlertType.WARNING);
                return;
            }

            if (libroDAO.buscar(isbn) != null) {
                mostrarAlerta("ISBN duplicado", "Ya existe un libro registrado con ese ISBN.", Alert.AlertType.WARNING);
                return;
            }

            double precio = Double.parseDouble(txtPrecio.getText().trim().replace(",", "."));
            int categoria = Integer.parseInt(txtCategoria.getText().trim());
            int stockActual = Integer.parseInt(txtStockActual.getText().trim());
            int stockMinimo = Integer.parseInt(txtStockMinimo.getText().trim());

            if (precio < 0 || categoria < 0 || stockActual < 0 || stockMinimo < 0) {
                mostrarAlerta("Datos inválidos", "Precio, categoría y cantidades no pueden ser negativos.", Alert.AlertType.WARNING);
                return;
            }

            Libro libro = new Libro();
            libro.setIsbn(isbn);
            libro.setTitulo(titulo);
            libro.setFechaPublicacion(dpFechaPublicacion.getValue());
            libro.setPrecio(precio);
            libro.setIdCategoria(categoria);
            libro.setNitEditorial(editorial.isEmpty() ? null : editorial);
            libro.setStockActual(stockActual);
            libro.setStockMinimo(stockMinimo);
            libro.setActivo(chkEstado.isSelected());

            if (libroDAO.insertar(libro)) {
                mostrarAlerta("Libro registrado", "El libro se agregó correctamente al catálogo.", Alert.AlertType.INFORMATION);
                limpiarFormulario(null);
            } else {
                mostrarAlerta("Error", "No se pudo registrar el libro en la base de datos.", Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Datos inválidos", "Precio, categoría, stock actual y stock mínimo deben contener valores numéricos válidos.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void limpiarFormulario(ActionEvent event) {
        txtIsbn.clear();
        txtTitulo.clear();
        dpFechaPublicacion.setValue(null);
        txtPrecio.clear();
        txtCategoria.clear();
        txtEditorial.clear();
        txtStockActual.clear();
        txtStockMinimo.clear();
        chkEstado.setSelected(true);
    }

    @FXML
    private void regresar(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/BodegaDashboardView.fxml");
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