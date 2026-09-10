package org.paginalibre.controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.paginalibre.dao.ClienteDAO;
import org.paginalibre.dao.LibroDAO;
import org.paginalibre.dao.impl.ClienteDAOImpl;
import org.paginalibre.dao.impl.LibroDAOImpl;
import org.paginalibre.dao.impl.VentaDAOImpl;
import org.paginalibre.model.Cliente;
import org.paginalibre.model.DetalleVenta;
import org.paginalibre.model.Libro;
import org.paginalibre.model.Venta;
import org.paginalibre.system.Main;

public class VentaController implements Initializable {

    @FXML
    private TextField txtBusqueda;
    @FXML
    private Spinner<Integer> spinnerCantidad;

    // Tabla Principal (Catálogo / Búsqueda)
    @FXML
    private TableView<DetalleVenta> tablaDetalleVenta;
    @FXML
    private TableColumn<DetalleVenta, String> colIsbn;
    @FXML
    private TableColumn<DetalleVenta, String> colTitulo;
    @FXML
    private TableColumn<DetalleVenta, Integer> colCantidad;
    @FXML
    private TableColumn<DetalleVenta, Double> colPrecioUnit;
    @FXML
    private TableColumn<DetalleVenta, Double> colSubtotal;

    // Tabla Carrito Lateral
    @FXML
    private TableView<DetalleVenta> tablaCarrito;
    @FXML
    private TableColumn<DetalleVenta, String> colCarritoProducto;
    @FXML
    private TableColumn<DetalleVenta, Integer> colCarritoCant;
    @FXML
    private TableColumn<DetalleVenta, Double> colCarritoPrecio;
    @FXML
    private TableColumn<DetalleVenta, Double> colCarritoSubtotal;
    @FXML
    private Spinner<Integer> spinnerCantidadCarrito;
    @FXML
    private Label lblTotalCarrito;

    // Cliente y Formulario
    @FXML
    private ComboBox<Cliente> cmbCliente;
    @FXML
    private Spinner<Double> spinnerDescuento;
    @FXML
    private TextField txtSubtotal;
    @FXML
    private TextField txtTotal;

    private final ObservableList<DetalleVenta> listaCatalogo = FXCollections.observableArrayList();
    private final ObservableList<DetalleVenta> listaCarrito = FXCollections.observableArrayList();
    private final ObservableList<Cliente> listaClientesBD = FXCollections.observableArrayList();

    private final LibroDAO libroDAO = new LibroDAOImpl();
    private final ClienteDAO clienteDAO = new ClienteDAOImpl();
    private final VentaDAOImpl ventaDAO = new VentaDAOImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar Spinners
        if (spinnerCantidad != null && spinnerCantidad.getValueFactory() == null) {
            spinnerCantidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        }
        if (spinnerCantidadCarrito != null && spinnerCantidadCarrito.getValueFactory() == null) {
            spinnerCantidadCarrito.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        }
        if (spinnerDescuento != null && spinnerDescuento.getValueFactory() == null) {
            spinnerDescuento.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 100.0, 0.0, 5.0));
        }

        // Mapeo de columnas de la tabla principal
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecioUnit.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        tablaDetalleVenta.setItems(listaCatalogo);

        // Mapeo de columnas del carrito
        if (tablaCarrito != null) {
            colCarritoProducto.setCellValueFactory(new PropertyValueFactory<>("titulo"));
            colCarritoCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
            colCarritoPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
            colCarritoSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
            tablaCarrito.setItems(listaCarrito);
        }

        // Configurar despliegue de nombre de clientes en ComboBox
        configurarComboBoxClientes();

        // Recalcular descuento al cambiar valor
        if (spinnerDescuento != null) {
            spinnerDescuento.valueProperty().addListener((obs, oldValue, newValue) -> calcularTotales());
        }
    }

    private void configurarComboBoxClientes() {
        if (cmbCliente != null) {
            cmbCliente.setCellFactory(param -> new ListCell<Cliente>() {
                @Override
                protected void updateItem(Cliente item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNombre() + " " + item.getApellido());
                    }
                }
            });

            cmbCliente.setButtonCell(new ListCell<Cliente>() {
                @Override
                protected void updateItem(Cliente item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Consumidor Final");
                    } else {
                        setText(item.getNombre() + " " + item.getApellido());
                    }
                }
            });

            cargarClientes();
        }
    }

    private void cargarClientes() {
        List<Cliente> clientes = clienteDAO.listar();
        if (clientes != null) {
            listaClientesBD.setAll(clientes);
            cmbCliente.setItems(listaClientesBD);
        }
    }

    @FXML
    void buscarLibro(ActionEvent event) {
        String query = txtBusqueda.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            listaCatalogo.clear();
            return;
        }

        List<Libro> listaLibrosBD = libroDAO.listar();
        listaCatalogo.clear();

        if (listaLibrosBD != null) {
            for (Libro libro : listaLibrosBD) {
                if (libro.isActivo() && libro.getStockActual() > 0) {
                    if (libro.getIsbn().toLowerCase().contains(query) || libro.getTitulo().toLowerCase().contains(query)) {
                        DetalleVenta detalle = new DetalleVenta();
                        detalle.setIsbn(libro.getIsbn());
                        detalle.setTitulo(libro.getTitulo());
                        detalle.setCantidad(0);
                        detalle.setPrecioUnitario(libro.getPrecio());
                        detalle.setSubtotal(0.0);
                        listaCatalogo.add(detalle);
                    }
                }
            }
        }

        if (listaCatalogo.isEmpty()) {
            mostrarAlerta("No Encontrado", "No se encontraron libros coincidentes con: " + query, Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    void agregarAlCarrito(ActionEvent event) {
        String input = txtBusqueda.getText().trim();
        DetalleVenta itemSeleccionado = tablaDetalleVenta.getSelectionModel().getSelectedItem();

        if (itemSeleccionado == null && !input.isEmpty()) {
            for (DetalleVenta item : listaCatalogo) {
                if (item.getIsbn().equalsIgnoreCase(input) || item.getTitulo().equalsIgnoreCase(input)) {
                    itemSeleccionado = item;
                    break;
                }
            }
        }

        if (itemSeleccionado == null) {
            mostrarAlerta("Atención", "Busque y seleccione un libro de la lista primero.", Alert.AlertType.WARNING);
            return;
        }

        Libro libroBD = libroDAO.buscar(itemSeleccionado.getIsbn());
        if (libroBD == null || !libroBD.isActivo()) {
            mostrarAlerta("Error", "El libro no se encuentra disponible.", Alert.AlertType.ERROR);
            return;
        }

        int cantidadAgregar = (spinnerCantidad != null && spinnerCantidad.getValue() != null) ? spinnerCantidad.getValue() : 1;
        DetalleVenta itemEnCarrito = null;

        for (DetalleVenta cartItem : listaCarrito) {
            if (cartItem.getIsbn().equals(itemSeleccionado.getIsbn())) {
                itemEnCarrito = cartItem;
                break;
            }
        }

        int cantidadActual = (itemEnCarrito != null) ? itemEnCarrito.getCantidad() : 0;
        int nuevaCantidadTotal = cantidadActual + cantidadAgregar;

        if (nuevaCantidadTotal > libroBD.getStockActual()) {
            mostrarAlerta("Stock Insuficiente", "Solo hay " + libroBD.getStockActual() + " unidades disponibles.", Alert.AlertType.WARNING);
            return;
        }

        if (itemEnCarrito != null) {
            itemEnCarrito.setCantidad(nuevaCantidadTotal);
            itemEnCarrito.setSubtotal(nuevaCantidadTotal * itemEnCarrito.getPrecioUnitario());
        } else {
            DetalleVenta nuevoDetalle = new DetalleVenta();
            nuevoDetalle.setIsbn(itemSeleccionado.getIsbn());
            nuevoDetalle.setTitulo(itemSeleccionado.getTitulo());
            nuevoDetalle.setCantidad(cantidadAgregar);
            nuevoDetalle.setPrecioUnitario(itemSeleccionado.getPrecioUnitario());
            nuevoDetalle.setSubtotal(cantidadAgregar * itemSeleccionado.getPrecioUnitario());
            listaCarrito.add(nuevoDetalle);
        }

        if (tablaCarrito != null) tablaCarrito.refresh();
        calcularTotales();

        txtBusqueda.clear();
        if (spinnerCantidad != null && spinnerCantidad.getValueFactory() != null) {
            spinnerCantidad.getValueFactory().setValue(1);
        }
    }

    @FXML
    void quitarDelCarrito(ActionEvent event) {
        DetalleVenta seleccionado = (tablaCarrito != null) ? tablaCarrito.getSelectionModel().getSelectedItem() : null;
        if (seleccionado != null) {
            listaCarrito.remove(seleccionado);
            if (tablaCarrito != null) tablaCarrito.refresh();
            calcularTotales();
        } else {
            mostrarAlerta("Atención", "Seleccione un producto del carrito para quitarlo.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    void actualizarCantidadCarrito(ActionEvent event) {
        DetalleVenta seleccionado = (tablaCarrito != null) ? tablaCarrito.getSelectionModel().getSelectedItem() : null;
        if (seleccionado != null && spinnerCantidadCarrito != null) {
            int nuevaCant = spinnerCantidadCarrito.getValue();

            Libro libroBD = libroDAO.buscar(seleccionado.getIsbn());
            if (libroBD != null && nuevaCant > libroBD.getStockActual()) {
                mostrarAlerta("Stock Insuficiente", "Solo hay " + libroBD.getStockActual() + " unidades disponibles.", Alert.AlertType.WARNING);
                return;
            }

            seleccionado.setCantidad(nuevaCant);
            seleccionado.setSubtotal(nuevaCant * seleccionado.getPrecioUnitario());
            tablaCarrito.refresh();
            calcularTotales();
        }
    }

    @FXML
    void vaciarCarrito(ActionEvent event) {
        listaCarrito.clear();
        if (tablaCarrito != null) tablaCarrito.refresh();
        calcularTotales();
    }

    @FXML
    void registrarVenta(ActionEvent event) {
        if (listaCarrito.isEmpty()) {
            mostrarAlerta("Atención", "El carrito está vacío. Agregue productos antes de registrar la venta.", Alert.AlertType.WARNING);
            return;
        }

        Venta venta = new Venta();
        double subtotalVenta = Double.parseDouble(txtSubtotal.getText().replace(",", "."));
        double totalVenta = Double.parseDouble(txtTotal.getText().replace(",", "."));

        venta.setSubtotal(subtotalVenta);
        venta.setDescuento(subtotalVenta - totalVenta);
        venta.setTotal(totalVenta);
        venta.setEstado("COMPLETADA");

        Cliente clienteSeleccionadoBD = cmbCliente.getValue();

        if (clienteSeleccionadoBD != null) {
            venta.setCuiCliente(clienteSeleccionadoBD.getCui());
        } else {
            venta.setCuiCliente(0L); // Consumidor Final
        }

        venta.setIdUsuario(1);

        boolean exito = ventaDAO.guardarVentaConDetalles(venta, listaCarrito);

        if (!exito) {
            mostrarAlerta("Error", "No se pudo registrar la venta en la base de datos.", Alert.AlertType.ERROR);
            return;
        }

        mostrarComprobante(venta, clienteSeleccionadoBD);
    }

    private void mostrarComprobante(Venta venta, Cliente cliente) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Comprobante de Venta - Página Viva");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        Label lblEmpresa = new Label("Página Viva");
        lblEmpresa.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label lblDir = new Label("Dirección de la Tienda");
        Label lblTel = new Label("Tel: 2200-0000");

        Separator sep1 = new Separator();

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(5);

        infoGrid.add(new Label("No. Venta:"), 0, 0);
        infoGrid.add(new Label(String.valueOf(venta.getIdVenta())), 1, 0);

        infoGrid.add(new Label("Fecha:"), 0, 1);
        infoGrid.add(new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))), 1, 1);

        infoGrid.add(new Label("Cliente:"), 0, 2);

        String nombreClienteMostrar = (cliente != null)
                ? cliente.getNombre() + " " + cliente.getApellido()
                : "Consumidor Final";

        infoGrid.add(new Label(nombreClienteMostrar), 1, 2);

        TableView<DetalleVenta> tablaComprobante = new TableView<>(FXCollections.observableArrayList(listaCarrito));

        TableColumn<DetalleVenta, String> colProd = new TableColumn<>("Producto");
        colProd.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colProd.setPrefWidth(160);

        TableColumn<DetalleVenta, Integer> colCant = new TableColumn<>("Cant.");
        colCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCant.setPrefWidth(60);

        TableColumn<DetalleVenta, Double> colPrec = new TableColumn<>("P. Unit.");
        colPrec.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colPrec.setPrefWidth(80);

        TableColumn<DetalleVenta, Double> colSub = new TableColumn<>("Subtotal");
        colSub.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colSub.setPrefWidth(90);

        tablaComprobante.getColumns().addAll(colProd, colCant, colPrec, colSub);
        tablaComprobante.setPrefHeight(200);

        Separator sep2 = new Separator();

        GridPane totalesGrid = new GridPane();
        totalesGrid.setHgap(15);
        totalesGrid.setVgap(5);

        totalesGrid.add(new Label("Subtotal:"), 0, 0);
        totalesGrid.add(new Label("Q" + txtSubtotal.getText()), 1, 0);

        totalesGrid.add(new Label("Descuento:"), 0, 1);
        double descVal = (txtSubtotal.getText().isEmpty()) ? 0.0 : Double.parseDouble(txtSubtotal.getText().replace(",", ".")) - Double.parseDouble(txtTotal.getText().replace(",", "."));
        totalesGrid.add(new Label(String.format("Q%.2f", descVal)), 1, 1);

        Label lblTotNombre = new Label("Total:");
        lblTotNombre.setStyle("-fx-font-weight: bold;");
        Label lblTotValor = new Label("Q" + txtTotal.getText());
        lblTotValor.setStyle("-fx-font-weight: bold;");

        totalesGrid.add(lblTotNombre, 0, 2);
        totalesGrid.add(lblTotValor, 1, 2);

        Label lblGracias = new Label("¡Gracias por su compra!");
        lblGracias.setStyle("-fx-font-style: italic;");

        HBox botones = new HBox(10);
        botones.setAlignment(Pos.CENTER);

        Button btnImprimir = new Button("Imprimir");
        Button btnCerrar = new Button("Cerrar");

        btnCerrar.setOnAction(e -> {
            stage.close();
            limpiarFormulario();
        });

        botones.getChildren().addAll(btnImprimir, btnCerrar);

        layout.getChildren().addAll(lblEmpresa, lblDir, lblTel, sep1, infoGrid, tablaComprobante, sep2, totalesGrid, lblGracias, botones);

        Scene scene = new Scene(layout, 450, 600);
        stage.setScene(scene);
        stage.showAndWait();
    }

    @FXML
    private void cancelarVenta(ActionEvent event) {
        try {
            Main.cambiarVista("/org/paginalibre/view/CajeroDashboardView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calcularTotales() {
        double subtotalAcumulado = 0.0;
        for (DetalleVenta item : listaCarrito) {
            subtotalAcumulado += item.getSubtotal();
        }

        double porcentajeDescuento = (spinnerDescuento != null && spinnerDescuento.getValue() != null) ? spinnerDescuento.getValue() : 0.0;
        double descuento = subtotalAcumulado * (porcentajeDescuento / 100.0);
        double totalFinal = subtotalAcumulado - descuento;

        if (txtSubtotal != null) {
            txtSubtotal.setText(String.format("%.2f", subtotalAcumulado));
        }
        if (txtTotal != null) {
            txtTotal.setText(String.format("%.2f", totalFinal));
        }
        if (lblTotalCarrito != null) {
            lblTotalCarrito.setText(String.format("Total: Q%.2f", totalFinal));
        }
    }

    private void limpiarFormulario() {
        listaCatalogo.clear();
        listaCarrito.clear();

        if (tablaCarrito != null) tablaCarrito.refresh();

        if (cmbCliente != null) {
            cmbCliente.getSelectionModel().clearSelection();
        }

        if (txtBusqueda != null) {
            txtBusqueda.clear();
        }
        if (spinnerCantidad != null && spinnerCantidad.getValueFactory() != null) {
            spinnerCantidad.getValueFactory().setValue(1);
        }
        if (spinnerDescuento != null && spinnerDescuento.getValueFactory() != null) {
            spinnerDescuento.getValueFactory().setValue(0.0);
        }
        if (txtSubtotal != null) {
            txtSubtotal.setText("0.00");
        }
        if (txtTotal != null) {
            txtTotal.setText("0.00");
        }
        if (lblTotalCarrito != null) {
            lblTotalCarrito.setText("Total: Q0.00");
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