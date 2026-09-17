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

        if (colIsbn != null) colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        if (colTitulo != null) colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        if (colCategoria != null) colCategoria.setCellValueFactory(new PropertyValueFactory<>("nombreCategoria"));
        if (colStockActual != null) colStockActual.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        if (colStockMin != null) colStockMin.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));

        cargarDashboard();
    }

    private void cargarDashboard() {
        if (libroDAO == null) return;

        List<Libro> listaBajoStock = libroDAO.listarLibrosBajoStock();
        ObservableList<Libro> itemsBajoStock = FXCollections.observableArrayList(listaBajoStock);

        if (tblBodega != null) {
            tblBodega.setItems(itemsBajoStock);
            tblBodega.refresh();
        }

        if (lblAlertasStock != null) {
            lblAlertasStock.setText(String.valueOf(listaBajoStock.size()));
        }

        List<Libro> todosLosLibros = libroDAO.listar();

        if (lblTitulosCatalogo != null) {
            lblTitulosCatalogo.setText(String.valueOf(todosLosLibros.size()));
        }

        String sqlEntradas = "SELECT COUNT(*) FROM movimientos_inventario mi "
                           + "INNER JOIN tipos_movimiento tm ON mi.id_tipo_movimiento = tm.id_tipo_movimiento "
                           + "WHERE tm.nombre_tipo = 'INGRESO' AND DATE(mi.fecha_movimiento) = CURDATE()";

        try (java.sql.Connection conn = org.paginalibre.util.Conexion.getInstancia().conectar();
             java.sql.PreparedStatement ps = conn.prepareStatement(sqlEntradas);
             java.sql.ResultSet rs = ps.executeQuery()) {

            if (rs.next() && lblEntradasHoy != null) {
                lblEntradasHoy.setText(String.valueOf(rs.getInt(1)));
            }

        } catch (java.sql.SQLException e) {
            if (lblEntradasHoy != null) {
                lblEntradasHoy.setText("0");
            }
        }
    }

    @Override
    public void iniciarUsuario(Usuario usuario) {
        this.usuarioSesion = usuario;

        if (usuario != null) {
            if (lblUsuario != null) {
                lblUsuario.setText(usuario.getNombre() + " " + usuario.getApellido());
            }

            if (lblRol != null) {
                lblRol.setText(usuario.getRol());
            }
        }
    }

    @FXML
    private void irIngresoInventario(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/IngresoInventario.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void irSalidaInventario(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/SalidaInventarioView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirNuevoLibro(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/NuevoLibroView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
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
    private void handleCerrarSesion(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/login.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}