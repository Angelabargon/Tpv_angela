package org.example.tpv_angela;

import javafx.application.Application;

/**
 * Lanzador auxiliar que inicia la aplicación JavaFX desde el método main.
 */
public class Launcher {
    public static void main(String[] args)
    {
        System.setProperty("prism.allowhidpi", "false");
        Application.launch(HelloApplication.class, args);
    }
}
