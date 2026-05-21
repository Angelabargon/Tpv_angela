package org.example.tpv_angela.controladores.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.bson.Document;
import org.example.tpv_angela.DAO.DAOMenu;
import org.example.tpv_angela.controladores.MenuDesplegableCarta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de la pantalla de administración del menú fijo y sus productos.
 */
public class ControladorGestionCarta {
    @FXML private Accordion acordeonProductos;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCategoria;
    @FXML private TextField txtSubcategoria;
    @FXML private TextField txtPrecio;
    @FXML private Label lblTituloFormulario;
    @FXML private Button btnGuardarProducto;
    @FXML private Button btnCancelarEdicion;
    @FXML private Button btnSeleccionarImagen;
    @FXML private Label lblEstado;

    private final DAOMenu daoMenu = new DAOMenu();
    private final List<Document> productos = new ArrayList<>();
    private Document productoSeleccionado;
    private String imagenFormulario = "";

    /**
     * Inicializa la vista, configura sus controles y carga los datos necesarios.
     */
    @FXML
    public void initialize() {
        refrescar();
        limpiarFormulario();
    }

    /**
     * Gestiona la selección de la imagen local del plato.
     */
    @FXML
    private void seleccionarImagenLocal() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Seleccionar imagen del plato");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Imagenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"
        ));

        File archivo = selector.showOpenDialog(btnSeleccionarImagen.getScene().getWindow());
        if (archivo == null) {
            return;
        }

        try {
            String rutaCopiada = copiarImagenLocal(archivo);
            imagenFormulario = rutaCopiada;
            lblEstado.setText("Imagen seleccionada: " + archivo.getName());
        } catch (IOException e) {
            lblEstado.setText("No se pudo copiar la imagen seleccionada.");
        }
    }

    /**
     * Agrega un plato nuevo o guarda los cambios del plato seleccionado.
     */
    @FXML
    private void agregarProducto() {
        try {
            String nombre = txtNombre.getText().trim();
            String categoria = txtCategoria.getText().trim();
            String subcategoria = txtSubcategoria.getText().trim();
            double precio = Double.parseDouble(txtPrecio.getText().trim().replace(",", "."));
            String imagen = obtenerImagenFormulario();

            if (productoSeleccionado == null) {
                daoMenu.agregarProductoMenuFijo(nombre, categoria, subcategoria, precio, imagen);
                lblEstado.setText("Plato añadido al menú fijo.");
            } else {
                daoMenu.actualizarProductoMenuFijo(productoSeleccionado, nombre, categoria, subcategoria, precio, imagen);
                lblEstado.setText("Plato modificado correctamente.");
            }
            refrescar();
            limpiarFormulario();
        } catch (Exception e) {
            lblEstado.setText("Revisa nombre, precio y campos obligatorios.");
        }
    }

    /**
     * Carga el plato seleccionado en el formulario para modificarlo.
     */
    @FXML
    private void modificarSeleccionado() {
        if (productoSeleccionado == null) {
            lblEstado.setText("Selecciona un plato para modificar.");
            return;
        }

        txtNombre.setText(texto(productoSeleccionado.getString("nombre")));
        txtCategoria.setText(texto(productoSeleccionado.getString("categoria")));
        txtSubcategoria.setText(texto(productoSeleccionado.getString("subcategoria")));
        txtPrecio.setText(String.valueOf(productoSeleccionado.get("precio", "")));
        imagenFormulario = texto(productoSeleccionado.getString("imagen"));
        lblTituloFormulario.setText("Modificar plato");
        btnGuardarProducto.setText("Guardar cambios");
        btnCancelarEdicion.setVisible(true);
        btnCancelarEdicion.setManaged(true);
        lblEstado.setText("Editando: " + formatearProducto(productoSeleccionado));
    }

    /**
     * Cancela la edición activa y limpia el formulario.
     */
    @FXML
    private void cancelarEdicion() {
        productoSeleccionado = null;
        limpiarFormulario();
        lblEstado.setText("Edición cancelada.");
    }

    /**
     * Elimina el elemento seleccionado tras aplicar las validaciones necesarias.
     */
    @FXML
    private void eliminarSeleccionado() {
        if (productoSeleccionado == null) {
            lblEstado.setText("Selecciona un plato para eliminar.");
            return;
        }
        daoMenu.eliminarProductoMenuFijo(productoSeleccionado);
        refrescar();
        lblEstado.setText("Plato eliminado del menú fijo.");
    }

    /**
     * Actualiza la información mostrada consultando los datos más recientes.
     */
    @FXML
    private void refrescar() {
        try {
            productos.clear();
            productos.addAll(daoMenu.obtenerMenuFijoDocumentos());
            productoSeleccionado = null;
            MenuDesplegableCarta.generarDesdeDocumentos(acordeonProductos, productos, this::seleccionarProducto, getClass());
            lblEstado.setText("Productos cargados: " + productos.size());
        } catch (Exception e) {
            lblEstado.setText("No se pudo cargar el menú del administrador.");
            e.printStackTrace();
        }
    }

    /**
     * Gestiona la selección del elemento indicado y actualiza la vista.
     * @param producto producto o documento seleccionado.
     */
    private void seleccionarProducto(Document producto) {
        productoSeleccionado = producto;
        lblEstado.setText("Seleccionado: " + formatearProducto(producto));
    }

    /**
     * Convierte el valor recibido en un texto preparado para mostrarse en pantalla.
     * @param doc documento con los datos a procesar.
     * @return texto formateado o normalizado.
     */
    private String formatearProducto(Document doc) {
        String nombre = doc.getString("nombre");
        String categoria = doc.getString("categoria");
        String subcategoria = doc.getString("subcategoria");
        Object precio = doc.get("precio");
        boolean disponible = doc.getBoolean("disponible", true);
        return nombre + " | " + categoria + " / " + subcategoria + " | " + precio + " EUR | " + (disponible ? "Activo" : "No disponible");
    }

    /**
     * Limpia los campos del formulario para dejar la vista preparada.
     */
    private void limpiarFormulario() {
        txtNombre.clear();
        txtCategoria.clear();
        txtSubcategoria.clear();
        txtPrecio.clear();
        imagenFormulario = "";
        productoSeleccionado = null;
        lblTituloFormulario.setText("Añadir plato");
        btnGuardarProducto.setText("Añadir al menú fijo");
        btnCancelarEdicion.setVisible(false);
        btnCancelarEdicion.setManaged(false);
    }

    /**
     * Devuelve un texto seguro para cargarlo en el formulario.
     * @param valor valor recibido desde MongoDB.
     * @return texto preparado para el campo.
     */
    private String texto(String valor) {
        return valor == null ? "" : valor;
    }

    /**
     * Devuelve la ruta real de la imagen aunque el campo muestre solo el nombre.
     * @return ruta real o texto introducido manualmente.
     */
    private String obtenerImagenFormulario() {
        return imagenFormulario == null ? "" : imagenFormulario;
    }

    /**
     * Copia el archivo recibido al destino usado por la aplicación.
     * @param origen archivo de origen que se copia.
     * @return texto formateado o normalizado.
     * @throws IOException si ocurre un error durante la operación.
     */
    private String copiarImagenLocal(File origen) throws IOException {
        Path carpetaDestino = Path.of(System.getProperty("user.dir"), "imagenes_platos");
        Files.createDirectories(carpetaDestino);

        String nombreSeguro = nombreArchivoSeguro(origen.getName());
        Path destino = carpetaDestino.resolve(System.currentTimeMillis() + "_" + nombreSeguro);
        Files.copy(origen.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        return destino.toString();
    }

    /**
     * Genera un nombre seguro para usarlo como archivo local.
     * @param nombreOriginal nombre original del archivo.
     * @return texto formateado o normalizado.
     */
    private String nombreArchivoSeguro(String nombreOriginal) {
        String normalizado = Normalizer.normalize(nombreOriginal, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        return normalizado.isBlank() ? "imagen_plato.png" : normalizado;
    }
}
