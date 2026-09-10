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
import org.paginalibre.dao.LibroDAO;
import org.paginalibre.dao.impl.LibroDAOImpl;
import org.paginalibre.model.DetalleVenta;
import org.paginalibre.model.Libro;
import org.paginalibre.system.Main;

public class VentaController implements Initializable {

    @FXML private TextField txtBusqueda;
    @FXML private Spinner<Integer> spinnerCantidad;

    @FXML private TableView<DetalleVenta> tablaDetalleVenta;
    @FXML private TableColumn<DetalleVenta, String> colIsbn;
    @FXML private TableColumn<DetalleVenta, String> colTitulo;
    @FXML private TableColumn<DetalleVenta, Integer> colCantidad;
    @FXML private TableColumn<DetalleVenta, Double> colPrecioUnit;
    @FXML private TableColumn<DetalleVenta, Double> colSubtotal;

    @FXML private ComboBox<String> cbCliente;
    @FXML private Spinner<Double> spinnerDescuento;
    @FXML private TextField txtSubtotal;
    @FXML private TextField txtTotal;

    private final ObservableList<DetalleVenta> listaTabla = FXCollections.observableArrayList();
    private final LibroDAO libroDAO = new LibroDAOImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (spinnerCantidad != null && spinnerCantidad.getValueFactory() == null) {
            spinnerCantidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        }
        if (spinnerDescuento != null && spinnerDescuento.getValueFactory() == null) {
            spinnerDescuento.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 100.0, 0.0, 5.0));
        }

        if (cbCliente != null) {
            cbCliente.setItems(FXCollections.observableArrayList("Consumidor Final", "Cliente Frecuente"));
            cbCliente.getSelectionModel().selectFirst();
        }

        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecioUnit.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        tablaDetalleVenta.setItems(listaTabla);

        if (spinnerDescuento != null) {
            spinnerDescuento.valueProperty().addListener((obs, oldValue, newValue) -> calcularTotales());
        }

        // Cargar todos los libros al entrar a la vista
        cargarLibrosIniciales();
    }

    private void cargarLibrosIniciales() {
        listaTabla.clear();
        List<Libro> listaLibrosBD = libroDAO.listar();

        if (listaLibrosBD != null && !listaLibrosBD.isEmpty()) {
            for (Libro libro : listaLibrosBD) {
                if (libro.isActivo() && libro.getStockActual() > 0) {
                    DetalleVenta detalle = new DetalleVenta();
                    detalle.setIsbn(libro.getIsbn());
                    detalle.setTitulo(libro.getTitulo());
                    detalle.setCantidad(0); // Cantidad 0 al inicio
                    detalle.setPrecioUnitario(libro.getPrecio());
                    detalle.setSubtotal(0.0);

                    listaTabla.add(detalle);
                }
            }
            calcularTotales();
        }
    }

    @FXML
    void buscarLibro(ActionEvent event) {
        String query = txtBusqueda.getText().trim().toLowerCase();
        
        if (query.isEmpty()) {
            cargarLibrosIniciales();
            return;
        }

        ObservableList<DetalleVenta> filtrados = FXCollections.observableArrayList();
        for (DetalleVenta item : listaTabla) {
            if (item.getIsbn().toLowerCase().contains(query) || item.getTitulo().toLowerCase().contains(query)) {
                filtrados.add(item);
            }
        }

        if (filtrados.isEmpty()) {
            mostrarAlerta("No Encontrado", "No se encontraron libros coincidentes con: " + query, Alert.AlertType.INFORMATION);
        } else {
            tablaDetalleVenta.setItems(filtrados);
        }
    }

    @FXML
    void agregarAlCarrito(ActionEvent event) {
        String input = txtBusqueda.getText().trim();
        DetalleVenta itemSeleccionado = tablaDetalleVenta.getSelectionModel().getSelectedItem();

        if (itemSeleccionado == null && !input.isEmpty()) {
            for (DetalleVenta item : listaTabla) {
                if (item.getIsbn().equalsIgnoreCase(input) || item.getTitulo().equalsIgnoreCase(input)) {
                    itemSeleccionado = item;
                    break;
                }
            }
        }

        if (itemSeleccionado == null) {
            mostrarAlerta("Atención", "Seleccione un libro de la lista o escriba su ISBN/Título exacto.", Alert.AlertType.WARNING);
            return;
        }

        Libro libroBD = libroDAO.buscar(itemSeleccionado.getIsbn());
        if (libroBD == null || !libroBD.isActivo()) {
            mostrarAlerta("Error", "El libro no se encuentra disponible.", Alert.AlertType.ERROR);
            return;
        }

        int cantidadAgregar = (spinnerCantidad != null && spinnerCantidad.getValue() != null) ? spinnerCantidad.getValue() : 1;
        int nuevaCantidadTotal = itemSeleccionado.getCantidad() + cantidadAgregar;

        if (nuevaCantidadTotal > libroBD.getStockActual()) {
            mostrarAlerta("Stock Insuficiente", "Solo hay " + libroBD.getStockActual() + " unidades disponibles.", Alert.AlertType.WARNING);
            return;
        }

        itemSeleccionado.setCantidad(nuevaCantidadTotal);
        itemSeleccionado.setSubtotal(nuevaCantidadTotal * itemSeleccionado.getPrecioUnitario());

        tablaDetalleVenta.refresh();
        calcularTotales();

        txtBusqueda.clear();
        tablaDetalleVenta.setItems(listaTabla);
        if (spinnerCantidad != null && spinnerCantidad.getValueFactory() != null) {
            spinnerCantidad.getValueFactory().setValue(1);
        }
    }

    @FXML
    void abrirModalCarrito(ActionEvent event) {
        ObservableList<DetalleVenta> productosEnCarrito = FXCollections.observableArrayList();
        for (DetalleVenta item : listaTabla) {
            if (item.getCantidad() > 0) {
                productosEnCarrito.add(item);
            }
        }

        if (productosEnCarrito.isEmpty()) {
            mostrarAlerta("Carrito Vacío", "No ha agregado unidades a ningún libro.", Alert.AlertType.INFORMATION);
            return;
        }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Panel de Caja y Ventas - Página Viva");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Label lblTitulo = new Label("Carrito de Venta");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableView<DetalleVenta> tablaCarritoModal = new TableView<>(productosEnCarrito);
        
        TableColumn<DetalleVenta, String> colProd = new TableColumn<>("Producto");
        colProd.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colProd.setPrefWidth(220);

        TableColumn<DetalleVenta, Integer> colCant = new TableColumn<>("Cantidad");
        colCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCant.setPrefWidth(90);

        TableColumn<DetalleVenta, Double> colPrec = new TableColumn<>("Precio Unit.");
        colPrec.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colPrec.setPrefWidth(110);

        TableColumn<DetalleVenta, Double> colSub = new TableColumn<>("Subtotal");
        colSub.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colSub.setPrefWidth(110);

        tablaCarritoModal.getColumns().addAll(colProd, colCant, colPrec, colSub);
        tablaCarritoModal.setPrefHeight(250);

        HBox controles = new HBox(10);
        controles.setAlignment(Pos.CENTER_LEFT);

        Label lblCant = new Label("Cantidad:");
        Spinner<Integer> spCantModal = new Spinner<>(1, 100, 1);
        spCantModal.setPrefWidth(70);

        // Actualizar el Spinner con la cantidad del producto seleccionado
        tablaCarritoModal.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && newSelection.getCantidad() > 0) {
                spCantModal.getValueFactory().setValue(newSelection.getCantidad());
            }
        });

        Button btnEliminar = new Button("Quitar del Carrito");
        Button btnActualizar = new Button("Actualizar Cantidad");

        btnEliminar.setOnAction(e -> {
            DetalleVenta seleccionado = tablaCarritoModal.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                seleccionado.setCantidad(0);
                seleccionado.setSubtotal(0.0);
                productosEnCarrito.remove(seleccionado);
                tablaDetalleVenta.refresh();
                calcularTotales();
            }
        });

        btnActualizar.setOnAction(e -> {
            DetalleVenta seleccionado = tablaCarritoModal.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                seleccionado.setCantidad(spCantModal.getValue());
                seleccionado.setSubtotal(spCantModal.getValue() * seleccionado.getPrecioUnitario());
                tablaCarritoModal.refresh();
                tablaDetalleVenta.refresh();
                calcularTotales();
            }
        });

        controles.getChildren().addAll(lblCant, spCantModal, btnEliminar, btnActualizar);

        HBox pieModal = new HBox(15);
        pieModal.setAlignment(Pos.CENTER_RIGHT);

        Label lblTotalModal = new Label("Total: Q" + txtTotal.getText());
        lblTotalModal.setStyle("-fx-font-weight: bold;");

        Button btnVaciar = new Button("Vaciar Carrito");
        btnVaciar.setOnAction(e -> {
            for (DetalleVenta item : listaTabla) {
                item.setCantidad(0);
                item.setSubtotal(0.0);
            }
            productosEnCarrito.clear();
            tablaDetalleVenta.refresh();
            calcularTotales();
            lblTotalModal.setText("Total: Q0.00");
        });

        Button btnContinuar = new Button("Continuar a Venta");
        btnContinuar.setOnAction(e -> stage.close());

        pieModal.getChildren().addAll(lblTotalModal, btnVaciar, btnContinuar);

        layout.getChildren().addAll(lblTitulo, tablaCarritoModal, controles, pieModal);

        Scene scene = new Scene(layout, 600, 450);
        stage.setScene(scene);
        stage.showAndWait();
    }

    @FXML
    void registrarVenta(ActionEvent event) {
        ObservableList<DetalleVenta> itemsAComprar = FXCollections.observableArrayList();
        for (DetalleVenta item : listaTabla) {
            if (item.getCantidad() > 0) {
                itemsAComprar.add(item);
            }
        }

        if (itemsAComprar.isEmpty()) {
            mostrarAlerta("Atención", "El carrito está vacío. Asigne cantidades a los productos antes de registrar la venta.", Alert.AlertType.WARNING);
            return;
        }

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
        infoGrid.add(new Label("000001"), 1, 0);

        infoGrid.add(new Label("Fecha:"), 0, 1);
        infoGrid.add(new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))), 1, 1);

        infoGrid.add(new Label("Cliente:"), 0, 2);
        String clienteSel = (cbCliente != null && cbCliente.getValue() != null) ? cbCliente.getValue() : "Consumidor Final";
        infoGrid.add(new Label(clienteSel), 1, 2);

        TableView<DetalleVenta> tablaComprobante = new TableView<>(itemsAComprar);
        
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

    @FXML private Button btnQuitar;

    @FXML
    void quitarProducto(ActionEvent event) {
        DetalleVenta seleccionado = tablaDetalleVenta.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            if (seleccionado.getCantidad() > 0) {
                seleccionado.setCantidad(0);
                seleccionado.setSubtotal(0.0);
                tablaDetalleVenta.refresh();
                calcularTotales();
            } else {
                mostrarAlerta("Atención", "El producto seleccionado ya está en 0.", Alert.AlertType.INFORMATION);
            }
        } else {
            mostrarAlerta("Atención", "Seleccione un producto de la tabla para quitarlo.", Alert.AlertType.WARNING);
        }
    }

    private void calcularTotales() {
        double subtotalAcumulado = 0.0;
        for (DetalleVenta item : listaTabla) {
            if (item.getCantidad() > 0) {
                subtotalAcumulado += item.getSubtotal();
            }
        }

        double porcentajeDescuento = (spinnerDescuento != null && spinnerDescuento.getValue() != null) ? spinnerDescuento.getValue() : 0.0;
        double descuento = subtotalAcumulado * (porcentajeDescuento / 100.0);
        double totalFinal = subtotalAcumulado - descuento;

        if (txtSubtotal != null) txtSubtotal.setText(String.format("%.2f", subtotalAcumulado));
        if (txtTotal != null) txtTotal.setText(String.format("%.2f", totalFinal));
    }

    private void limpiarFormulario() {
        cargarLibrosIniciales();
        if (txtBusqueda != null) txtBusqueda.clear();
        if (spinnerCantidad != null && spinnerCantidad.getValueFactory() != null) spinnerCantidad.getValueFactory().setValue(1);
        if (spinnerDescuento != null && spinnerDescuento.getValueFactory() != null) spinnerDescuento.getValueFactory().setValue(0.0);
        if (txtSubtotal != null) txtSubtotal.setText("0.00");
        if (txtTotal != null) txtTotal.setText("0.00");
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}