package org.paginalibre.controller;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.paginalibre.dao.LibroDAO;
import org.paginalibre.dao.impl.LibroDAOImpl;
import org.paginalibre.model.Libro;
import org.paginalibre.system.Main;

public class InventarioLibrosController implements Initializable {

    @FXML private TextField txtBuscar;
    @FXML private Button btnRegresar;

    @FXML private TableView<Libro> tablaLibros;
    @FXML private TableColumn<Libro, String> colIsbn;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, Double> colPrecio;
    @FXML private TableColumn<Libro, Integer> colStock;
    @FXML private TableColumn<Libro, String> colEditorial;
    @FXML private TableColumn<Libro, LocalDate> colFecha;

    private LibroDAO libroDAO;
    private ObservableList<Libro> listaLibros;
    private FilteredList<Libro> listaFiltrada;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        libroDAO = new LibroDAOImpl();
        listaLibros = FXCollections.observableArrayList();

        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        colEditorial.setCellValueFactory(new PropertyValueFactory<>("nitEditorial"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaPublicacion"));

        cargarDatos();
        configurarFiltroBusqueda();
    }

    private void cargarDatos() {
        listaLibros.clear();

        List<Libro> libros = libroDAO.listar();

        if (libros != null) {

            for (Libro libro : libros) {

                if (libro.getStockActual() > 0) {
                    listaLibros.add(libro);
                }

            }
        }

        listaFiltrada = new FilteredList<>(listaLibros, p -> true);
        tablaLibros.setItems(listaFiltrada);
    }

    private void configurarFiltroBusqueda() {
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            listaFiltrada.setPredicate(libro -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String filtro = newValue.toLowerCase().trim();
                return libro.getIsbn().toLowerCase().contains(filtro)
                        || libro.getTitulo().toLowerCase().contains(filtro);
            });
        });
    }

    
    @FXML 
    private void agregarLibro(ActionEvent event){
        
    
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