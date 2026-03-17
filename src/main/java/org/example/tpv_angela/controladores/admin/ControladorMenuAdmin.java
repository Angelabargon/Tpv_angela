package org.example.tpv_angela.controladores.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import org.example.tpv_angela.Navegacion;

public class ControladorMenuAdmin {

    public void cerrarSesion(ActionEvent event) {
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaPantallaInicio.fxml", "Pantalla Inicial");
    }

    public void mapaMesas(ActionEvent event) {
        navegarAContenedor(event, "/org/example/tpv_angela/vistas/admin/VistaMapaMesasAdmin.fxml");
    }

    public void menuPrincipal(ActionEvent event) {
        navegarAContenedor(event, "/org/example/tpv_angela/vistas/admin/VistaGestionCarta.fxml");
    }

    public void cierreCaja(ActionEvent event) {
        navegarAContenedor(event, "/org/example/tpv_angela/vistas/admin/VistaCajaAdmin.fxml");
    }

    public void compra(ActionEvent event) {
        navegarAContenedor(event, "/org/example/tpv_angela/vistas/admin/VistaListaCompraAdmin.fxml");
    }

    private void navegarAContenedor(ActionEvent event, String rutaInterna) {
        FXMLLoader loader = Navegacion.cambiarVista(
                event,
                "/org/example/tpv_angela/vistas/admin/VistaGeneralAdmin.fxml",
                "TPV - Administrador"
        );

        if (loader != null) {
            ControladorGeneralAdmin controladorGeneral = loader.getController();
            // Usamos el método que marca el botón para que aparezca en rojo oscuro
            controladorGeneral.setVistaInicial(rutaInterna);
        }
    }
}