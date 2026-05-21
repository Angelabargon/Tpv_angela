package org.example.tpv_angela.controladores.admin;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.example.tpv_angela.ControladorAlertas;
import org.example.tpv_angela.DAO.DAOLogin;
import org.example.tpv_angela.IconoApp;
import org.example.tpv_angela.controladores.TecladoTactil;

/**
 * Diálogo reutilizable para cambiar el usuario y la contraseña del administrador.
 */
public final class CambiosUsuario
{
    /**
     * Evita la creación de instancias de esta clase de utilidad.
     */
    private CambiosUsuario() {}
    /**
     * Muestra la ventana de cambio de credenciales y guarda los cambios si son válidos.
     */
    public static void mostrar()
    {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Cambiar usuario / contraseña");
        IconoApp.aplicar(dialogo);
        dialogo.setHeaderText("Cambia los datos de acceso del administrador");
        ButtonType guardar = new ButtonType("GUARDAR", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("CANCELAR", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().setAll(guardar, cancelar);
        dialogo.getDialogPane().getStylesheets().add(CambiosUsuario.class.getResource("/org/example/tpv_angela/estilo/style.css").toExternalForm());
        dialogo.getDialogPane().getStyleClass().add("credentials-dialog");

        TextField txtUsuarioActual = new TextField();
        txtUsuarioActual.setPromptText("Usuario actual");
        PasswordField txtPasswordActual = new PasswordField();
        txtPasswordActual.setPromptText("Contrasena actual");
        TextField txtNuevoUsuario = new TextField();
        txtNuevoUsuario.setPromptText("Nuevo usuario");
        PasswordField txtNuevaPassword = new PasswordField();
        txtNuevaPassword.setPromptText("Minimo 4 digitos");
        PasswordField txtRepetirPassword = new PasswordField();
        txtRepetirPassword.setPromptText("Repetir contraseña");
        txtUsuarioActual.getStyleClass().add("credentials-input");
        txtPasswordActual.getStyleClass().add("credentials-input");
        txtNuevoUsuario.getStyleClass().add("credentials-input");
        txtNuevaPassword.getStyleClass().add("credentials-input");
        txtRepetirPassword.getStyleClass().add("credentials-input");

        GridPane contenido = new GridPane();
        contenido.setHgap(18);
        contenido.setVgap(14);
        contenido.setPadding(new Insets(24));
        contenido.getStyleClass().add("credentials-form");
        ColumnConstraints columnaEtiquetas = new ColumnConstraints();
        columnaEtiquetas.setMinWidth(180);
        ColumnConstraints columnaCampos = new ColumnConstraints();
        columnaCampos.setHgrow(Priority.ALWAYS);
        columnaCampos.setMinWidth(280);
        contenido.getColumnConstraints().setAll(columnaEtiquetas, columnaCampos);

        contenido.add(crearEtiqueta("Usuario actual:"), 0, 0);
        contenido.add(txtUsuarioActual, 1, 0);
        contenido.add(crearEtiqueta("Contrasena actual:"), 0, 1);
        contenido.add(txtPasswordActual, 1, 1);
        contenido.add(crearEtiqueta("Nuevo usuario:"), 0, 2);
        contenido.add(txtNuevoUsuario, 1, 2);
        contenido.add(crearEtiqueta("Nueva contraseña:"), 0, 3);
        contenido.add(txtNuevaPassword, 1, 3);
        contenido.add(crearEtiqueta("Repetir contraseña:"), 0, 4);
        contenido.add(txtRepetirPassword, 1, 4);

        dialogo.getDialogPane().setContent(contenido);
        TecladoTactil.instalar(dialogo.getDialogPane());
        Button btnGuardar = (Button) dialogo.getDialogPane().lookupButton(guardar);
        Button btnCancelar = (Button) dialogo.getDialogPane().lookupButton(cancelar);
        btnGuardar.getStyleClass().add("credentials-save-button");
        btnCancelar.getStyleClass().add("credentials-cancel-button");
        btnGuardar.addEventFilter(ActionEvent.ACTION, event ->
        {
            if (!guardarCredenciales(
                    txtUsuarioActual.getText(),
                    txtPasswordActual.getText(),
                    txtNuevoUsuario.getText(),
                    txtNuevaPassword.getText(),
                    txtRepetirPassword.getText()
            ))
            {event.consume();}
        });
        dialogo.showAndWait();
    }

    /**
     * Crea una etiqueta preparada para el formulario de credenciales.
     * @param texto texto de la etiqueta.
     * @return etiqueta estilizada.
     */
    private static Label crearEtiqueta(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("credentials-label");
        return etiqueta;
    }

    /**
     * Valida los campos del formulario y actualiza la base de datos en MongoDB.
     * @param usuarioActual usuario actual.
     * @param passwordActual contraseña actual.
     * @param nuevoUsuario nuevo usuario.
     * @param nuevaPassword nueva contraseña.
     * @param repetirPassword repetición de la nueva contraseña.
     * @return true si el cambio se ha guardado correctamente.
     */
    private static boolean guardarCredenciales(String usuarioActual, String passwordActual, String nuevoUsuario, String nuevaPassword, String repetirPassword)
    {
        String usuarioActualLimpio = textoLimpio(usuarioActual);
        String nuevoUsuarioLimpio = textoLimpio(nuevoUsuario);
        String passwordActualLimpia = textoLimpio(passwordActual);
        String nuevaPasswordLimpia = textoLimpio(nuevaPassword);
        String repetirPasswordLimpia = textoLimpio(repetirPassword);

        if (usuarioActualLimpio.isEmpty() || passwordActualLimpia.isEmpty() || nuevoUsuarioLimpio.isEmpty() || nuevaPasswordLimpia.isEmpty())
        {
            ControladorAlertas.mostrar("Campos incompletos", "Rellena todos los campos.");
            return false;
        }
        if (nuevaPasswordLimpia.length() < 4)
        {
            ControladorAlertas.mostrar("Contraseña no valida", "La contraseña debe tener mínimo 4 dígitos.");
            return false;
        }
        if (!nuevaPasswordLimpia.equals(repetirPasswordLimpia))
        {
            ControladorAlertas.mostrar("Contraseña no valida", "Las contraseñas no coinciden.");
            return false;
        }
        DAOLogin daoLogin = new DAOLogin();
        if (!daoLogin.cambiarCredenciales(usuarioActualLimpio, passwordActualLimpia, nuevoUsuarioLimpio, nuevaPasswordLimpia))
        {
            ControladorAlertas.mostrar("Credenciales incorrectas", "El usuario o la contraseña actual no son correctos.");
            return false;
        }
        ControladorAlertas.mostrar("Datos actualizados", "Usuario y contraseña actualizados correctamente.");
        return true;
    }

    /**
     * Limpia los espacios innecesarios de un texto para evitar nullpointerexception
     * @param texto texto recibido.
     * @return texto sin espacios iniciales ni finales.
     */
    private static String textoLimpio(String texto)
    {return texto == null ? "" : texto.trim();}
}
