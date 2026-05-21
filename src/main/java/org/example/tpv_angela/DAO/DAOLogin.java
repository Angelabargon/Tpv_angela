package org.example.tpv_angela.DAO;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.example.tpv_angela.conexion.MongoDBConexion;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * DAO encargado de validar credenciales de usuario contra MongoDB.
 */
public class DAOLogin
{
    private final MongoCollection<Document> usuariosCollection;

    /**
     * Método que obtiene la base de datos
     */
    public DAOLogin()
    {
        MongoDatabase db = MongoDBConexion.getDatabase();
        this.usuariosCollection = db.getCollection("Usuarios");
    }

    /**
     * Método que verifica si las credenciales existen en MongoDB.
     * @param username nombre de usuario introducido.
     * @param password contraseña introducida.
     * @return documento del usuario encontrado o null si no existe.
     */
    public Document validarUsuario(String username, String password)
    {
        return usuariosCollection.find(
                and(
                        eq("userName", username),
                        eq("passwd", password)
                )
        ).first();
    }

    /**
     * Cambia el usuario y la contraseña de una cuenta si las credenciales actuales son correctas.
     * @param usernameActual usuario actual.
     * @param passwordActual contraseña actual.
     * @param nuevoUsername nuevo usuario.
     * @param nuevaPassword nueva contraseña.
     * @return true si se ha encontrado y actualizado la cuenta.
     */
    public boolean cambiarCredenciales(String usernameActual, String passwordActual, String nuevoUsername, String nuevaPassword)
    {
        Document usuario = validarUsuario(usernameActual, passwordActual);
        if (usuario == null || usuario.get("_id") == null) {
            return false;
        }

        usuariosCollection.updateOne(
                eq("_id", usuario.get("_id")),
                Updates.combine(
                        Updates.set("userName", nuevoUsername),
                        Updates.set("passwd", nuevaPassword)
                )
        );
        return true;
    }
}