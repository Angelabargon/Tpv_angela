package org.example.tpv_angela.controladores.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import org.example.tpv_angela.Navegacion;

/**
 * Controlador del menú inicial del administrador y su navegación principal.
 */
public class ControladorMenuAdmin {

    /**
     * Método auxiliar usado por esta clase.
     * @param event evento lanzado por la interfaz.
     */
    public void cerrarSesion(ActionEvent event) {
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaPantallaInicio.fxml", "Pantalla Inicial");
    }

    /**
     * Abre el formulario para cambiar el usuario y la contraseña del administrador.
     */
    public void cambiarCredenciales() {
        CambiosUsuario.mostrar();
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param event evento lanzado por la interfaz.
     */
    public void mapaMesas(ActionEvent event) {
        navegarAContenedor(event, "/org/example/tpv_angela/vistas/admin/VistaMapaMesasAdmin.fxml");
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param event evento lanzado por la interfaz.
     */
    public void menuPrincipal(ActionEvent event) {
        navegarAContenedor(event, "/org/example/tpv_angela/vistas/admin/VistaGestionCarta.fxml");
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param event evento lanzado por la interfaz.
     */
    public void cierreCaja(ActionEvent event) {
        navegarAContenedor(event, "/org/example/tpv_angela/vistas/admin/VistaCajaAdmin.fxml");
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param event evento lanzado por la interfaz.
     */
    public void compra(ActionEvent event) {
        navegarAContenedor(event, "/org/example/tpv_angela/vistas/admin/VistaListaCompraAdmin.fxml");
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param event evento lanzado por la interfaz.
     * @param rutaInterna ruta FXML que se carga en el contenedor.
     */
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