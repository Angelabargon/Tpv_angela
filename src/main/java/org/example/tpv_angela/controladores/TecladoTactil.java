package org.example.tpv_angela.controladores;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Componente reutilizable que muestra un teclado tactil sobre los campos de texto.
 */
public final class TecladoTactil {
    private static final Object INSTALADO = new Object();
    private static final Object NUMERICO = new Object();
    private static final Object CIERRE_FUERA_INSTALADO = new Object();
    private static final Popup popup = new Popup();
    private static final VBox teclado = new VBox(8);
    private static TextInputControl campoActivo;
    private static boolean mayusculas = true;
    private static boolean modoNumerico = false;
    private static boolean movidoManual = false;
    private static double desfaseX;
    private static double desfaseY;

    static {
        popup.setAutoHide(false);
        teclado.setAlignment(Pos.CENTER);
        teclado.setPadding(new Insets(14));
        teclado.getStyleClass().add("teclado-tactil");
        teclado.getStylesheets().add(TecladoTactil.class.getResource("/org/example/tpv_angela/estilo/style.css").toExternalForm());
        reconstruirTeclado();
        popup.getContent().add(teclado);
        popup.setOnHidden(event -> movidoManual = false);
    }

    /**
     * Método auxiliar usado por esta clase.
     */
    private TecladoTactil() {
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param root nodo raíz donde se instala el teclado.
     */
    public static void instalar(Parent root) {
        instalarCierreAlTocarFuera(root);
        instalarEnNodo(root);
    }

    /**
     * Instala un teclado tactil numerico en un campo concreto.
     * @param campo campo donde se escribe el importe o numero.
     */
    public static void instalarNumerico(TextInputControl campo) {
        if (campo == null) {
            return;
        }
        campo.getProperties().put(NUMERICO, true);
        if (campo.getProperties().containsKey(INSTALADO)) {
            return;
        }
        instalarEnNodo(campo);
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param root nodo raíz donde se instala el teclado.
     */
    private static void instalarCierreAlTocarFuera(Parent root) {
        if (root == null || root.getProperties().containsKey(CIERRE_FUERA_INSTALADO)) {
            return;
        }
        root.getProperties().put(CIERRE_FUERA_INSTALADO, true);
        root.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getTarget() instanceof Node nodo && !estaDentroDeCampoTexto(nodo)) {
                popup.hide();
                campoActivo = null;
            }
        });
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param nodo nodo de la interfaz que se evalúa.
     */
    private static void instalarEnNodo(Node nodo) {
        if (nodo == null || nodo.getProperties().containsKey(INSTALADO)) {
            return;
        }
        nodo.getProperties().put(INSTALADO, true);

        if (nodo instanceof TextInputControl campo) {
            campo.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> mostrar(campo));
            campo.focusedProperty().addListener((obs, antes, ahora) -> {
                if (ahora) {
                    mostrar(campo);
                }
            });
        }

        if (nodo instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(TecladoTactil::instalarEnNodo);
        }
    }

    /**
     * Indica si se cumple la condición evaluada por el método.
     * @param nodo nodo de la interfaz que se evalúa.
     * @return resultado calculado por el método.
     */
    private static boolean estaDentroDeCampoTexto(Node nodo) {
        Node actual = nodo;
        while (actual != null) {
            if (actual instanceof TextInputControl) {
                return true;
            }
            actual = actual.getParent();
        }
        return false;
    }

    /**
     * Muestra información o un aviso al usuario.
     * @param campo campo del documento que se consulta.
     */
    private static void mostrar(TextInputControl campo) {
        campoActivo = campo;
        boolean numerico = esCampoNumerico(campo);
        if (modoNumerico != numerico) {
            modoNumerico = numerico;
            reconstruirTeclado();
        }
        Scene scene = campo.getScene();
        if (scene == null) {
            return;
        }
        Window window = scene.getWindow();
        if (window == null) {
            return;
        }

        if (!popup.isShowing()) {
            popup.show(window);
        }

        double ancho = modoNumerico ? 360 : Math.min(980, Math.max(760, window.getWidth() * 0.62));
        teclado.setPrefWidth(ancho);
        if (!movidoManual) {
            popup.setX(window.getX() + (window.getWidth() - ancho) / 2);
            popup.setY(window.getY() + window.getHeight() - (modoNumerico ? 315 : 335));
        }
    }

    /**
     * Detecta campos que solo deben recibir numeros o importes.
     * @param campo campo de texto que se evalua.
     * @return true si debe abrirse el teclado numerico.
     */
    private static boolean esCampoNumerico(TextInputControl campo) {
        if (campo.getProperties().containsKey(NUMERICO)) {
            return true;
        }

        String descripcion = normalizar(
                valor(campo.getId()) + " " +
                        valor(campo.getPromptText()) + " " +
                        valor(campo.getAccessibleText())
        );

        return descripcion.contains("precio")
                || descripcion.contains("numero")
                || descripcion.contains("mesa")
                || descripcion.contains("capacidad")
                || descripcion.contains("cantidad")
                || descripcion.contains("importe")
                || descripcion.contains("efectivo")
                || descripcion.contains("tarjeta")
                || descripcion.contains("total")
                || descripcion.contains("contado")
                || descripcion.contains("datafono")
                || descripcion.contains("recibido");
    }

    /**
     * Normaliza texto para comparar sin mayusculas ni acentos.
     * @param texto texto original.
     * @return texto normalizado.
     */
    private static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    /**
     * Devuelve un texto seguro para concatenar descriptores del campo.
     * @param texto texto que se procesa.
     * @return texto no nulo.
     */
    private static String valor(String texto) {
        return texto == null ? "" : texto;
    }

    /**
     * Método auxiliar usado por esta clase.
     */
    private static void reconstruirTeclado() {
        if (modoNumerico) {
            teclado.getChildren().setAll(
                    crearCabecera(),
                    crearFilaNumerica("789"),
                    crearFilaNumerica("456"),
                    crearFilaNumerica("123"),
                    crearFilaNumerica("0,."),
                    crearFilaAccionesNumericas()
            );
            return;
        }

        teclado.getChildren().setAll(
                crearCabecera(),
                crearFila("1234567890"),
                crearFila(mayusculas ? "QWERTYUIOP" : "qwertyuiop"),
                crearFila(mayusculas ? "ASDFGHJKL\u00D1" : "asdfghjkl\u00F1"),
                crearFila(mayusculas ? "ZXCVBNM" : "zxcvbnm"),
                crearFilaAcciones()
        );
    }

    /**
     * Crea la cabecera arrastrable del teclado tactil.
     * @return cabecera del teclado.
     */
    private static HBox crearCabecera() {
        HBox cabecera = new HBox();
        cabecera.setAlignment(Pos.CENTER);
        cabecera.setPadding(new Insets(0, 8, 6, 8));
        cabecera.getStyleClass().add("teclado-cabecera");
        Label titulo = new Label("TECLADO");
        titulo.getStyleClass().add("teclado-titulo");
        cabecera.getChildren().add(titulo);

        cabecera.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            desfaseX = event.getScreenX() - popup.getX();
            desfaseY = event.getScreenY() - popup.getY();
            movidoManual = true;
            event.consume();
        });

        cabecera.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            popup.setX(event.getScreenX() - desfaseX);
            popup.setY(event.getScreenY() - desfaseY);
            event.consume();
        });

        return cabecera;
    }

    /**
     * Crea un componente o estructura auxiliar usada por la aplicación.
     * @param letras conjunto de letras que se añaden al teclado.
     * @return resultado calculado por el método.
     */
    private static HBox crearFila(String letras) {
        HBox fila = new HBox(8);
        fila.setAlignment(Pos.CENTER);
        for (int i = 0; i < letras.length(); i++) {
            String texto = letras.substring(i, i + 1);
            fila.getChildren().add(crearTecla(texto, () -> insertar(texto), 62));
        }
        return fila;
    }

    /**
     * Crea una fila del teclado numerico.
     * @param numeros numeros o separadores decimales de la fila.
     * @return fila construida.
     */
    private static HBox crearFilaNumerica(String numeros) {
        HBox fila = new HBox(8);
        fila.setAlignment(Pos.CENTER);
        for (int i = 0; i < numeros.length(); i++) {
            String texto = numeros.substring(i, i + 1);
            fila.getChildren().add(crearTecla(texto, () -> insertar(texto), 82));
        }
        return fila;
    }

    /**
     * Crea un componente o estructura auxiliar usada por la aplicación.
     * @return resultado calculado por el método.
     */
    private static HBox crearFilaAcciones() {
        HBox fila = new HBox(8);
        fila.setAlignment(Pos.CENTER);
        fila.getChildren().add(crearTecla(mayusculas ? "abc" : "ABC", () -> {
            mayusculas = !mayusculas;
            reconstruirTeclado();
        }, 92));
        fila.getChildren().add(crearTecla("Borrar", TecladoTactil::borrar, 116));
        fila.getChildren().add(crearTecla("Espacio", () -> insertar(" "), 230));
        fila.getChildren().add(crearTecla(".", () -> insertar("."), 62));
        fila.getChildren().add(crearTecla(",", () -> insertar(","), 62));
        fila.getChildren().add(crearTecla("Limpiar", () -> {
            if (campoActivo != null) {
                campoActivo.clear();
            }
        }, 116));
        fila.getChildren().add(crearTecla("Cerrar", popup::hide, 116));
        return fila;
    }

    /**
     * Crea las acciones del teclado numerico.
     * @return fila de acciones.
     */
    private static HBox crearFilaAccionesNumericas() {
        HBox fila = new HBox(8);
        fila.setAlignment(Pos.CENTER);
        fila.getChildren().add(crearTecla("Borrar", TecladoTactil::borrar, 116));
        fila.getChildren().add(crearTecla("Limpiar", () -> {
            if (campoActivo != null) {
                campoActivo.clear();
            }
        }, 116));
        fila.getChildren().add(crearTecla("Cerrar", popup::hide, 116));
        return fila;
    }

    /**
     * Crea un componente o estructura auxiliar usada por la aplicación.
     * @param texto texto que se procesa.
     * @param accion acción que se ejecuta al pulsar el botón.
     * @param ancho ancho del control.
     * @return resultado calculado por el método.
     */
    private static Button crearTecla(String texto, Runnable accion, double ancho) {
        Button boton = new Button(texto);
        boton.setMinSize(ancho, 54);
        boton.setPrefSize(ancho, 54);
        boton.getStyleClass().add("teclado-tecla");
        boton.setOnAction(event -> accion.run());
        return boton;
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param texto texto que se procesa.
     */
    private static void insertar(String texto) {
        if (campoActivo == null) {
            return;
        }
        campoActivo.replaceSelection(texto);
        campoActivo.requestFocus();
    }

    /**
     * Método auxiliar usado por esta clase.
     */
    private static void borrar() {
        if (campoActivo == null) {
            return;
        }
        int inicio = campoActivo.getSelection().getStart();
        int fin = campoActivo.getSelection().getEnd();
        if (inicio != fin) {
            campoActivo.replaceSelection("");
        } else if (campoActivo.getCaretPosition() > 0) {
            int posicion = campoActivo.getCaretPosition();
            campoActivo.deleteText(posicion - 1, posicion);
        }
        campoActivo.requestFocus();
    }
}
