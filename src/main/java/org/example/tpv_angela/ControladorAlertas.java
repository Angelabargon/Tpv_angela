package org.example.tpv_angela;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Controlador de la ventana FXML usada para mostrar alertas personalizadas al usuario.
 */
public class ControladorAlertas
{
    @FXML private Label lblTitulo;
    @FXML private Label lblMensaje;
    @FXML private Button btnAccion;

    private Runnable accionAlCerrar;

    /**
     * Muestra la alerta personalizada sin aplicar efectos sobre una ventana padre.
     * @param titulo título de la alerta.
     * @param mensaje mensaje que se muestra al usuario.
     */
    public static void mostrar(String titulo, String mensaje)
    {
        mostrar(titulo, mensaje, null, false);
    }

    /**
     * Muestra la alerta personalizada asociada a un nodo de la ventana actual.
     * @param titulo título de la alerta.
     * @param mensaje mensaje que se muestra al usuario.
     * @param nodoPropietario nodo usado para obtener la ventana propietaria.
     */
    public static void mostrar(String titulo, String mensaje, Node nodoPropietario)
    {
        mostrar(titulo, mensaje, nodoPropietario, false);
    }

    /**
     * Muestra la alerta personalizada y, si se indica, oscurece la ventana de fondo.
     * @param titulo título de la alerta.
     * @param mensaje mensaje que se muestra al usuario.
     * @param nodoPropietario nodo usado para obtener la ventana propietaria.
     * @param oscurecerFondo true para oscurecer la ventana propietaria mientras se muestra la alerta.
     */
    public static void mostrar(String titulo, String mensaje, Node nodoPropietario, boolean oscurecerFondo)
    {
        mostrar(titulo, mensaje, nodoPropietario, oscurecerFondo, "ENTENDIDO", null);
    }

    /**
     * Muestra una alerta personalizada con texto de botón y acción final.
     * @param titulo título de la alerta.
     * @param mensaje mensaje que se muestra al usuario.
     * @param nodoPropietario nodo usado para obtener la ventana propietaria.
     * @param textoBoton texto mostrado en el botón principal.
     * @param accion acción ejecutada al cerrar la alerta.
     */
    public static void mostrarConAccion(String titulo, String mensaje, Node nodoPropietario, String textoBoton, Runnable accion)
    {
        mostrar(titulo, mensaje, nodoPropietario, true, textoBoton, accion);
    }

    private static void mostrar(String titulo, String mensaje, Node nodoPropietario, boolean oscurecerFondo, String textoBoton, Runnable accion)
    {
        Scene escenaPropietaria = nodoPropietario == null ? null : nodoPropietario.getScene();
        Stage stagePropietario = escenaPropietaria == null ? null : (Stage) escenaPropietaria.getWindow();

        try
        {
            if (oscurecerFondo && escenaPropietaria != null)
            {
                ColorAdjust adj = new ColorAdjust();
                adj.setBrightness(-0.5);
                escenaPropietaria.getRoot().setEffect(adj);
            }

            FXMLLoader loader = new FXMLLoader(ControladorAlertas.class.getResource("/org/example/tpv_angela/vistas/VistaAlertas.fxml"));
            Parent root = loader.load();
            ControladorAlertas controlador = loader.getController();
            controlador.configurarAlerta(titulo, mensaje, textoBoton, accion);

            Stage stageAlerta = new Stage();
            stageAlerta.initModality(Modality.APPLICATION_MODAL);
            if (stagePropietario != null)
            {
                stageAlerta.initOwner(stagePropietario);
            }
            stageAlerta.initStyle(StageStyle.UNDECORATED);
            stageAlerta.setScene(new Scene(root));
            IconoApp.aplicar(stageAlerta);
            stageAlerta.showAndWait();
        }
        catch (IOException e)
        {
            System.out.println(mensaje);
        }
        finally
        {
            if (oscurecerFondo && escenaPropietaria != null)
            {
                escenaPropietaria.getRoot().setEffect(null);
            }
        }
    }

    /**
     * Configura el texto principal de la alerta personalizada.
     * @param titulo título de la alerta.
     * @param mensaje mensaje que se muestra al usuario.
     */
    public void configurarAlerta(String titulo, String mensaje)

    {
        configurarAlerta(titulo, mensaje, "ENTENDIDO", null);
    }

    /**
     * Configura el texto, el botón y la acción de la alerta personalizada.
     * @param titulo título de la alerta.
     * @param mensaje mensaje que se muestra al usuario.
     * @param textoBoton texto mostrado en el botón principal.
     * @param accion acción ejecutada al cerrar la alerta.
     */
    public void configurarAlerta(String titulo, String mensaje, String textoBoton, Runnable accion)
    {
        lblTitulo.setText(titulo);
        lblMensaje.setText(mensaje);
        btnAccion.setText(textoBoton == null || textoBoton.isBlank() ? "ENTENDIDO" : textoBoton);
        accionAlCerrar = accion;
    }

    /**
     * Cierra la ventana de alerta.
     * @param event evento de acción del botón de cierre.
     */
    @FXML
    private void cerrarAlerta(javafx.event.ActionEvent event)
    {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close();
        if (accionAlCerrar != null) {
            accionAlCerrar.run();
        }
    }
}
