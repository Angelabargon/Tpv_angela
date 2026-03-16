package org.example.tpv_angela.controladores;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.example.tpv_angela.Navegacion;

public class ControladorPantallaInicio
{
    /** Botón que redirige a la vista de Login. */
    @FXML
    private Button login;

    /** Constructor vacío. */
    public ControladorPantallaInicio() {}

    /**
     * Cambia la vista actual a la pantalla de Login.
     *
     * @param event evento de acción generado al pulsar el botón de Login.
     */
    public void irALogin(javafx.event.ActionEvent event)
    {Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaLogin.fxml", "Login");}

    public void irACamarero(javafx.event.ActionEvent event)
    {Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaMenuInicialCamarero.fxml", "Login");}

    public void irACocinero(javafx.event.ActionEvent event)
    {Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaMenuInicialCocinero.fxml", "Login");}
}



