package org.example.tpv_angela.controladores.camarero;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import org.example.tpv_angela.Navegacion;

public class ControladorMenuCamarero {

    public void cerrarSesion(ActionEvent event)
    {Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaPantallaInicio.fxml", "Login");}

    public void ventas(ActionEvent event)
    {navegarAContenedor(event, "VistaVentas.fxml");}

    public void mapaMesas(ActionEvent event)
    {navegarAContenedor(event, "VistaMapaMesas.fxml");}

    public void menuPrincipal(ActionEvent event)
    {navegarAContenedor(event, "VistaMenuPrincipal.fxml");}

    public void cierreCaja(ActionEvent event)
    {navegarAContenedor(event, "VistaCierreCaja.fxml");}

    /**
     * Método auxiliar para cargar la carcasa y luego la vista interna
     */
    private void navegarAContenedor(ActionEvent event, String nombreVistaInterna)
    {
        // Cargamos la vista general que contiene el menu lateral
        FXMLLoader loader = Navegacion.cambiarVista(
                event,
                "/org/example/tpv_angela/vistas/camarero/VistaGeneralCamarero.fxml",
                "Sistema TPV - Camarero"
        );

        if (loader != null) {
            // Obtenemos el controlador de esa Vista General
            ControladorGeneralCamarero controladorGeneral = loader.getController();
            // Le decimos qué vista cargar en su AnchorPane central
            controladorGeneral.setVistaInicial(nombreVistaInterna);
        }
    }
}