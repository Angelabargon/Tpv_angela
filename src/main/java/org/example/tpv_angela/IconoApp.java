package org.example.tpv_angela;

import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Utilidad para aplicar el icono de la aplicación a ventanas emergentes.
 */
public final class IconoApp {
    private static final String RUTA_ICONO = "/org/example/tpv_angela/imagenes/logo.png";

    private IconoApp() {
    }

    /**
     * Aplica el icono principal a una ventana.
     * @param stage ventana a configurar.
     */
    public static void aplicar(Stage stage) {
        if (stage == null || IconoApp.class.getResourceAsStream(RUTA_ICONO) == null) {
            return;
        }
        stage.getIcons().setAll(new Image(Objects.requireNonNull(IconoApp.class.getResourceAsStream(RUTA_ICONO))));
    }

    /**
     * Aplica el icono principal cuando se muestra un dialogo nativo.
     * @param dialogo dialogo a configurar.
     */
    public static void aplicar(Dialog<?> dialogo) {
        if (dialogo == null) {
            return;
        }
        dialogo.setOnShown(event -> {
            if (dialogo.getDialogPane().getScene() != null
                    && dialogo.getDialogPane().getScene().getWindow() instanceof Stage stage) {
                aplicar(stage);
            }
        });
    }
}
