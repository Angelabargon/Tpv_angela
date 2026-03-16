package org.example.tpv_angela.DAO;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.example.tpv_angela.conexion.MongoDBConexion;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

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
     * Método que verifica si las credenciales existen en MongoDB
     * @return
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
}
