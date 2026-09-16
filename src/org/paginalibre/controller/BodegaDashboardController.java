package org.paginalibre.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.paginalibre.dao.LibroDAO;
import org.paginalibre.dao.impl.LibroDAOImpl;
import org.paginalibre.model.Libro;
import org.paginalibre.model.Usuario;
import org.paginalibre.system.Main;

public class BodegaDashboardController implements Initializable, BaseDashboardController {

    @FXML private Label lblUsuario;
    @FXML private Label lblRol;
    @FXML private Label lblAlertasStock;
    @FXML private Label lblEntradasHoy;
    @FXML private Label lblTitulosCatalogo;

    @FXML private TableView<Libro> tblBodega;
    @FXML private TableColumn<Libro, String> colIsbn;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, String> colCategoria;
    @FXML private TableColumn<Libro, Integer> colStockActual;
    @FXML private TableColumn<Libro, Integer> colStockMin;

    private LibroDAO libroDAO;
    private Usuario usuarioSesion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        libroDAO = new LibroDAOImpl();

        // 1. Configurar Fábricas de Celda PRIMERO
        if (colIsbn != null) colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        if (colTitulo != null) colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        if (colCategoria != null) colCategoria.setCellValueFactory(new PropertyValueFactory<>("nombreCategoria"));
        if (colStockActual != null) colStockActual.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        if (colStockMin != null) colStockMin.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));

        // 2. Cargar Datos
        cargarDashboard();
    }

    private void cargarDashboard() {
        if (libroDAO == null) return;

        // Obtener lista de libros bajo stock
        List<Libro> listaBajoStock = libroDAO.listarLibrosBajoStock();
        
        if (listaBajoStock != null) {
            ObservableList<Libro> itemsBajoStock = FXCollections.observableArrayList(listaBajoStock);
            
            if (tblBodega != null) {
                tblBodega.setItems(itemsBajoStock);
            }
            
            // Asignar el conteo directo a la tarjeta de Alertas
            if (lblAlertasStock != null) {
                lblAlertasStock.setText(String.valueOf(listaBajoStock.size()));
            }
        } else {
            if (lblAlertasStock != null) lblAlertasStock.setText("0");
        }

        // Obtener total de catálogo
        List<Libro> todosLosLibros = libroDAO.listar();
        if (lblTitulosCatalogo != null && todosLosLibros != null) {
            lblTitulosCatalogo.setText(String.valueOf(todosLosLibros.size()));
        }

        if (lblEntradasHoy != null) {
            lblEntradasHoy.setText("0");
        }
    }

    @Override
    public void iniciarUsuario(Usuario usuario) {
        this.usuarioSesion = usuario;
        if (usuario != null) {
            if (lblUsuario != null) lblUsuario.setText(usuario.getNombre() + " " + usuario.getApellido());
            if (lblRol != null) lblRol.setText(usuario.getRol());
        }
    }

    @FXML
    private void abrirMovimientos(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/IngresoInventario.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirInventario(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/InventarioBodegaView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirReportes(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/ReportesBodegaView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cerrarSesion(ActionEvent event) {
        handleCerrarSesion(event);
    }

    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/login.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}