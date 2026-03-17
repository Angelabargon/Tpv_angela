package org.example.tpv_angela.controladores.cocinero;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import org.example.tpv_angela.Navegacion;

public class ControladorMenuCocinero {

    public void cerrarSesion(ActionEvent event) {
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaPantallaInicio.fxml", "Pantalla Inicial");
    }

    public void menu(ActionEvent event) {
        navegarAContenedor(event, "/org/example/tpv_angela/vistas/cocinero/VistaMenuComidaCocinero.fxml");
    }

    public void listaCompra(ActionEvent event) {
        navegarAContenedor(event, "/org/example/tpv_angela/vistas/cocinero/VistaListaCompra.fxml");
    }

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