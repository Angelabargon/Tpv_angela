package org.example.tpv_angela.controladores.camarero;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import org.example.tpv_angela.Navegacion;

import java.io.IOException;

public class ControladorGeneralCamarero {

    @FXML private AnchorPane contentArea;
    @FXML private Button btnInicio, btnVentas, btnMapa, btnMenu, btnArqueo;

    /**
     * Carga la vista inicial y marca automáticamente el botón correspondiente
     */
    public void setVistaInicial(String fxmlInterno) {
        // 1. Cargamos el contenido en el centro
        cambiarContenidoCentral(fxmlInterno);

        // 2. Marcamos el botón comparando la ruta del FXML recibido
        if (fxmlInterno.contains("VistaVentas")) {
            marcarBoton(btnVentas);
        } else if (fxmlInterno.contains("VistaMapaMesas")) {
            marcarBoton(btnMapa);
        } else if (fxmlInterno.contains("VistaMenuCarta")) {
            marcarBoton(btnMenu);
        } else if (fxmlInterno.contains("VistaCaja")) {
            marcarBoton(btnArqueo);
        } else {
            marcarBoton(btnInicio);
        }
    }

    @FXML
    private void clickVentas() {
        cambiarContenidoCentral("/org/example/tpv_angela/vistas/VistaVentas.fxml");
        marcarBoton(btnVentas);
    }

    @FXML
    private void clickMapa() {
        cambiarContenidoCentral("/org/example/tpv_angela/vistas/VistaMapaMesas.fxml");
        marcarBoton(btnMapa);
    }

    @FXML
    private void clickMenu() {
        cambiarContenidoCentral("/org/example/tpv_angela/vistas/VistaMenuCarta.fxml");
        marcarBoton(btnMenu);
    }

    @FXML
    private void clickArqueo() {
        cambiarContenidoCentral("/org/example/tpv_angela/vistas/VistaCaja.fxml");
        marcarBoton(btnArqueo);
    }

    @FXML
    private void clickInicio(ActionEvent event) {
        // Al volver al menú de botones grandes, cambiamos la escena completa
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/camarero/VistaMenuInicialCamarero.fxml", "Menu Inicial Camarero");
    }

    @FXML
    private void clickSalir(ActionEvent event) {
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaPantallaInicio.fxml", "Inicio");
    }

    private void marcarBoton(Button bSeleccionado) {
        Button[] botones = {btnInicio, btnVentas, btnMapa, btnMenu, btnArqueo};
        for (Button b : botones) {
            if (b != null) {
                b.getStyleClass().remove("boton-activo");
            }
        }
        if (bSeleccionado != null) {
            bSeleccionado.getStyleClass().add("boton-activo");
        }
    }

    private void cambiarContenidoCentral(String fxml) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxml));
            contentArea.getChildren().setAll(view);

            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
        } catch (IOException e) {
            System.err.println("Error al cargar el FXML: " + fxml);
            e.printStackTrace();
        }
    }
}