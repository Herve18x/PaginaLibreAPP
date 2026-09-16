package org.paginalibre.controller;

import java.net.URL;
import java.time.LocalDate;
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
    private TableColumn<Libro, LocalDate> colFechaPublicacion;

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

    private LibroDAO libroDAO;

    private ObservableList<Libro> listaLibros;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        libroDAO = new LibroDAOImpl();

        listaLibros = FXCollections.observableArrayList();

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

        cargarLibros();
    }

    private void cargarLibros() {

        listaLibros.clear();

        List<Libro> libros = libroDAO.listar();

        if (libros != null) {
            listaLibros.addAll(libros);
        }

        tablaLibros.setItems(listaLibros);
    }

    @FXML
    private void buscarPorIsbn(ActionEvent event) {

        String isbn = txtBuscarIsbn.getText().trim();

        if (isbn.isEmpty()) {
            cargarLibros();
            return;
        }

        Libro libro = libroDAO.buscar(isbn);

        if (libro != null) {

            listaLibros.clear();

            listaLibros.add(libro);
            
            tablaLibros.setItems(listaLibros);
        } else {
            Alert alerta = new Alert(
                    Alert.AlertType.INFORMATION
            );
            alerta.setTitle("Buscar libro");
            alerta.setHeaderText(null);
            alerta.setContentText(
                    "No se encontró ningún libro con el ISBN: " + isbn
            );
            alerta.showAndWait();
        }
    }

    @FXML
    private void mostrarTodos(ActionEvent event) {
        txtBuscarIsbn.clear();
        cargarLibros();
    }

    @FXML
    private void regresarDashboard(ActionEvent event) {
        try {
            Main.cambiarVista(
                    "/org/paginalibre/view/AdminDashboardView.fxml"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}