package org.example.tpv_angela;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ControladorAlertas
{
    @FXML private Label lblTitulo;
    @FXML private Label lblMensaje;

    public void configurarAlerta(String titulo, String mensaje)

    {
        lblTitulo.setText(titulo);
        lblMensaje.setText(mensaje);
    }

    @FXML
    private void cerrarAlerta(javafx.event.ActionEvent event)
    {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
