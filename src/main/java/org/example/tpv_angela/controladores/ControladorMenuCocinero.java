package org.example.tpv_angela.controladores;

import javafx.event.ActionEvent;
import org.example.tpv_angela.Navegacion;

public class ControladorMenuCocinero
{
    public void cerrarSesion(ActionEvent event)
    {
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaPantallaInicio.fxml", "Login");
    }
    public void menu(ActionEvent event)
    {
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaMenuCocinero.fxml", "Login");
    }
    public void listaCompra(ActionEvent event)
    {
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaListaCompra.fxml", "Login");
    }
}
