package org.example.tpv_angela.controladores.camarero;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import org.example.tpv_angela.ControladorAlertas;
import org.example.tpv_angela.DAO.DAOArqueoCaja;
import org.example.tpv_angela.Navegacion;
import org.example.tpv_angela.controladores.TecladoTactil;

import java.io.IOException;

/**
 * Controlador principal que coordina la navegación del área de camarero.
 */
public class ControladorGeneralCamarero {

    @FXML private AnchorPane contentArea;
    @FXML private Button btnInicio, btnVentas, btnMapa, btnMenu, btnArqueo;
    private final DAOArqueoCaja daoArqueo = new DAOArqueoCaja();

    /**
     * Carga la vista inicial y marca automáticamente el botón correspondiente
     * @param fxmlInterno ruta FXML interna que se debe cargar.
     */
    public void setVistaInicial(String fxmlInterno) {
        if (fxmlInterno.contains("VistaVentas") && ventasBloqueadas()) {
            fxmlInterno = "/org/example/tpv_angela/vistas/camarero/VistaCaja.fxml";
        }
        // 1. Cargamos el contenido en el centro
        cambiarContenidoCentral(fxmlInterno);

        if (fxmlInterno.contains("VistaVentas")) {
            marcarBoton(btnVentas);
        } else if (fxmlInterno.contains("VistaMapaMesas")) {
            marcarBoton(btnMapa);
        } else if (fxmlInterno.contains("VistaMenuCarta") || fxmlInterno.contains("VistaMenuPrincipal")) {
            marcarBoton(btnMenu);
        } else if (fxmlInterno.contains("VistaCaja")) {
            marcarBoton(btnArqueo);
        } else {
            marcarBoton(btnInicio);
        }
    }

    /**
     * Gestiona la acción de navegación asociada al botón pulsado.
     */
    @FXML
    private void clickVentas() {
        if (ventasBloqueadas()) {
            return;
        }
        cambiarContenidoCentral("/org/example/tpv_angela/vistas/camarero/VistaVentas.fxml");
        marcarBoton(btnVentas);
    }

    /**
     * Gestiona la acción de navegación asociada al botón pulsado.
     */
    @FXML
    private void clickMapa() {
        cambiarContenidoCentral("/org/example/tpv_angela/vistas/VistaMapaMesas.fxml");
        marcarBoton(btnMapa);
    }

    /**
     * Gestiona la acción de navegación asociada al botón pulsado.
     */
    @FXML
    private void clickMenu() {
        cambiarContenidoCentral("/org/example/tpv_angela/vistas/VistaMenuCarta.fxml");
        marcarBoton(btnMenu);
    }

    /**
     * Gestiona la acción de navegación asociada al botón pulsado.
     */
    @FXML
    private void clickArqueo() {
        cambiarContenidoCentral("/org/example/tpv_angela/vistas/camarero/VistaCaja.fxml");
        marcarBoton(btnArqueo);
    }

    /**
     * Gestiona la acción de navegación asociada al botón pulsado.
     * @param event evento que dispara la acción.
     */
    @FXML
    private void clickInicio(ActionEvent event) {
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/camarero/VistaMenuInicialCamarero.fxml", "Menú Inicial Camarero");
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
     * Ejecuta la operación asociada a este controlador o servicio.
     * @param bSeleccionado botón que debe quedar marcado como seleccionado.
     */
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

    /**
     * Ejecuta la operación asociada a este controlador o servicio.
     * @param fxml ruta FXML que se va a cargar.
     */
    private void cambiarContenidoCentral(String fxml) {
        try {
            if (fxml.endsWith("VistaMenuPrincipal.fxml")) {
                fxml = "/org/example/tpv_angela/vistas/VistaMenuCarta.fxml";
            }
            // Si la ruta no empieza por /, se la ponemos para que busque desde la raíz del classpath
            if (!fxml.startsWith("/")) {
                fxml = "/org/example/tpv_angela/vistas/camarero/" + fxml;
            }

            System.out.println("Intentando cargar: " + fxml);
            var resource = getClass().getResource(fxml);

            if (resource == null) {
                throw new RuntimeException("No se encontro el FXML en la ruta: " + fxml);
            }

            Parent view = FXMLLoader.load(resource);
            TecladoTactil.instalar(view);
            contentArea.getChildren().setAll(view);

            // Ajustar al tamaño del AnchorPane
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO: Fallo al cargar la subvista");
            e.printStackTrace();
        }
    }

    /**
     * Comprueba el cierre del dia antes de abrir pantallas de venta.
     */
    private boolean ventasBloqueadas() {
        if (!daoArqueo.cajaCerradaCamareroHoy()) {
            return false;
        }
        ControladorAlertas.mostrar("VENTA BLOQUEADA", "La caja del dia ya esta cerrada. No se pueden realizar mas cobros.");
        return true;
    }
}
