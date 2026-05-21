package org.example.tpv_angela.controladores.cocinero;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.bson.Document;
import org.example.tpv_angela.DAO.DAOMenu;
import org.example.tpv_angela.controladores.MenuDesplegableCarta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Controlador de cocina para gestionar comida, platos del día y disponibilidad.
 */
public class ControladorMenuComidaCocinero {
    @FXML private Accordion acordeonMenuFijo;
    @FXML private ListView<String> listaPlatosDia;
    @FXML private TextField txtNombre;
    @FXML private TextField txtSubcategoria;
    @FXML private TextField txtPrecio;
    @FXML private Button btnSeleccionarImagen;
    @FXML private Label lblEstado;

    private final DAOMenu daoMenu = new DAOMenu();
    private final List<Document> menuFijo = new ArrayList<>();
    private Document platoSeleccionado;
    private String imagenFormulario = "";
    private final Timeline autoRefresh = new Timeline(
            new KeyFrame(Duration.seconds(30), event -> refrescarSilencioso())
    );

    /**
     * Inicializa la vista y prepara los datos necesarios al cargar el FXML.
     */
    @FXML
    public void initialize() {
        refrescar();
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
    }

    /**
     * Gestiona la selección realizada por el usuario.
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
     * Método auxiliar usado por esta clase.
     */
    @FXML
    private void deshabilitar24h() {
        if (platoSeleccionado == null) {
            lblEstado.setText("Selecciona un plato del menú fijo.");
            return;
        }
        daoMenu.deshabilitarProducto24h(platoSeleccionado);
        refrescar();
        lblEstado.setText("Plato deshabilitado hasta las 00:00 del día siguiente.");
    }

    /**
     * Método auxiliar usado por esta clase.
     */
    @FXML
    private void rehabilitarPlato() {
        if (platoSeleccionado == null) {
            lblEstado.setText("Selecciona un plato del menú fijo.");
            return;
        }
        daoMenu.rehabilitarProducto(platoSeleccionado);
        refrescar();
        lblEstado.setText("Plato rehabilitado y disponible.");
    }

    /**
     * Agrega un nuevo elemento a la base de datos o a la vista.
     */
    @FXML
    private void agregarPlatoDia() {
        try {
            daoMenu.agregarPlatoDelDia(
                    txtNombre.getText().trim(),
                    "PLATO DEL DIA",
                    txtSubcategoria.getText().trim(),
                    Double.parseDouble(txtPrecio.getText().trim().replace(",", ".")),
                    obtenerImagenFormulario()
            );
            txtNombre.clear();
            txtSubcategoria.clear();
            txtPrecio.clear();
            imagenFormulario = "";
            refrescar();
            lblEstado.setText("Plato del día añadido hasta las 00:00 del día siguiente.");
        } catch (Exception e) {
            lblEstado.setText("Revisa los campos del plato del día.");
        }
    }

    /**
     * Recarga la información mostrada en pantalla.
     */
    @FXML
    private void refrescar() {
        cargarDatos();
        lblEstado.setText("Carta de cocina actualizada.");
    }

    /**
     * Recarga la información mostrada en pantalla.
     */
    private void refrescarSilencioso() {
        String mensajeActual = lblEstado.getText();
        cargarDatos();
        lblEstado.setText(mensajeActual == null || mensajeActual.isBlank() ? "Carta de cocina actualizada." : mensajeActual);
    }

    /**
     * Carga datos y actualiza la vista con la información disponible.
     */
    private void cargarDatos() {
        menuFijo.clear();
        menuFijo.addAll(daoMenu.obtenerMenuFijoDocumentos().stream()
                .filter(this::esComida)
                .toList());
        platoSeleccionado = null;
        MenuDesplegableCarta.generarDesdeDocumentos(acordeonMenuFijo, menuFijo, this::seleccionarPlato, getClass());
        listaPlatosDia.getItems().setAll(daoMenu.obtenerPlatosDelDiaDocumentos().stream().map(this::formatearPlatoDia).toList());
    }

    /**
     * Indica si se cumple la condición evaluada por el método.
     * @param plato documento del plato seleccionado.
     * @return resultado calculado por el método.
     */
    private boolean esComida(Document plato) {
        String texto = String.join(" ",
                valor(plato.getString("categoria")),
                valor(plato.getString("subcategoria")),
                valor(plato.getString("tipo")),
                valor(plato.getString("nombre"))
        ).toLowerCase(Locale.ROOT);

        return !contieneAlguno(texto,
                "bebida",
                "bebidas",
                "refresco",
                "refrescos",
                "cerveza",
                "cervezas",
                "vino",
                "vinos",
                "agua",
                "cafe",
                "café",
                "infusion",
                "infusión",
                "licor",
                "licores",
                "copa",
                "copas",
                "zumo",
                "zumos"
        );
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param texto texto que se procesa.
     * @param palabras palabras que se buscan en el texto.
     * @return resultado calculado por el método.
     */
    private boolean contieneAlguno(String texto, String... palabras) {
        for (String palabra : palabras) {
            if (texto.contains(palabra)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param texto texto que se procesa.
     * @return resultado calculado por el método.
     */
    private String valor(String texto) {
        return texto == null ? "" : texto;
    }

    /**
     * Gestiona la selección realizada por el usuario.
     * @param plato documento del plato seleccionado.
     */
    private void seleccionarPlato(Document plato) {
        platoSeleccionado = plato;
        lblEstado.setText("Seleccionado: " + formatearMenuFijo(plato));
    }

    /**
     * Formatea datos para mostrarlos en pantalla o en documentos.
     * @param doc documento de MongoDB que se procesa.
     * @return resultado calculado por el método.
     */
    private String formatearMenuFijo(Document doc) {
        String estado = "Disponible";
        Object hasta = doc.get("deshabilitadoHasta");
        if (hasta instanceof Date fecha && fecha.after(new Date())) {
            String textoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-ES")).format(fecha);
            estado = "Deshabilitado hasta " + textoFecha;
        } else if (!doc.getBoolean("disponible", true)) {
            estado = "No disponible";
        }
        return doc.getString("nombre") + " | " + doc.getString("categoria") + " | " + estado;
    }

    /**
     * Formatea datos para mostrarlos en pantalla o en documentos.
     * @param doc documento de MongoDB que se procesa.
     * @return resultado calculado por el método.
     */
    private String formatearPlatoDia(Document doc) {
        Object hasta = doc.get("disponibleHasta");
        if (hasta instanceof Date fecha) {
            String textoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-ES")).format(fecha);
            return doc.getString("nombre") + " | " + doc.getString("categoria") + " | Activo hasta " + textoFecha;
        }
        return doc.getString("nombre") + " | " + doc.getString("categoria") + " | Activo hasta 00:00";
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param origen archivo de imagen original.
     * @return resultado calculado por el método.
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
     * Devuelve la ruta real de la imagen aunque el campo muestre solo el nombre.
     * @return ruta real o texto introducido manualmente.
     */
    private String obtenerImagenFormulario() {
        return imagenFormulario == null ? "" : imagenFormulario;
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param nombreOriginal nombre original del archivo.
     * @return resultado calculado por el método.
     */
    private String nombreArchivoSeguro(String nombreOriginal) {
        String normalizado = Normalizer.normalize(nombreOriginal, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        return normalizado.isBlank() ? "imagen_plato.png" : normalizado;
    }
}
