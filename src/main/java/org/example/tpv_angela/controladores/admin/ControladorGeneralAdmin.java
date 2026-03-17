package org.example.tpv_angela.controladores.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import org.example.tpv_angela.Navegacion;
import java.io.IOException;

public class ControladorGeneralAdmin {

    @FXML private AnchorPane contentArea;

    // Referencias a los botones para el marcado
    @FXML private Button btnInicio, btnMapa, btnMenu, btnArqueo, btnLista;

    /**
     * Carga la vista inicial y marca automáticamente el botón basándose en la ruta
     */
    public void setVistaInicial(String fxmlInterno) {
        // 1. Cargamos el contenido en el centro
        cambiarContenidoCentral(fxmlInterno);

        // 2. Marcamos el botón comparando la ruta del FXML recibido
        if (fxmlInterno.contains("VistaMapaMesasAdmin")) {
            marcarBoton(btnMapa);
        } else if (fxmlInterno.contains("VistaGestionCarta")) {
            marcarBoton(btnMenu);
        } else if (fxmlInterno.contains("VistaCajaAdmin")) {
            marcarBoton(btnArqueo);
        } else if (fxmlInterno.contains("VistaListaCompraAdmin")) {
            marcarBoton(btnLista);
        } else {
            marcarBoton(btnInicio);
        }
    }

    @FXML private void clickInicio(ActionEvent event) {
        // Volvemos al menú principal de botones grandes del Admin
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/admin/VistaMenuInicialAdmin.fxml", "Menú Inicial Administrador");
    }

    @FXML private void clickMapa() {
        cambiarContenidoCentral("/org/example/tpv_angela/vistas/admin/VistaMapaMesasAdmin.fxml");
        marcarBoton(btnMapa);
    }

    @FXML private void clickMenu() {
        cambiarContenidoCentral("/org/example/tpv_angela/vistas/admin/VistaGestionCarta.fxml");
        marcarBoton(btnMenu);
    }

    @FXML private void clickArqueo() {
        cambiarContenidoCentral("/org/example/tpv_angela/vistas/admin/VistaCajaAdmin.fxml");
        marcarBoton(btnArqueo);
    }

    @FXML private void clickLista() {
        cambiarContenidoCentral("/org/example/tpv_angela/vistas/admin/VistaListaCompraAdmin.fxml");
        marcarBoton(btnLista);
    }

    @FXML private void clickSalir(ActionEvent event) {
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaPantallaInicio.fxml", "Inicio");
    }

    private void marcarBoton(Button bSeleccionado) {
        Button[] botones = {btnInicio, btnMapa, btnMenu, btnArqueo, btnLista};
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
            System.err.println("Error cargando FXML: " + fxml);
            e.printStackTrace();
        }
    }
}