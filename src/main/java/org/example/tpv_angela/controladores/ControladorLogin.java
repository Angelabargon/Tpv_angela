package org.example.tpv_angela.controladores;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.bson.Document;
import org.example.tpv_angela.ControladorAlertas;
import org.example.tpv_angela.DAO.DAOLogin;
import org.example.tpv_angela.Navegacion;

import java.io.IOException;

/**
 * Controlador de login para autenticar que el usuario que entra existe
 */

public class ControladorLogin
{
    /**
     * Atributos fxml
     */
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;

    /**
     * Atributos privados de la clase
     */
    private final DAOLogin loginDAO = new DAOLogin();

    /** Método para que el usuario pueda pulsar enter y entrar
     */
    @FXML
    public void initialize()
    {
        btnLogin.setDefaultButton(true);
    }

    /** Método login principal
     * @param event
     */
    @FXML
    private void entrarComoAdmin(javafx.event.ActionEvent event)
    {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        //Validación para ver si los campos están vacíos
        if (user.isEmpty() || pass.isEmpty())
        {
            shakeEffect(btnLogin);
            mostrarAlerta("Campos incompletos", "Por favor, introduce usuario y contraseña.");
        }
        else
        {
            // Comprobación en la Base de Datos a través del DAO
            Document usuarioEncontrado = loginDAO.validarUsuario(user, pass);
            if (usuarioEncontrado != null)
            {
                //Login correcto
                Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaMenuInicialAdmin.fxml", "Menu Inicial");
            }
            else
            {
                //Login incorrecto
                shakeEffect(btnLogin);
                mostrarAlerta("Error de autenticación", "El usuario o la contraseña no son correctos.");
            }
        }
    }

    /** Método auxiliar para mostrar alertas de error
     * @param titulo
     * @param mensaje
     */
    private void mostrarAlerta(String titulo, String mensaje)
    {
        try
        {
            Stage stagePrincipal = (Stage) btnLogin.getScene().getWindow();
            Scene escenaPrincipal = btnLogin.getScene();

            // Efecto de oscurecimiento al fondo
            ColorAdjust adj = new ColorAdjust();
            adj.setBrightness(-0.5);
            escenaPrincipal.getRoot().setEffect(adj);

            // Cargamos el FXML de la alerta
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/tpv_angela/vistas/VistaAlertas.fxml"));
            Parent root = loader.load();

            ControladorAlertas controlador = loader.getController();
            controlador.configurarAlerta(titulo, mensaje);
            Stage stageAlerta = new Stage();
            stageAlerta.initModality(Modality.APPLICATION_MODAL);
            stageAlerta.initOwner(stagePrincipal);
            stageAlerta.initStyle(StageStyle.UNDECORATED);
            Scene sceneAlerta = new Scene(root);
            stageAlerta.setScene(sceneAlerta);
            stageAlerta.showAndWait();

            // Quitamos el efecto
            escenaPrincipal.getRoot().setEffect(null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método que le añade un efecto al botón de login si se entra mal
     * @param node
     */
    private void shakeEffect(javafx.scene.Node node)
    {
        // Creamos la animación de vibración (movimiento lateral)
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setByX(10f);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }

    /**
     * Método para volver a la pantalla principal
     * @param event
     */
    @FXML
    private void salir(javafx.event.ActionEvent event)
    {Navegacion.cambiarVista(event, "/org/example/tpv_angela/vistas/VistaPantallaInicio.fxml", "Pantalla de inicio");}
}