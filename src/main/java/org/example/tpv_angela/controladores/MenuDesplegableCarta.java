package org.example.tpv_angela.controladores;

import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.bson.Document;
import org.example.tpv_angela.modelos.Producto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Utilidad para construir acordeones de carta a partir de productos o documentos.
 */
public final class MenuDesplegableCarta {
    private static final String IMAGEN_PLATO_DEFAULT = "C:\\Users\\angel\\Downloads\\biblioteca\\Tpv_angela\\src\\main\\resources\\org\\example\\tpv_angela\\imagenes\\platoDefault.jpg";

    /**
     * Crea una instancia de MenuDesplegableCarta con los datos necesarios para su uso.
     */
    private MenuDesplegableCarta() {
    }

    /**
     * Genera la salida o estructura solicitada a partir de los datos recibidos.
     * @param acordeon acordeón que se rellena con los productos.
     * @param productos lista de productos que se muestran.
     * @param alPulsar acción ejecutada al seleccionar un producto.
     * @param recursos clase usada para resolver recursos de la aplicación.
     */
    public static void generarDesdeProductos(Accordion acordeon, List<Producto> productos, Consumer<Producto> alPulsar, Class<?> recursos) {
        acordeon.getPanes().clear();
        prepararAcordeon(acordeon);
        Map<String, List<Producto>> porCategoria = productos.stream()
                .sorted(Comparator.comparing(Producto::getNombre, Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.groupingBy(p -> valorOGeneral(p.getCategoria(), "OTROS")));

        porCategoria.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String::compareToIgnoreCase))
                .forEach(entry -> {
                    VBox contenedorCategoria = new VBox(10);
                    contenedorCategoria.setPadding(new Insets(10));
                    TitledPane pane = new TitledPane(entry.getKey().toUpperCase(), contenedorCategoria);
                    pane.getStyleClass().add("menu-titled-pane");

                    Map<String, List<Producto>> porSubcategoria = entry.getValue().stream()
                            .collect(Collectors.groupingBy(p -> valorOGeneral(p.getSubcategoria(), "GENERAL")));

                    porSubcategoria.entrySet().stream()
                            .sorted(Map.Entry.comparingByKey(String::compareToIgnoreCase))
                            .forEach(subEntry -> {
                                Label lblSub = crearEtiquetaSubcategoria(subEntry.getKey());
                                FlowPane flow = new FlowPane(8, 8);
                                subEntry.getValue().forEach(producto -> flow.getChildren().add(crearBotonProducto(
                                        producto.getNombre(),
                                        producto.getPrecio(),
                                        producto.getImagen(),
                                        producto.isDisponible(),
                                        true,
                                        140,
                                        126,
                                        12,
                                        76,
                                        56,
                                        () -> alPulsar.accept(producto),
                                        recursos
                                )));
                                contenedorCategoria.getChildren().addAll(lblSub, flow, new Separator());
                            });

                    acordeon.getPanes().add(pane);
                });
    }

    /**
     * Genera la salida o estructura solicitada a partir de los datos recibidos.
     * @param acordeon acordeón que se rellena con los productos.
     * @param documentos documento con los datos a procesar.
     * @param alPulsar acción ejecutada al seleccionar un producto.
     * @param recursos clase usada para resolver recursos de la aplicación.
     */
    public static void generarDesdeDocumentos(Accordion acordeon, List<Document> documentos, Consumer<Document> alPulsar, Class<?> recursos) {
        generarDesdeDocumentos(acordeon, documentos, alPulsar, recursos, false);
    }

    /**
     * Genera la salida o estructura solicitada a partir de los datos recibidos.
     * @param acordeon acordeón que se rellena con los productos.
     * @param documentos documento con los datos a procesar.
     * @param alPulsar acción ejecutada al seleccionar un producto.
     * @param recursos clase usada para resolver recursos de la aplicación.
     */
    public static void generarDesdeDocumentosGrande(Accordion acordeon, List<Document> documentos, Consumer<Document> alPulsar, Class<?> recursos) {
        generarDesdeDocumentos(acordeon, documentos, alPulsar, recursos, true);
    }

    /**
     * Genera la salida o estructura solicitada a partir de los datos recibidos.
     * @param acordeon acordeón que se rellena con los productos.
     * @param documentos documento con los datos a procesar.
     * @param alPulsar acción ejecutada al seleccionar un producto.
     * @param recursos clase usada para resolver recursos de la aplicación.
     * @param grande indica si se usa el diseño grande.
     */
    private static void generarDesdeDocumentos(Accordion acordeon, List<Document> documentos, Consumer<Document> alPulsar, Class<?> recursos, boolean grande) {
        acordeon.getPanes().clear();
        prepararAcordeon(acordeon);
        Map<String, List<Document>> porCategoria = documentos.stream()
                .sorted(Comparator.comparing(doc -> valorOGeneral(textoDocumento(doc, "nombre"), ""), String::compareToIgnoreCase))
                .collect(Collectors.groupingBy(doc -> valorOGeneral(textoDocumento(doc, "categoria"), "OTROS")));

        porCategoria.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String::compareToIgnoreCase))
                .forEach(entry -> {
                    VBox contenedorCategoria = new VBox(grande ? 14 : 10);
                    contenedorCategoria.setPadding(new Insets(grande ? 14 : 10));
                    TitledPane pane = new TitledPane(entry.getKey().toUpperCase(), contenedorCategoria);
                    pane.getStyleClass().add("menu-titled-pane");

                    Map<String, List<Document>> porSubcategoria = entry.getValue().stream()
                            .collect(Collectors.groupingBy(doc -> valorOGeneral(textoDocumento(doc, "subcategoria"), "GENERAL")));

                    porSubcategoria.entrySet().stream()
                            .sorted(Map.Entry.comparingByKey(String::compareToIgnoreCase))
                            .forEach(subEntry -> {
                                Label lblSub = crearEtiquetaSubcategoria(subEntry.getKey());
                                FlowPane flow = new FlowPane(grande ? 12 : 8, grande ? 12 : 8);
                                subEntry.getValue().forEach(documento -> flow.getChildren().add(crearBotonProducto(
                                        textoDocumento(documento, "nombre"),
                                        extraerPrecio(documento),
                                        textoDocumento(documento, "imagen"),
                                        documentoDisponible(documento),
                                        false,
                                        grande ? 190 : 140,
                                        grande ? 170 : 126,
                                        grande ? 15 : 12,
                                        grande ? 104 : 76,
                                        grande ? 78 : 56,
                                        () -> alPulsar.accept(documento),
                                        recursos
                                )));
                                contenedorCategoria.getChildren().addAll(lblSub, flow, new Separator());
                            });

                    acordeon.getPanes().add(pane);
                });
    }

    /**
     * Construye y devuelve el componente visual solicitado.
     * @param nombre nombre del producto o elemento.
     * @param precio precio del producto.
     * @param rutaImagen ruta de la imagen asociada.
     * @param disponible indica si el producto está disponible.
     * @param bloquearSiNoDisponible indica si el botón se bloquea cuando el producto no está disponible.
     * @param ancho ancho del control.
     * @param alto alto del control.
     * @param fuente fuente usada para dibujar el texto.
     * @param imagenAncho ancho de la imagen.
     * @param imagenAlto alto de la imagen.
     * @param accion acción que se ejecuta al interactuar con el control.
     * @param recursos clase usada para resolver recursos de la aplicación.
     * @return componente visual construido.
     */
    private static Button crearBotonProducto(String nombre, double precio, String rutaImagen, boolean disponible, boolean bloquearSiNoDisponible, double ancho, double alto, int fuente, double imagenAncho, double imagenAlto, Runnable accion, Class<?> recursos) {
        Button boton = new Button(valorOGeneral(nombre, "Producto") + "\n" + String.format("%.2f EUR", precio));
        boton.getStyleClass().addAll("num-button", fuente >= 14 ? "product-button-large" : "product-button-small");
        boton.setPrefSize(ancho, alto);
        boton.setWrapText(true);
        boton.setContentDisplay(ContentDisplay.TOP);
        boton.setOnAction(event -> {
            if (disponible || !bloquearSiNoDisponible) {
                accion.run();
            }
        });
        if (!disponible) {
            boton.setOpacity(0.5);
        }

        ImageView imagenProducto = crearImagenProducto(rutaImagen, recursos, imagenAncho, imagenAlto);
        if (imagenProducto != null) {
            boton.setGraphic(imagenProducto);
        }
        return boton;
    }

    /**
     * Aplica clases comunes a los acordeones generados.
     * @param acordeon acordeon de destino.
     */
    private static void prepararAcordeon(Accordion acordeon) {
        if (!acordeon.getStyleClass().contains("menu-accordion")) {
            acordeon.getStyleClass().add("menu-accordion");
        }
    }

    /**
     * Construye y devuelve el componente visual solicitado.
     * @param subcategoria subcategoría del producto.
     * @return componente visual construido.
     */
    private static Label crearEtiquetaSubcategoria(String subcategoria) {
        Label label = new Label(subcategoria.toUpperCase());
        label.getStyleClass().add("product-subcategory-label");
        return label;
    }

    /**
     * Construye y devuelve el componente visual solicitado.
     * @param rutaImagen ruta de la imagen asociada.
     * @param recursos clase usada para resolver recursos de la aplicación.
     * @param ancho ancho del control.
     * @param alto alto del control.
     * @return componente visual construido.
     */
    private static ImageView crearImagenProducto(String rutaImagen, Class<?> recursos, double ancho, double alto) {
        String ruta = rutaImagen == null || rutaImagen.isBlank() ? IMAGEN_PLATO_DEFAULT : rutaImagen;
        ImageView imagen = crearImagenDesdeRuta(ruta, recursos, ancho, alto);
        if (imagen != null) {
            return imagen;
        }
        return crearImagenDesdeRuta(IMAGEN_PLATO_DEFAULT, recursos, ancho, alto);
    }

    /**
     * Construye y devuelve el componente visual solicitado.
     * @param rutaImagen ruta de la imagen asociada.
     * @param recursos clase usada para resolver recursos de la aplicación.
     * @param ancho ancho del control.
     * @param alto alto del control.
     * @return componente visual construido.
     */
    private static ImageView crearImagenDesdeRuta(String rutaImagen, Class<?> recursos, double ancho, double alto) {
        try {
            Image imagen;
            String rutaLimpia = rutaImagen.trim();

            if (rutaLimpia.startsWith("data:image")) {
                String base64 = rutaLimpia.substring(rutaLimpia.indexOf(",") + 1);
                imagen = new Image(new ByteArrayInputStream(Base64.getDecoder().decode(base64)));
            } else if (rutaLimpia.length() > 100 && !rutaLimpia.contains("\\") && !rutaLimpia.contains("/") && !rutaLimpia.startsWith("http")) {
                imagen = new Image(new ByteArrayInputStream(Base64.getDecoder().decode(rutaLimpia)));
            } else if (rutaLimpia.startsWith("http://") || rutaLimpia.startsWith("https://")) {
                imagen = new Image(URI.create(rutaLimpia).toASCIIString(), ancho, alto, true, true, true);
            } else if (rutaLimpia.startsWith("file:")) {
                imagen = new Image(rutaLimpia, ancho, alto, true, true, true);
            } else {
                File archivo = new File(rutaLimpia);
                if (archivo.exists()) {
                    imagen = new Image(archivo.toURI().toString(), ancho, alto, true, true, true);
                } else {
                    String recurso = rutaLimpia.startsWith("/")
                            ? rutaLimpia
                            : "/org/example/tpv_angela/imagenes/" + rutaLimpia;
                    if (recursos.getResourceAsStream(recurso) == null) return null;
                    imagen = new Image(Objects.requireNonNull(recursos.getResourceAsStream(recurso)));
                }
            }

            ImageView imageView = new ImageView(imagen);
            imageView.setFitWidth(ancho);
            imageView.setFitHeight(alto);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            return imageView;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extrae el valor solicitado desde el documento recibido.
     * @param doc documento con los datos a procesar.
     * @return valor numérico calculado.
     */
    private static double extraerPrecio(Document doc) {
        Object precio = doc.get("precio");
        return precio instanceof Number ? ((Number) precio).doubleValue() : 0.0;
    }

    /**
     * Devuelve una representación de texto segura para el valor recibido.
     * @param doc documento con los datos a procesar.
     * @param campo campo del documento que se consulta.
     * @return texto formateado o normalizado.
     */
    private static String textoDocumento(Document doc, String campo) {
        Object valor = doc.get(campo);
        return valor == null ? null : String.valueOf(valor);
    }

    /**
     * Comprueba el estado del documento recibido.
     * @param doc documento con los datos a procesar.
     * @return true si la condición se cumple; false en caso contrario.
     */
    private static boolean documentoDisponible(Document doc) {
        Object deshabilitadoHasta = doc.get("deshabilitadoHasta");
        if (deshabilitadoHasta instanceof Date fecha && fecha.after(new Date())) {
            return false;
        }
        return doc.getBoolean("disponible", true);
    }

    /**
     * Ejecuta la operación asociada a este controlador o servicio.
     * @param valor valor que se procesa.
     * @param fallback valor usado cuando el texto está vacío.
     * @return texto formateado o normalizado.
     */
    private static String valorOGeneral(String valor, String fallback) {
        return valor == null || valor.isBlank() ? fallback : valor;
    }
}
