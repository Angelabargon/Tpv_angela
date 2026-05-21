package org.example.tpv_angela.conexion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONObject;

/**
 * Gestiona la conexión compartida con MongoDB y el acceso a la base de datos de la aplicación.
 */
public class MongoDBConexion
{
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    /**
     * Este método lee el JSON local y conecta con MongoDB
     */
    public static void iniciarConexion()
    {
        try
        {
            var resource = MongoDBConexion.class.getResourceAsStream("/org/example/tpv_angela/conexion/conexionBD.json");

            if (resource == null) {
                System.err.println("No se encontró conexionBD.json en el paquete");
                return;
            }

            String content = new String(resource.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            JSONObject config = new JSONObject(content);

            String host = config.getString("host");
            int puerto = config.getInt("puerto");
            String dbName = config.getString("database");

            String uri = "mongodb://"  + host + ":" + puerto + "/" + dbName;

            mongoClient = MongoClients.create(uri);
            database = mongoClient.getDatabase(dbName);

            System.out.println("Conexión exitosa a la base de datos: " + dbName);

        }
        catch (Exception e)
        {System.err.println("Error al conectar a MongoDB: " + e.getMessage());}
    }

    /**
     * Método para obtener la base de datos desde otras clases.
     * @return base de datos MongoDB usada por la aplicación.
     */
    public static MongoDatabase getDatabase()
    {
        if (database == null)
        {iniciarConexion();}
        return database;
    }

    /** Método para cerrar la conexión al cerrar la App
     */
    public static void cerrar()
    {
        if (mongoClient != null)
        {mongoClient.close();}
    }
}