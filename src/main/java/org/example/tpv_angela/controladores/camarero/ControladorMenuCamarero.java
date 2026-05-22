package org.example.tpv_angela.controladores.camarero;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import org.example.tpv_angela.ControladorAlertas;
import org.example.tpv_angela.DAO.DAOArqueoCaja;
import org.example.tpv_angela.Navegacion;

/**
 * Controlador del menú inicial del camarero y sus accesos principales.
 */
public class ControladorMenuCamarero {
    private final DAOArqueoCaja daoArqueo = new DAOArqueoCaja();

    public void cerrarSesion(ActionEvent event)
    {Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaPantallaInicio.fxml", "Login");}

    public void ventas(ActionEvent event)
    {
        if (ventasBloqueadas()) {
            return;
        }
        navegarAContenedor(event, "VistaVentas.fxml");
    }

    public void mapaMesas(ActionEvent event)
    {
        navegarAContenedor(event, "/org/example/tpv_angela/vistas/VistaMapaMesas.fxml");
    }

    public void menuPrincipal(ActionEvent event)
    {navegarAContenedor(event, "/org/example/tpv_angela/vistas/VistaMenuCarta.fxml");}

    public void cierreCaja(ActionEvent event)
    {navegarAContenedor(event, "VistaCaja.fxml");}

    /**
     * Método auxiliar para cargar la carcasa y luego la vista interna
     */
    private void navegarAContenedor(ActionEvent event, String nombreVistaInterna)
    {
        // Cargamos la vista general que contiene el menú lateral
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

    private boolean ventasBloqueadas() {
        if (!daoArqueo.cajaCerradaCamareroHoy()) {
            return false;
        }
        ControladorAlertas.mostrar("VENTA BLOQUEADA", "La caja del dia ya esta cerrada. No se pueden realizar mas cobros.");
        return true;
    }
}
