package org.example.tpv_angela;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Punto de entrada JavaFX que carga la primera vista de la aplicación.
 */
public class HelloApplication extends Application {
    @Override
    /**
     * Ejecuta la operación asociada a este controlador o servicio.
     * @param stage ventana principal de JavaFX.
     * @throws IOException si ocurre un error durante la operación.
     */
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/org/example/tpv_angela/vistas/VistaPantallaInicio.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("TPV_ABG");
        stage.setScene(scene);
        stage.setMaximized(true);
        IconoApp.aplicar(stage);
        stage.show();
    }
}
