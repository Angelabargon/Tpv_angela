package org.example.tpv_angela;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/org/example/tpv_angela/vistas/VistaPantallaInicio.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("HostelHub");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/tpv_angela/imagenes/logo.png"))));        stage.show();
    }
}
