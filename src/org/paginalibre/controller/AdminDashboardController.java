package org.paginalibre.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.paginalibre.dao.AdminReportesDAO;
import org.paginalibre.dao.impl.AdminReportesDAOImpl;
import org.paginalibre.model.Usuario;
import org.paginalibre.system.Main;

public class AdminDashboardController implements BaseDashboardController {

    @FXML
    private Label lblUsuario, lblRol, lblVentas, lblLibros, lblUsuarios;
    private final AdminReportesDAO reportesDAO = new AdminReportesDAOImpl();

    @Override
    public void iniciarUsuario(Usuario u) {
        if (u != null) {
            lblUsuario.setText((u.getNombre() + " " + u.getApellido()).trim());
            lblRol.setText("Rol: " + u.getRol());
        }
        cargarIndicadores();
    }

    @FXML
    public void initialize() {
        cargarIndicadores();
    }

    private void cargarIndicadores() {
        if (lblVentas == null) {
            return;
        }
        var k = reportesDAO.obtenerKPI();
        lblVentas.setText(String.format("Q %.2f", k.ventasTotales()));
        lblLibros.setText(String.valueOf(k.libros()));
        lblUsuarios.setText(String.valueOf(k.usuariosActivos()));
    }

    private void abrir(String ruta, String titulo) {
        try {
            Main.cambiarVista(ruta);
            Stage s = Main.getEscenarioPrincipal();
            if (s != null) {
                s.setTitle(titulo + " - Página Libre");
            }
        } catch (Exception e) {
            alert("Error", "No se pudo abrir el módulo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void abrirUsuarios(ActionEvent e) {
        abrir("/org/paginalibre/view/UsuarioView.fxml", "Gestión de Usuarios");
    }

    @FXML
    private void abrirInventario(ActionEvent e) {
        abrir("/org/paginalibre/view/InventarioBodegaView.fxml", "Gestión de Inventario");
    }

    @FXML
    private void abrirReportes(ActionEvent e) {
        abrir("/org/paginalibre/view/AdminReportesView.fxml", "Reportes Administrativos");
    }

    @FXML
    private void abrirNuevaVenta(ActionEvent e) {
        abrir("/org/paginalibre/view/ventasventas.fxml", "Nueva Venta");
    }

    @FXML
    private void abrirVentas(ActionEvent e) {
        abrir("/org/paginalibre/view/HistorialVentasView.fxml", "Historial de Ventas");
    }

    @FXML
    private void abrirReembolsos(ActionEvent e) {
        abrir("/org/paginalibre/view/ReembolsosView.fxml", "Devoluciones");
    }

    @FXML
    private void abrirLibros(ActionEvent e) {
        abrir("/org/paginalibre/view/InventarioLibrosView.fxml", "Catálogo de Libros");
    }

    @FXML
    private void abrirBodega(ActionEvent e) {
        abrir("/org/paginalibre/view/IngresoInventario.fxml", "Ingreso de Inventario");
    }

    @FXML
    private void abrirSalidas(ActionEvent e) {
        abrir("/org/paginalibre/view/SalidaInventarioView.fxml", "Salidas de Inventario");
    }

    @FXML
    private void abrirNuevoLibro(ActionEvent e) {
        abrir("/org/paginalibre/view/NuevoLibroView.fxml", "Nuevo Libro");
    }

    @FXML
    private void abrirClientes(ActionEvent e) {
        abrir("/org/paginalibre/view/SeleccionarClienteView.fxml", "Clientes");
    }

    @FXML
    private void abrirGestionAdmin(ActionEvent e) {
        abrir("/org/paginalibre/view/GestionAdministrativaView.fxml", "Gestión Administrativa");
    }

    @FXML
    private void cerrarSesion(ActionEvent e) {
        Main.cerrarSesion();
        try {
            Main.cambiarVista("/org/paginalibre/view/login.fxml");
        } catch (Exception ex) {
            alert("Error", "No se pudo cerrar sesión.", Alert.AlertType.ERROR);
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
