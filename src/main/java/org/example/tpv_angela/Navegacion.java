package org.example.tpv_angela;

/**
 * Hacemos los imports necesarios.
 */
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.tpv_angela.controladores.TecladoTactil;

/**
 * Creamos la clase Navegación que almacenará los métodos encargados de los cambios entre páginas,
 * así como sus cierres.
 */
public class Navegacion {

    /**
     * Creamos un método para cambiar de la página actual a otra, devuelve el loader para acceder a los controladores.
     *
     * @param event  Evento que dispara la acción (ej. clic en botón).
     * @param rutaFXML  Ruta del archivo FXML de la nueva vista.
     * @param titulo  Título de la ventana.
     */
    public static FXMLLoader cambiarVista(ActionEvent event, String rutaFXML, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(Navegacion.class.getResource(rutaFXML));
            Parent root = loader.load();
            if (rutaFXML.contains("/admin/") || rutaFXML.contains("/camarero/") || rutaFXML.contains("/cocinero/")) {
                TecladoTactil.instalar(root);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.show();

            return loader;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Creamos un método cerrar una ventana y volver a la página anterior.
     *
     * @param event  Evento que dispara la acción.
     */
    public static void cerrarVentana(ActionEvent event)
    {
        String titulo = "Título";
        try
        {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            titulo = stage.getTitle();
            stage.close();
        }
        catch (Exception e)
        {
            System.out.println("Error al cerrar la vista: " + titulo);
            e.printStackTrace();
        }
    }
}
