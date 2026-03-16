package org.example.tpv_angela;

/**
 * Hacemos los imports necesarios.
 */
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * Creamos la clase Navegación que almacenará los métodos encargados de los cambios entre páginas,
 * así como sus cierres.
 */
public class Navegacion {

    /**
     * Creamos un metodo para cambiar de la página actual a otra.
     *
     * @param event  Evento que dispara la acción (ej. clic en botón).
     * @param rutaFXML  Ruta del archivo FXML de la nueva vista.
     * @param titulo  Título de la ventana.
     */
    public static void cambiarVista(ActionEvent event, String rutaFXML, String titulo)
    {
        try
        {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    Navegacion.class.getResource(rutaFXML)
            );
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle(titulo);
            stage.show();
        }
        catch (Exception e)
        {
            System.out.println("Error al cambiar a la vista: " + rutaFXML);
            e.printStackTrace();
        }
    }

    /**
     * Creamos un metodo cerrar una ventana y vover a la página anterior.
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
