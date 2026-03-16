package org.example.tpv_angela;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args)
    {
        System.setProperty("prism.allowhidpi", "false");
        Application.launch(HelloApplication.class, args);
    }
}
