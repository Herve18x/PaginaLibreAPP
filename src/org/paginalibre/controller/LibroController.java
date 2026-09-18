package org.paginalibre.controller;

import java.net.URL;
import java.util.List;
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

import org.paginalibre.dao.LibroDAO;
import org.paginalibre.dao.impl.LibroDAOImpl;
import org.paginalibre.model.Libro;
import org.paginalibre.system.Main;

public class LibroController implements Initializable {


@FXML
private TableView<Libro> tablaLibros;

@FXML
private TableColumn<Libro, String> colIsbn;

@FXML
private TableColumn<Libro, String> colTitulo;

@FXML
private TableColumn<Libro, java.time.LocalDate> colFechaPublicacion;

@FXML
private TableColumn<Libro, Double> colPrecio;

@FXML
private TableColumn<Libro, Integer> colCategoria;

@FXML
private TableColumn<Libro, String> colEditorial;

@FXML
private TableColumn<Libro, Integer> colStockActual;

@FXML
private TableColumn<Libro, Integer> colStockMinimo;

@FXML
private TextField txtBuscarIsbn;

private final LibroDAO libroDAO = new LibroDAOImpl();

private final ObservableList<Libro> listaLibros =
        FXCollections.observableArrayList();

@Override
public void initialize(URL url, ResourceBundle rb) {
    colIsbn.setCellValueFactory(
            new PropertyValueFactory<>("isbn")
    );

    colTitulo.setCellValueFactory(
            new PropertyValueFactory<>("titulo")
    );

    colFechaPublicacion.setCellValueFactory(
            new PropertyValueFactory<>("fechaPublicacion")
    );

    colPrecio.setCellValueFactory(
            new PropertyValueFactory<>("precio")
    );

    colCategoria.setCellValueFactory(
            new PropertyValueFactory<>("categoriaId")
    );

    colEditorial.setCellValueFactory(
            new PropertyValueFactory<>("nitEditorial")
    );

    colStockActual.setCellValueFactory(
            new PropertyValueFactory<>("stockActual")
    );

    colStockMinimo.setCellValueFactory(
            new PropertyValueFactory<>("stockMinimo")
    );

    tablaLibros.setItems(listaLibros);

    cargarLibros();
}

private void cargarLibros() {
    List<Libro> libros = libroDAO.listar();

    listaLibros.clear();

    if (libros != null) {
        listaLibros.addAll(libros);
    }
}

@FXML
private void buscarPorIsbn(ActionEvent event) {
    String isbn = txtBuscarIsbn.getText();

    if (isbn == null || isbn.trim().isEmpty()) {
        cargarLibros();
        return;
    }

    try {
        Libro libro = libroDAO.buscar(isbn.trim());

        listaLibros.clear();

        if (libro != null) {
            listaLibros.add(libro);
        } else {
            mostrarAlerta(
                    "Buscar libro",
                    "No se encontró ningún libro con el ISBN: "
                            + isbn.trim(),
                    Alert.AlertType.INFORMATION
            );
        }
    } catch (Exception e) {
        mostrarAlerta(
                "Error",
                "No se pudo realizar la búsqueda.",
                Alert.AlertType.ERROR
        );
    }
}

@FXML
private void mostrarTodos(ActionEvent event) {
    txtBuscarIsbn.clear();
    cargarLibros();
}

@FXML
private void mostrarFormularioAgregar(ActionEvent event) {
    try {
        Main.cambiarVista(
                "/org/paginalibre/view/NuevoLibroView.fxml"
        );
    } catch (Exception e) {
        mostrarAlerta(
                "Error",
                "No se pudo abrir el formulario de libros.",
                Alert.AlertType.ERROR
        );
    }
}

@FXML
private void regresarDashboard(ActionEvent event) {
    try {
        Main.regresarDashboard();
    } catch (Exception e) {
        mostrarAlerta(
                "Error",
                "No se pudo regresar al panel principal.",
                Alert.AlertType.ERROR
        );
    }
}

private void mostrarAlerta(
        String titulo,
        String mensaje,
        Alert.AlertType tipo) {

    Alert alerta = new Alert(tipo);
    alerta.setTitle(titulo);
    alerta.setHeaderText(null);
    alerta.setContentText(mensaje);
    alerta.showAndWait();
}

}
