package org.example.tpv_angela.controladores.cocinero;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import org.example.tpv_angela.Navegacion;

/**
 * Controlador del menú inicial del cocinero y sus accesos principales.
 */
public class ControladorMenuCocinero {

    /**
     * Cierra la vista o finaliza el flujo activo.
     * @param event evento que dispara la acción.
     */
    public void cerrarSesion(ActionEvent event) {
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaPantallaInicio.fxml", "Pantalla Inicial");
    }

    /**
     * Ejecuta la operación asociada a este controlador o servicio.
     * @param event evento que dispara la acción.
     */
    public void menu(ActionEvent event) {
        navegarAContenedor(event, "/org/example/tpv_angela/vistas/cocinero/VistaMenuComidaCocinero.fxml");
    }

    /**
     * Ejecuta la operación asociada a este controlador o servicio.
     * @param event evento que dispara la acción.
     */
    public void listaCompra(ActionEvent event) {
        navegarAContenedor(event, "/org/example/tpv_angela/vistas/cocinero/VistaListaCompra.fxml");
    }

    /**
     * Navega a la vista indicada desde el evento recibido.
     * @param event evento que dispara la acción.
     * @param rutaInterna ruta FXML que se carga en el contenedor.
     */
    private void navegarAContenedor(ActionEvent event, String rutaInterna) {
        FXMLLoader loader = Navegacion.cambiarVista(
                event,
                "/org/example/tpv_angela/vistas/cocinero/VistaGeneralCocinero.fxml",
                "TPV - Cocina"
        );

        if (loader != null) {
            ControladorGeneralCocinero controladorGeneral = loader.getController();
            controladorGeneral.setVistaInicial(rutaInterna);
        }
    }
}