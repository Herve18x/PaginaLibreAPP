package org.paginalibre.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.paginalibre.dao.UsuarioDAO;
import org.paginalibre.dao.impl.UsuarioDAOImpl;
import org.paginalibre.model.Usuario;

public class UsuarioController {

    // Componentes de la Tabla
    @FXML private TableView<Usuario> tblUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colApellido;
    @FXML private TableColumn<Usuario, String> colCorreo;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, Boolean> colActivo;

    // Componentes del Formulario
    @FXML private Label lblTituloFormulario;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtCorreo;
    @FXML private ComboBox<String> cmbRol;

    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private final ObservableList<Usuario> listaUsuarios = FXCollections.observableArrayList();
    private Usuario usuarioEdicion = null;

    @FXML
    public void initialize() {
        tblUsuarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colUsername.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colApellido.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellido()));
        colCorreo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCorreo()));
        colRol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRol()));
        colActivo.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isActivo()));

        cmbRol.setItems(FXCollections.observableArrayList("admin", "bodega", "cajero"));

        // Listener para cargar automáticamente el usuario seleccionado en el formulario
        tblUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cargarParaEditar(newSelection);
            }
        });

        cargarUsuarios();
    }

    public void cargarUsuarios() {
        listaUsuarios.clear();
        listaUsuarios.addAll(usuarioDAO.listar());
        tblUsuarios.setItems(listaUsuarios);
    }

    private void cargarParaEditar(Usuario usuario) {
        this.usuarioEdicion = usuario;
        lblTituloFormulario.setText("EDITAR USUARIO (ID: " + usuario.getId() + ")");
        txtUsername.setText(usuario.getUsername());
        txtNombre.setText(usuario.getNombre());
        txtApellido.setText(usuario.getApellido());
        txtCorreo.setText(usuario.getCorreo());
        cmbRol.setValue(usuario.getRol());
        txtPassword.clear();
    }

    @FXML
    private void guardarUsuario() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String correo = txtCorreo.getText().trim();
        String rol = cmbRol.getValue();

        if (username.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || rol == null) {
            mostrarAlerta("Campos Requeridos", "Por favor complete usuario, nombre, apellido y rol.", Alert.AlertType.WARNING);
            return;
        }

        if (usuarioEdicion == null) {
            if (password.isEmpty()) {
                mostrarAlerta("Campo Requerido", "Debe ingresar una contraseña para el nuevo usuario.", Alert.AlertType.WARNING);
                return;
            }
            Usuario nuevo = new Usuario();
            nuevo.setUsername(username);
            nuevo.setPasswordHash(password);
            nuevo.setRol(rol);
            nuevo.setNombre(nombre);
            nuevo.setApellido(apellido);
            nuevo.setCorreo(correo);

            if (usuarioDAO.insertar(nuevo)) {
                mostrarAlerta("Éxito", "Usuario registrado correctamente.", Alert.AlertType.INFORMATION);
                cargarUsuarios();
                limpiarFormulario();
            } else {
                mostrarAlerta("Error", "No se pudo registrar el usuario.", Alert.AlertType.ERROR);
            }
        } else {
            usuarioEdicion.setUsername(username);
            usuarioEdicion.setNombre(nombre);
            usuarioEdicion.setApellido(apellido);
            usuarioEdicion.setCorreo(correo);
            usuarioEdicion.setRol(rol);

            if (!password.isEmpty()) {
                usuarioEdicion.setPasswordHash(password);
            }

            if (usuarioDAO.actualizar(usuarioEdicion)) {
                mostrarAlerta("Éxito", "Usuario actualizado correctamente.", Alert.AlertType.INFORMATION);
                cargarUsuarios();
                limpiarFormulario();
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el usuario.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void limpiarFormulario() {
        tblUsuarios.getSelectionModel().clearSelection();
        this.usuarioEdicion = null;
        lblTituloFormulario.setText("REGISTRAR NUEVO USUARIO");
        txtUsername.clear();
        txtPassword.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtCorreo.clear();
        cmbRol.setValue(null);
    }

    @FXML
    private void toggleEstado() {
        Usuario seleccionado = tblUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            seleccionado.setActivo(!seleccionado.isActivo());
            if (usuarioDAO.actualizar(seleccionado)) {
                tblUsuarios.refresh();
            } else {
                mostrarAlerta("Error", "No se pudo cambiar el estado del usuario.", Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Selección Requerida", "Seleccione un usuario de la tabla para cambiar su estado.", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) tblUsuarios.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}