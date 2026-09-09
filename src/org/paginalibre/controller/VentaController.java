package org.paginalibre.controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import org.paginalibre.model.DetalleVenta;
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
    @FXML private TableColumn<DetalleVenta, Void> colAccion;

    @FXML private ComboBox<String> cbCliente;
    @FXML private Spinner<Double> spinnerDescuento;
    @FXML private TextField txtSubtotal;
    @FXML private TextField txtTotal;

    private ObservableList<DetalleVenta> listaCarrito = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuración de Spinners y Clientes
        spinnerCantidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        spinnerDescuento.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 100.0, 0.0, 5.0));

        if (cbCliente != null) {
            cbCliente.setItems(FXCollections.observableArrayList("Consumidor Final", "Cliente Frecuente"));
            cbCliente.getSelectionModel().selectFirst();
        }

        // Binding de columnas
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecioUnit.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        configurarColumnaAccion();

        tablaDetalleVenta.setItems(listaCarrito);
        spinnerDescuento.valueProperty().addListener((obs, oldValue, newValue) -> calcularTotales());
    }

    @FXML
    void buscarLibro(ActionEvent event) {
        String isbnInput = txtBusqueda.getText().trim();
        if (isbnInput.isEmpty()) {
            mostrarAlerta("Advertencia", "Ingrese un código o ISBN para simular la búsqueda.", Alert.AlertType.WARNING);
            return;
        }
        mostrarAlerta("Búsqueda Lista", "Libro " + isbnInput + " listo para agregar al detalle.", Alert.AlertType.INFORMATION);
    }

    @FXML
    void agregarAlCarrito(ActionEvent event) {
        String isbnInput = txtBusqueda.getText().trim();
        if (isbnInput.isEmpty()) {
            mostrarAlerta("Atención", "Escriba un ISBN/Código antes de agregar.", Alert.AlertType.WARNING);
            return;
        }

        int cantidad = spinnerCantidad.getValue();
        double precioSimulado = 150.00;

        DetalleVenta detalle = new DetalleVenta();
        detalle.setIsbn(isbnInput);
        detalle.setTitulo("Libro Muestra (" + isbnInput + ")");
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(precioSimulado);
        detalle.setSubtotal(cantidad * precioSimulado);

        listaCarrito.add(detalle);
        calcularTotales();

        txtBusqueda.clear();
        spinnerCantidad.getValueFactory().setValue(1);
    }

    // Modal del Carrito de Venta (Imagen 2)
    @FXML
    void abrirModalCarrito(ActionEvent event) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Panel de Caja y Ventas - Página Viva");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Label lblTitulo = new Label("Carrito de Venta");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableView<DetalleVenta> tablaCarritoModal = new TableView<>(listaCarrito);
        
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

        // Fila de Controles Inferiores
        HBox controles = new HBox(10);
        controles.setAlignment(Pos.CENTER_LEFT);

        Label lblCant = new Label("Cantidad:");
        Spinner<Integer> spCantModal = new Spinner<>(1, 100, 1);
        spCantModal.setPrefWidth(70);

        Button btnAgregar = new Button("Agregar Producto");
        Button btnEliminar = new Button("Eliminar Producto");
        Button btnActualizar = new Button("Actualizar Cantidad");

        btnEliminar.setOnAction(e -> {
            DetalleVenta seleccionado = tablaCarritoModal.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                listaCarrito.remove(seleccionado);
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

        controles.getChildren().addAll(lblCant, spCantModal, btnAgregar, btnEliminar, btnActualizar);

        // Fila Inferior de Totales y Salida
        HBox pieModal = new HBox(15);
        pieModal.setAlignment(Pos.CENTER_RIGHT);

        Label lblTotalModal = new Label("Total: " + txtTotal.getText());
        lblTotalModal.setStyle("-fx-font-weight: bold;");

        Button btnVaciar = new Button("Vaciar Carrito");
        btnVaciar.setOnAction(e -> {
            listaCarrito.clear();
            calcularTotales();
            lblTotalModal.setText("Total: 0.00");
        });

        Button btnContinuar = new Button("Continuar a Venta");
        btnContinuar.setOnAction(e -> stage.close());

        pieModal.getChildren().addAll(lblTotalModal, btnVaciar, btnContinuar);

        layout.getChildren().addAll(lblTitulo, tablaCarritoModal, controles, pieModal);

        Scene scene = new Scene(layout, 600, 450);
        stage.setScene(scene);
        stage.showAndWait();
    }

    // Modal de Factura / Ticket (Imagen 4)
    @FXML
    void registrarVenta(ActionEvent event) {
        if (listaCarrito.isEmpty()) {
            mostrarAlerta("Atención", "El carrito está vacío. Agregue productos antes de registrar.", Alert.AlertType.WARNING);
            return;
        }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Panel de Caja y Ventas - Página Viva");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        Label lblEmpresa = new Label("Nombre de la Libreria");
        lblEmpresa.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label lblDir = new Label("Direccion de la tienda");
        Label lblTel = new Label("Tel: 0000-0000");

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

        infoGrid.add(new Label("Vendedor:"), 0, 3);
        infoGrid.add(new Label("-"), 1, 3);

        // Tabla del Comprobante
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

        Label lblGracias = new Label("Gracias por su compra");
        lblGracias.setStyle("-fx-font-style: italic;");

        HBox botones = new HBox(10);
        botones.setAlignment(Pos.CENTER);

        Button btnImprimir = new Button("Imprimir");
        Button btnVistaPrevia = new Button("Vista Previa");
        Button btnCerrar = new Button("Cerrar");

        btnCerrar.setOnAction(e -> {
            stage.close();
            limpiarFormulario();
        });

        botones.getChildren().addAll(btnImprimir, btnVistaPrevia, btnCerrar);

        layout.getChildren().addAll(lblEmpresa, lblDir, lblTel, sep1, infoGrid, tablaComprobante, sep2, totalesGrid, lblGracias, botones);

        Scene scene = new Scene(layout, 450, 620);
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

    private void configurarColumnaAccion() {
        if (colAccion == null) return;

        colAccion.setCellFactory(param -> new TableCell<>() {
            private final Button btnEliminar = new Button("Eliminar");

            {
                btnEliminar.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                btnEliminar.setOnAction(event -> {
                    DetalleVenta item = getTableView().getItems().get(getIndex());
                    listaCarrito.remove(item);
                    calcularTotales();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnEliminar);
                }
            }
        });
    }

    private void calcularTotales() {
        double subtotalAcumulado = 0.0;
        for (DetalleVenta item : listaCarrito) {
            subtotalAcumulado += item.getSubtotal();
        }

        double porcentajeDescuento = (spinnerDescuento != null && spinnerDescuento.getValue() != null) ? spinnerDescuento.getValue() : 0.0;
        double descuento = subtotalAcumulado * (porcentajeDescuento / 100.0);
        double totalFinal = subtotalAcumulado - descuento;

        if (txtSubtotal != null) txtSubtotal.setText(String.format("%.2f", subtotalAcumulado));
        if (txtTotal != null) txtTotal.setText(String.format("%.2f", totalFinal));
    }

    private void limpiarFormulario() {
        listaCarrito.clear();
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