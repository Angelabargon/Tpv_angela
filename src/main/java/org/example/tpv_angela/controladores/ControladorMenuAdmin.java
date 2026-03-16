package org.example.tpv_angela.controladores;

import javafx.event.ActionEvent;
import org.example.tpv_angela.Navegacion;

public class ControladorMenuAdmin
{
    public void cerrarSesion(ActionEvent event)
    {
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaPantallaInicio.fxml", "Login");
    }
    public void ventas(ActionEvent event)
    {
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaVentas.fxml", "Login");
    }
    public void mapaMesas(ActionEvent event)
    {
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaMapaMesas.fxml", "Login");
    }
    public void paraRecoger(ActionEvent event)
    {
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaParaLlevar.fxml", "Login");
    }
    public void menuPrincipal(ActionEvent event)
    {
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaMenuPrincipal.fxml", "Login");
    }
    public void cierreCaja(ActionEvent event)
    {
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaCierreCaja.fxml", "Login");
    }
    public void compra(ActionEvent event)
    {
        Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaCompraAdmin.fxml", "Login");
    }
}
