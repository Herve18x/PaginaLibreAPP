package org.paginalibre.controller;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import org.paginalibre.dao.LibroDAO;
import org.paginalibre.dao.impl.LibroDAOImpl;
import org.paginalibre.model.Libro;

public class LibroController implements Initializable {
    
    @FXML 
    private TableView<Libro> tablaLibros;
    @FXML
    private TableColumn<Libro, String> colIsbn;
    @FXML
    private TableColumn<Libro, String> colTitulo;    
    @FXML
    private TableColumn<Libro, Date> colFechaPublicacion;
    @FXML
    private TableColumn<Libro, Double> colPrecio;
    @FXML
    private TableColumn<Libro, Integer> colIdCategoria;
    @FXML
    private TableColumn<Libro, String> colNitEditorial;
    @FXML
    private TableColumn<Libro, Integer> colStockActual;
    @FXML
    private TableColumn<Libro, Integer> colStockMinimo;
    
    
    
    private LibroDAO libroDAO;
    
    private ObservableList<Libro> listaLibros;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        libroDAO = new LibroDAOImpl();
        
        listaLibros = FXCollections.observableArrayList();
        
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        
        colFechaPublicacion.setCellValueFactory(new PropertyValueFactory<>("fechaPublicacion"));
        
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("Precio")); 
        
        colIdCategoria.setCellValueFactory(new PropertyValueFactory<>("idCategoria"));
        
        colNitEditorial.setCellValueFactory(new PropertyValueFactory<>("nitEditorial"));
        
        colStockActual.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        
        colStockMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        
        cargarLibros();
    }
        private void cargarLibros(){
            
            listaLibros.clear();
            
            
            List<Libro> libros = libroDAO.listar();
            
            if (libros != null){
                listaLibros.addAll(libros);
            
            }
        tablaLibros.setItems(listaLibros);
        }
}
