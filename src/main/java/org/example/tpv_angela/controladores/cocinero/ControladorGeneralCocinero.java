package org.example.tpv_angela.controladores.cocinero;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import org.example.tpv_angela.Navegacion;
import org.example.tpv_angela.controladores.TecladoTactil;
import java.io.IOException;

/**
 * Controlador principal que coordina la navegación del área de cocina.
 */
public class ControladorGeneralCocinero {

    @FXML private AnchorPane contentArea;
    @FXML private Button btnInicio, btnMenu, btnListaCompra;

    /**
     * Carga la vista inicial y marca el botón correspondiente comparando la ruta
     * @param fxmlInterno ruta FXML interna que se debe cargar.
     */
    public void setVistaInicial(String fxmlInterno) {
        // 1. Cargamos el contenido en el centro
        cambiarContenidoCentral(fxmlInterno);

        // 2. Marcamos el botón comparando la ruta del FXML recibido
        if (fxmlInterno.contains("VistaMenuComidaCocinero")) {
            marcarBoton(btnMenu);
        } else if (fxmlInterno.contains("VistaListaCompra")) {
            marcarBoton(btnListaCompra);
        } else {
            marcarBoton(btnInicio);
        }
    }

    /**
     * Gestiona la acción de navegación asociada al botón pulsado.
     * @param event evento que dispara la acción.
     */
    @FXML
    private void clickSalir(ActionEvent event) {
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaPantallaInicio.fxml", "Inicio");
    }

    /**
     * Gestiona la acción de navegación asociada al botón pulsado.
     */
    @FXML
    private void clickMenuComida() {
        cambiarContenidoCentral("/org/example/tpv_angela/vistas/cocinero/VistaMenuComidaCocinero.fxml");
        marcarBoton(btnMenu);
    }

    /**
     * Gestiona la acción de navegación asociada al botón pulsado.
     */
    @FXML
    private void clickListaCompra() {
        cambiarContenidoCentral("/org/example/tpv_angela/vistas/cocinero/VistaListaCompra.fxml");
        marcarBoton(btnListaCompra);
    }

    /**
     * Gestiona la acción de navegación asociada al botón pulsado.
     * @param event evento que dispara la acción.
     */
    @FXML
    private void clickInicio(ActionEvent event) {
        // Al volver al menú de botones grandes, cambiamos la escena completa
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/cocinero/VistaMenuInicialCocinero.fxml", "Menú Inicial Cocinero");
    }

    /**
     * Ejecuta la operación asociada a este controlador o servicio.
     * @param bSeleccionado botón que debe quedar marcado como seleccionado.
     */
    private void marcarBoton(Button bSeleccionado) {
        Button[] botones = {btnInicio, btnMenu, btnListaCompra};
        for (Button b : botones) {
            if (b != null) {
                b.getStyleClass().remove("boton-activo");
            }
        }
        if (bSeleccionado != null) {
            bSeleccionado.getStyleClass().add("boton-activo");
        }
    }

    /**
     * Ejecuta la operación asociada a este controlador o servicio.
     * @param fxml ruta FXML que se va a cargar.
     */
    private void cambiarContenidoCentral(String fxml) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxml));
            TecladoTactil.instalar(view);
            contentArea.getChildren().setAll(view);

            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
        } catch (IOException e) {
            System.err.println("Error cargando: " + fxml);
            e.printStackTrace();
        }
    }
}
