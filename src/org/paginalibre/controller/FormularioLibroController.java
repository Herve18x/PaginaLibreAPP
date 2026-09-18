package org.paginalibre.controller;

import java.time.LocalDate;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import org.paginalibre.dao.LibroDAO;
import org.paginalibre.dao.impl.LibroDAOImpl;
import org.paginalibre.model.Libro;
import org.paginalibre.system.Main;

public class FormularioLibroController {

@FXML
private TextField txtIsbn;

@FXML
private TextField txtTituloLibro;

@FXML
private TextField txtAutor;

@FXML
private TextField txtCategoria;

@FXML
private TextField txtEditorial;

@FXML
private TextField txtPrecio;

@FXML
private TextField txtStockMinimo;

@FXML
private CheckBox chkActivo;

@FXML
private Label lblError;

private final LibroDAO libroDAO = new LibroDAOImpl();

@FXML
private void guardarLibro(ActionEvent event) {
    String isbn = txtIsbn.getText().trim();
    String titulo = txtTituloLibro.getText().trim();
    String categoriaTexto = txtCategoria.getText().trim();
    String editorial = txtEditorial.getText().trim();
    String precioTexto = txtPrecio.getText().trim();
    String stockMinimoTexto = txtStockMinimo.getText().trim();

    if (isbn.isEmpty()
            || titulo.isEmpty()
            || categoriaTexto.isEmpty()
            || editorial.isEmpty()
            || precioTexto.isEmpty()
            || stockMinimoTexto.isEmpty()) {
        lblError.setText(
                "Complete todos los campos obligatorios."
        );
        return;
    }

    try {
        int categoriaId = Integer.parseInt(categoriaTexto);
        double precio = Double.parseDouble(
                precioTexto.replace(",", ".")
        );
        int stockMinimo = Integer.parseInt(stockMinimoTexto);

        if (categoriaId <= 0) {
            lblError.setText(
                    "La categoría debe ser un número mayor que 0."
            );
            return;
        }

        if (precio < 0) {
            lblError.setText(
                    "El precio no puede ser negativo."
            );
            return;
        }

        if (stockMinimo < 0) {
            lblError.setText(
                    "El stock mínimo no puede ser negativo."
            );
            return;
        }

        Libro existente = libroDAO.buscar(isbn);

        if (existente != null) {
            lblError.setText(
                    "Ya existe un libro con ese ISBN."
            );
            return;
        }

        Libro libro = new Libro();

        libro.setIsbn(isbn);
        libro.setTitulo(titulo);
        libro.setFechaPublicacion(LocalDate.now());
        libro.setPrecio(precio);
        libro.setCategoriaId(categoriaId);
        libro.setNitEditorial(editorial);
        libro.setStockActual(0);
        libro.setStockMinimo(stockMinimo);
        libro.setEstado(chkActivo.isSelected());

        boolean agregado = libroDAO.insertar(libro);

        if (agregado) {
            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Libro agregado",
                    "El libro fue agregado correctamente."
            );

            limpiarCampos();
        } else {
            lblError.setText(
                    "No se pudo agregar el libro."
            );
        }

    } catch (NumberFormatException e) {
        lblError.setText(
                "Categoría, precio y stock mínimo deben ser números."
        );
    } catch (Exception e) {
        lblError.setText(
                "Ocurrió un error al guardar el libro."
        );
    }
}

@FXML
private void cancelar(ActionEvent event) {
    try {
        Main.cambiarVista(
                "/org/paginalibre/view/libro.fxml"
        );
    } catch (Exception e) {
        lblError.setText(
                "No se pudo regresar a la gestión de libros."
        );
    }
}

private void limpiarCampos() {
    txtIsbn.clear();
    txtTituloLibro.clear();
    txtAutor.clear();
    txtCategoria.clear();
    txtEditorial.clear();
    txtPrecio.clear();
    txtStockMinimo.clear();

    chkActivo.setSelected(true);
    lblError.setText("");
    txtIsbn.requestFocus();
}

private void mostrarAlerta(
        Alert.AlertType tipo,
        String titulo,
        String mensaje) {

    Alert alerta = new Alert(tipo);
    alerta.setTitle(titulo);
    alerta.setHeaderText(null);
    alerta.setContentText(mensaje);
    alerta.showAndWait();
}

}
