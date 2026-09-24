package org.paginalibre.controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import org.paginalibre.model.Usuario;
import org.paginalibre.system.Main;
import org.paginalibre.util.Conexion;

public class NuevoLibroController implements Initializable {

    @FXML private TextField txtIsbn;
    @FXML private TextField txtTitulo;
    @FXML private DatePicker dpFechaPublicacion;
    @FXML private TextField txtPrecio;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbEditorial;
    @FXML private ComboBox<String> cmbAutor;
    @FXML private TextField txtStockActual;
    @FXML private TextField txtStockMinimo;
    @FXML private CheckBox chkEstado;

    private final Map<Integer, String> autores = new LinkedHashMap<>();
    private final Map<Integer, String> categorias = new LinkedHashMap<>();
    private final Map<String, String> editoriales = new LinkedHashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chkEstado.setSelected(true);
        txtStockMinimo.setText("5");

        configurarValidaciones();
        cargarCategorias();
        cargarAutores();
        cargarEditoriales();

        Usuario usuario = Main.getUsuarioSesion();
        boolean esAdmin = usuario != null && "admin".equalsIgnoreCase(usuario.getRol());
        txtStockMinimo.setDisable(!esAdmin);
        txtStockMinimo.setEditable(esAdmin);

        cmbAutor.setEditable(false);
        cmbEditorial.setEditable(false);
    }

    private void configurarValidaciones() {
        txtIsbn.setTextFormatter(new TextFormatter<String>(change -> {
            String nuevo = change.getControlNewText();
            return nuevo.matches("[0-9-]{0,20}") ? change : null;
        }));
        txtTitulo.setTextFormatter(new TextFormatter<String>(change -> {
            String nuevo = change.getControlNewText();
            return nuevo.matches("[\\p{L}\\p{N}\\s.,:;!?¿¡()'\"-]{0,100}") ? change : null;
        }));

        txtPrecio.setTextFormatter(new TextFormatter<String>(change -> {
            String nuevo = change.getControlNewText();
            return nuevo.matches("\\d{0,6}([.,]\\d{0,2})?") ? change : null;
        }));

        txtStockActual.setTextFormatter(new TextFormatter<String>(change -> {
            String nuevo = change.getControlNewText();
            return nuevo.matches("\\d{0,10}") ? change : null;
        }));

        txtStockMinimo.setTextFormatter(new TextFormatter<String>(change -> {
            String nuevo = change.getControlNewText();
            return nuevo.matches("\\d{0,10}") ? change : null;
        }));
    }

    private void cargarCategorias() {
        categorias.clear();
        cmbCategoria.getItems().clear();
        String sql = "SELECT categoria_id, nombre_categoria FROM categoria ORDER BY nombre_categoria";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("categoria_id");
                String nombre = rs.getString("nombre_categoria");
                categorias.put(id, nombre);
                cmbCategoria.getItems().add(id + " - " + nombre);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar las categorías: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarAutores() {
        autores.clear();
        cmbAutor.getItems().clear();

        String sql = "SELECT id_autor, nombre_autor, apellido_autor FROM autores "
                   + "ORDER BY apellido_autor, nombre_autor";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_autor");
                String nombre = rs.getString("nombre_autor") + " " + rs.getString("apellido_autor");
                autores.put(id, nombre);
                cmbAutor.getItems().add(nombre);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los autores: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarEditoriales() {
        editoriales.clear();
        cmbEditorial.getItems().clear();

        String sql = "SELECT nit, nombre_editorial FROM editoriales ORDER BY nombre_editorial";

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String nit = rs.getString("nit");
                String nombre = rs.getString("nombre_editorial");
                editoriales.put(nit, nombre);
                cmbEditorial.getItems().add(nombre + " (" + nit + ")");
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar las editoriales: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void nuevoAutor(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nuevo autor");
        dialog.setHeaderText("Agregar autor al catálogo");

        TextField nombre = new TextField();
        TextField apellido = new TextField();
        TextField nacionalidad = new TextField();
        TextArea biografia = new TextArea();
        biografia.setPrefRowCount(3);

        aplicarValidadorNombre(nombre);
        aplicarValidadorNombre(apellido);
        aplicarValidadorNombre(nacionalidad);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nombre *"), 0, 0);
        grid.add(nombre, 1, 0);
        grid.add(new Label("Apellido *"), 0, 1);
        grid.add(apellido, 1, 1);
        grid.add(new Label("Nacionalidad"), 0, 2);
        grid.add(nacionalidad, 1, 2);
        grid.add(new Label("Biografía"), 0, 3);
        grid.add(biografia, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (nombre.getText().trim().isEmpty() || apellido.getText().trim().isEmpty()) {
                    mostrarAlerta("Datos incompletos", "Nombre y apellido son obligatorios.", Alert.AlertType.WARNING);
                    return null;
                }
                String sql = "INSERT INTO autores(nombre_autor, apellido_autor, nacionalidad, biografia) VALUES (?, ?, ?, ?)";
                try (Connection conn = Conexion.getInstancia().conectar();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, nombre.getText().trim());
                    ps.setString(2, apellido.getText().trim());
                    ps.setString(3, nacionalidad.getText().trim().isEmpty() ? null : nacionalidad.getText().trim());
                    ps.setString(4, biografia.getText().trim().isEmpty() ? null : biografia.getText().trim());
                    ps.executeUpdate();
                    return ButtonType.OK;
                } catch (SQLException e) {
                    mostrarAlerta("Error", "No se pudo agregar el autor: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            cargarAutores();
            String nuevoNombre = nombre.getText().trim() + " " + apellido.getText().trim();
            cmbAutor.getSelectionModel().select(nuevoNombre);
        });
    }

    @FXML
    private void nuevaCategoria(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nueva categoría");
        dialog.setHeaderText("Agregar categoría");
        dialog.setContentText("Nombre:");
        dialog.showAndWait().ifPresent(valor -> {
            String nombre = valor.trim();
            if (nombre.isEmpty()) {
                mostrarAlerta("Datos incompletos", "El nombre es obligatorio.", Alert.AlertType.WARNING);
                return;
            }
            try (Connection conn = Conexion.getInstancia().conectar();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO categoria(nombre_categoria) VALUES (?)", java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nombre);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        cargarCategorias();
                        cmbCategoria.getSelectionModel().select(rs.getInt(1) + " - " + nombre);
                    }
                }
            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudo agregar la categoría: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void editarCategoria(ActionEvent event) {
        if (categorias.isEmpty()) return;
        java.util.List<String> opciones = new java.util.ArrayList<>(cmbCategoria.getItems());
        ChoiceDialog<String> selector = new ChoiceDialog<>(cmbCategoria.getValue() == null ? opciones.get(0) : cmbCategoria.getValue(), opciones);
        selector.setTitle("Editar categoría");
        selector.setHeaderText("Selecciona una categoría");
        selector.showAndWait().ifPresent(elegida -> {
            int id = Integer.parseInt(elegida.split(" - ", 2)[0]);
            TextInputDialog dialog = new TextInputDialog(categorias.get(id));
            dialog.setTitle("Editar categoría");
            dialog.setHeaderText("Categoría #" + id);
            dialog.setContentText("Nombre:");
            dialog.showAndWait().ifPresent(valor -> {
                if (valor.trim().isEmpty()) {
                    mostrarAlerta("Datos incompletos", "El nombre es obligatorio.", Alert.AlertType.WARNING);
                    return;
                }
                try (Connection conn = Conexion.getInstancia().conectar();
                     PreparedStatement ps = conn.prepareStatement("UPDATE categoria SET nombre_categoria = ? WHERE categoria_id = ?")) {
                    ps.setString(1, valor.trim());
                    ps.setInt(2, id);
                    ps.executeUpdate();
                    cargarCategorias();
                    cmbCategoria.getSelectionModel().select(id + " - " + valor.trim());
                } catch (SQLException e) {
                    mostrarAlerta("Error", "No se pudo editar la categoría: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            });
        });
    }

    @FXML
    private void editarAutor(ActionEvent event) {
        if (autores.isEmpty()) return;
        java.util.List<String> opciones = new java.util.ArrayList<>();
        autores.forEach((id, nombre) -> opciones.add(id + " - " + nombre));
        ChoiceDialog<String> selector = new ChoiceDialog<>(opciones.get(0), opciones);
        selector.setTitle("Editar autor");
        selector.setHeaderText("Selecciona un autor");
        selector.showAndWait().ifPresent(elegido -> {
            int id = Integer.parseInt(elegido.split(" - ", 2)[0]);
            try (Connection conn = Conexion.getInstancia().conectar();
                 PreparedStatement ps = conn.prepareStatement("SELECT nombre_autor, apellido_autor, nacionalidad, biografia FROM autores WHERE id_autor = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return;
                    TextField nombre = new TextField(rs.getString("nombre_autor"));
                    TextField apellido = new TextField(rs.getString("apellido_autor"));
                    TextField nacionalidad = new TextField(rs.getString("nacionalidad") == null ? "" : rs.getString("nacionalidad"));
                    TextArea biografia = new TextArea(rs.getString("biografia") == null ? "" : rs.getString("biografia"));
                    biografia.setPrefRowCount(3);
                    GridPane grid = new GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);
                    grid.addRow(0, new Label("Nombre *"), nombre);
                    grid.addRow(1, new Label("Apellido *"), apellido);
                    grid.addRow(2, new Label("Nacionalidad"), nacionalidad);
                    grid.addRow(3, new Label("Biografía"), biografia);
                    Dialog<ButtonType> dialog = new Dialog<>();
                    dialog.setTitle("Editar autor");
                    dialog.setHeaderText("Autor #" + id);
                    dialog.getDialogPane().setContent(grid);
                    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
                    dialog.setResultConverter(btn -> {
                        if (btn != ButtonType.OK) return null;
                        if (nombre.getText().trim().isEmpty() || apellido.getText().trim().isEmpty()) {
                            mostrarAlerta("Datos incompletos", "Nombre y apellido son obligatorios.", Alert.AlertType.WARNING);
                            return null;
                        }
                        try (Connection updateConn = Conexion.getInstancia().conectar();
                             PreparedStatement update = updateConn.prepareStatement("UPDATE autores SET nombre_autor = ?, apellido_autor = ?, nacionalidad = ?, biografia = ? WHERE id_autor = ?")) {
                            update.setString(1, nombre.getText().trim());
                            update.setString(2, apellido.getText().trim());
                            update.setString(3, nacionalidad.getText().trim().isEmpty() ? null : nacionalidad.getText().trim());
                            update.setString(4, biografia.getText().trim().isEmpty() ? null : biografia.getText().trim());
                            update.setInt(5, id);
                            update.executeUpdate();
                            return ButtonType.OK;
                        } catch (SQLException e) {
                            mostrarAlerta("Error", "No se pudo editar el autor: " + e.getMessage(), Alert.AlertType.ERROR);
                            return null;
                        }
                    });
                    dialog.showAndWait().ifPresent(resultado -> {
                        cargarAutores();
                        cmbAutor.getSelectionModel().select(autores.get(id));
                    });
                }
            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudo cargar el autor: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void nuevoEditorial(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nueva editorial");
        dialog.setHeaderText("Agregar editorial al catálogo");

        TextField nit = new TextField();
        TextField nombre = new TextField();
        TextField telefono = new TextField();
        TextField direccion = new TextField();

        nit.setTextFormatter(new TextFormatter<String>(change ->
            change.getControlNewText().matches("[A-Za-z0-9-]{0,20}") ? change : null));
        telefono.setTextFormatter(new TextFormatter<String>(change ->
            change.getControlNewText().matches("\\d{0,15}") ? change : null));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("NIT *"), 0, 0);
        grid.add(nit, 1, 0);
        grid.add(new Label("Nombre *"), 0, 1);
        grid.add(nombre, 1, 1);
        grid.add(new Label("Teléfono"), 0, 2);
        grid.add(telefono, 1, 2);
        grid.add(new Label("Dirección"), 0, 3);
        grid.add(direccion, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (nit.getText().trim().isEmpty() || nombre.getText().trim().isEmpty()) {
                    mostrarAlerta("Datos incompletos", "NIT y nombre son obligatorios.", Alert.AlertType.WARNING);
                    return null;
                }
                String sql = "INSERT INTO editoriales(nit, nombre_editorial, telefono_editorial, direccion_editorial) VALUES (?, ?, ?, ?)";
                try (Connection conn = Conexion.getInstancia().conectar();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, nit.getText().trim());
                    ps.setString(2, nombre.getText().trim());
                    ps.setString(3, telefono.getText().trim().isEmpty() ? null : telefono.getText().trim());
                    ps.setString(4, direccion.getText().trim().isEmpty() ? null : direccion.getText().trim());
                    ps.executeUpdate();
                    return ButtonType.OK;
                } catch (SQLException e) {
                    mostrarAlerta("Error", "No se pudo agregar la editorial: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            cargarEditoriales();
            String nitCreado = nit.getText().trim();
            String nombreCreado = editoriales.get(nitCreado);
            if (nombreCreado != null) {
                cmbEditorial.getSelectionModel().select(nombreCreado + " (" + nitCreado + ")");
            }
        });
    }

    private void aplicarValidadorNombre(TextField campo) {
        campo.setTextFormatter(new TextFormatter<String>(change -> {
            String nuevo = change.getControlNewText();
            return nuevo.matches("[\\p{L}\\s.'-]{0,100}") ? change : null;
        }));
    }

    @FXML
    private void guardarLibro(ActionEvent event) {
        String isbn = txtIsbn.getText().trim();
        String titulo = txtTitulo.getText().trim();
        String precioTexto = txtPrecio.getText().trim().replace(",", ".");
        String categoriaTexto = cmbCategoria.getValue();
        String stockTexto = txtStockActual.getText().trim();

        if (isbn.isEmpty() || titulo.isEmpty() || precioTexto.isEmpty()
                || categoriaTexto == null || categoriaTexto.isBlank() || stockTexto.isEmpty()) {
            mostrarAlerta("Campos incompletos", "Completa todos los campos obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        if (cmbAutor.getValue() == null || cmbAutor.getValue().trim().isEmpty()) {
            mostrarAlerta("Autor requerido", "Selecciona un autor o utiliza «Nuevo autor».", Alert.AlertType.WARNING);
            return;
        }

        if (cmbEditorial.getValue() == null || cmbEditorial.getValue().trim().isEmpty()) {
            mostrarAlerta("Editorial requerida", "Selecciona una editorial o utiliza «Nueva editorial».", Alert.AlertType.WARNING);
            return;
        }

        try {
            double precio = Double.parseDouble(precioTexto);
            int categoria = Integer.parseInt(categoriaTexto.split(" - ", 2)[0].trim());
            int stockActual = Integer.parseInt(stockTexto);

            Usuario usuario = Main.getUsuarioSesion();
            boolean esAdmin = usuario != null && "admin".equalsIgnoreCase(usuario.getRol());
            int stockMinimo = esAdmin
                    ? Integer.parseInt(txtStockMinimo.getText().trim().isEmpty() ? "5" : txtStockMinimo.getText().trim())
                    : 5;

            if (precio < 0 || categoria <= 0 || stockActual < 0 || stockMinimo < 0) {
                mostrarAlerta("Datos inválidos", "Precio y cantidades no pueden ser negativos y la categoría debe ser válida.", Alert.AlertType.WARNING);
                return;
            }

            if (isbn.length() < 4) {
                mostrarAlerta("ISBN inválido", "Ingresa un ISBN válido.", Alert.AlertType.WARNING);
                return;
            }

            String nitEditorial = obtenerNitEditorialSeleccionada();
            int idAutor = obtenerIdAutorSeleccionado();

            if (nitEditorial == null || idAutor <= 0) {
                mostrarAlerta("Información incompleta", "El autor y la editorial seleccionados no son válidos.", Alert.AlertType.WARNING);
                return;
            }

            String sqlLibro = "INSERT INTO libros "
                    + "(isbn, titulo, fecha_publicacion, precio, categoria_id, nit_editorial, stock_actual, stock_minimo, estado) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            String sqlAutor = "INSERT INTO autores_libro(id_autor, isbn) VALUES (?, ?)";

            try (Connection conn = Conexion.getInstancia().conectar()) {
                conn.setAutoCommit(false);
                try (PreparedStatement psLibro = conn.prepareStatement(sqlLibro);
                     PreparedStatement psAutor = conn.prepareStatement(sqlAutor)) {

                    psLibro.setString(1, isbn);
                    psLibro.setString(2, titulo);
                    psLibro.setDate(3, dpFechaPublicacion.getValue() == null ? null : Date.valueOf(dpFechaPublicacion.getValue()));
                    psLibro.setDouble(4, precio);
                    psLibro.setInt(5, categoria);
                    psLibro.setString(6, nitEditorial);
                    psLibro.setInt(7, stockActual);
                    psLibro.setInt(8, stockMinimo);
                    psLibro.setBoolean(9, chkEstado.isSelected());
                    psLibro.executeUpdate();

                    psAutor.setInt(1, idAutor);
                    psAutor.setString(2, isbn);
                    psAutor.executeUpdate();

                    conn.commit();
                } catch (SQLException ex) {
                    conn.rollback();
                    if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("duplicate")) {
                        mostrarAlerta("ISBN duplicado", "Ya existe un libro registrado con ese ISBN.", Alert.AlertType.WARNING);
                    } else {
                        mostrarAlerta("Error", "No se pudo registrar el libro: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                    return;
                } finally {
                    conn.setAutoCommit(true);
                }
            }

            mostrarAlerta("Libro registrado", "El libro se agregó correctamente con su autor y editorial.", Alert.AlertType.INFORMATION);
            limpiarFormulario(null);

        } catch (NumberFormatException e) {
            mostrarAlerta("Datos inválidos", "Precio, categoría, stock actual y stock mínimo deben contener solo números válidos.", Alert.AlertType.WARNING);
        } catch (SQLException e) {
            mostrarAlerta("Error de conexión", "No se pudo conectar con la base de datos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private int obtenerIdAutorSeleccionado() {
        String seleccionado = cmbAutor.getValue();
        for (Map.Entry<Integer, String> entry : autores.entrySet()) {
            if (entry.getValue().equals(seleccionado)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    private String obtenerNitEditorialSeleccionada() {
        String seleccionado = cmbEditorial.getValue();
        if (seleccionado == null) return null;

        for (Map.Entry<String, String> entry : editoriales.entrySet()) {
            String opcion = entry.getValue() + " (" + entry.getKey() + ")";
            if (opcion.equals(seleccionado)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @FXML
    private void limpiarFormulario(ActionEvent event) {
        txtIsbn.clear();
        txtTitulo.clear();
        dpFechaPublicacion.setValue(null);
        txtPrecio.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        cmbAutor.getSelectionModel().clearSelection();
        cmbEditorial.getSelectionModel().clearSelection();
        txtStockActual.clear();
        txtStockMinimo.setText("5");
        chkEstado.setSelected(true);

        Usuario usuario = Main.getUsuarioSesion();
        boolean esAdmin = usuario != null && "admin".equalsIgnoreCase(usuario.getRol());
        txtStockMinimo.setDisable(!esAdmin);
        txtStockMinimo.setEditable(esAdmin);
    }

    @FXML
    private void regresar(ActionEvent event) {
        try {
            Main.regresarAnterior();
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo regresar al dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
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
