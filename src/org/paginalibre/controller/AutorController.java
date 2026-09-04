package org.paginalibre.controller; // O el paquete org.paginalibre.controller según manejes tu controlador

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.paginalibre.dao.AutorDAO;
import org.paginalibre.dao.impl.AutorDAOImpl;
import org.paginalibre.model.Autor;

public class AutorController implements Initializable {

    @FXML private TableView<Autor> tablaAutores;
    @FXML private TableColumn<Autor, Integer> colId;
    @FXML private TableColumn<Autor, String> colNombre;
    @FXML private TableColumn<Autor, String> colApellido;
    @FXML private TableColumn<Autor, String> colNacionalidad;
    @FXML private TableColumn<Autor, String> colBiografia;

    @FXML private TextField txtNombreAutor;
    @FXML private TextField txtApellidoAutor;
    @FXML private TextField txtNacionalidad;
    @FXML private TextField txtBiografia;
    @FXML private Label lblMensaje;

    private final AutorDAO autorDAO = new AutorDAOImpl();
    private ObservableList<Autor> listaAutores;
    private Autor autorSeleccionado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idAutor"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreAutor"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellidoAutor"));
        colNacionalidad.setCellValueFactory(new PropertyValueFactory<>("nacionalidad"));
        colBiografia.setCellValueFactory(new PropertyValueFactory<>("biografia"));

        cargarDatos();

        tablaAutores.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                autorSeleccionado = newSel;
                txtNombreAutor.setText(newSel.getNombreAutor());
                txtApellidoAutor.setText(newSel.getApellidoAutor());
                txtNacionalidad.setText(newSel.getNacionalidad());
                txtBiografia.setText(newSel.getBiografia());
            }
        });
    }

    public void cargarDatos() {
        listaAutores = FXCollections.observableArrayList(autorDAO.listar());
        tablaAutores.setItems(listaAutores);
    }

    @FXML
    public void handleGuardar() {
        if (!txtNombreAutor.getText().trim().isEmpty()) {
            Autor autor = new Autor(
                txtNombreAutor.getText(),
                txtApellidoAutor.getText(),
                txtNacionalidad.getText(),
                txtBiografia.getText()
            );
            if (autorDAO.insertar(autor)) {
                lblMensaje.setText("Autor guardado correctamente");
                cargarDatos();
                handleLimpiar();
            } else {
                lblMensaje.setText("Error al guardar autor");
            }
        }
    }

    @FXML
    public void handleActualizar() {
        cargarDatos();
        lblMensaje.setText("Tabla actualizada");
    }

    @FXML
    public void handleLimpiar() {
        txtNombreAutor.clear();
        txtApellidoAutor.clear();
        txtNacionalidad.clear();
        txtBiografia.clear();
        tablaAutores.getSelectionModel().clearSelection();
        autorSeleccionado = null;
    }

    @FXML
    public void handleVolver() {
        // Lógica para regresar al menú si aplica
    }
}