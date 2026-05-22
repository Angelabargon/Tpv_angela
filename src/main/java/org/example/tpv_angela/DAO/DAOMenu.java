package org.example.tpv_angela.DAO;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.example.tpv_angela.conexion.MongoDBConexion;
import org.example.tpv_angela.modelos.Producto;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO encargado de acceder y modificar productos, mesas, platos del día y lista de compra.
 */
public class DAOMenu {
    private static final String IMAGEN_PLATO_DEFAULT = "C:\\Users\\angel\\Downloads\\biblioteca\\Tpv_angela\\src\\main\\resources\\org\\example\\tpv_angela\\imagenes\\platoDefault.jpg";
    private final MongoDatabase database;

    /**
     * Crea una instancia de DAOMenu con los datos necesarios para su uso.
     */
    public DAOMenu() {
        this.database = MongoDBConexion.getDatabase();
    }

    /**
     * Obtiene datos desde la base de datos o desde el estado actual.
     * @return resultado calculado por el método.
     */
    public List<Producto> obtenerTodosLosProductos() {
        List<Producto> productos = new ArrayList<>();
        try {
            limpiarEstadosTemporales();

            for (Document doc : obtenerMenuFijoActivos()) {
                productos.add(convertirProducto(doc, doc.getString("tipo") != null ? doc.getString("tipo") : "menu_regular"));
            }

            for (Document doc : obtenerPlatosDiaActivos()) {
                productos.add(convertirProducto(doc, "plato_dia"));
            }
        } catch (Exception e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
        }
        return productos;
    }

    /**
     * Obtiene datos desde la base de datos o desde el estado actual.
     * @param numMesa número de mesa.
     * @return resultado calculado por el método.
     */
    public List<String> obtenerProductosMesa(int numMesa) {
        try {
            MongoCollection<Document> coleccion = getColeccionMesas();
            Document mesa = coleccion.find(filtroMesa(numMesa)).first();
            if (mesa != null && mesa.get("productosActuales") != null) {
                return (List<String>) mesa.get("productosActuales");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param numMesa número de mesa.
     * @return resultado calculado por el método.
     */
    public boolean existeMesa(int numMesa) {
        try {
            MongoCollection<Document> coleccion = getColeccionMesas();
            return coleccion.find(filtroMesa(numMesa)).first() != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param numMesa número de mesa.
     * @return resultado calculado por el método.
     */
    public boolean mesaTieneProductos(int numMesa) {
        return !obtenerProductosMesa(numMesa).isEmpty();
    }

    /**
     * Obtiene datos desde la base de datos o desde el estado actual.
     * @return resultado calculado por el método.
     */
    public List<Document> obtenerMesas() {
        try {
            return getColeccionMesas()
                    .find()
                    .sort(new Document("numero", 1))
                    .into(new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene datos desde la base de datos o desde el estado actual.
     * @return resultado calculado por el método.
     */
    public List<Document> obtenerMenuFijoDocumentos() {
        try {
            limpiarEstadosTemporales();
            return obtenerMenuFijoActivos();
        } catch (Exception e) {
            System.err.println("Error al obtener menú fijo: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene datos desde la base de datos o desde el estado actual.
     * @return resultado calculado por el método.
     */
    public List<Document> obtenerPlatosDelDiaDocumentos() {
        try {
            limpiarEstadosTemporales();
            return obtenerPlatosDiaActivos();
        } catch (Exception e) {
            System.err.println("Error al obtener platos del día: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene datos desde la base de datos o desde el estado actual.
     * @return resultado calculado por el método.
     */
    public List<Document> obtenerCartaCompletaDocumentos() {
        List<Document> carta = new ArrayList<>();
        try {
            limpiarEstadosTemporales();
            carta.addAll(obtenerMenuFijoActivos());
            for (Document doc : obtenerPlatosDiaActivos()) {
                carta.add(normalizarPlatoDia(doc));
            }
        } catch (Exception e) {
            System.err.println("Error al obtener carta completa: " + e.getMessage());
        }
        return carta;
    }

    /**
     * Agrega un nuevo elemento a la base de datos o a la vista.
     * @param nombre nombre del producto o elemento.
     * @param categoria categoría del producto.
     * @param subcategoria subcategoría del producto.
     * @param precio precio del producto.
     * @param imagen ruta o nombre de la imagen asociada.
     */
    public void agregarProductoMenuFijo(String nombre, String categoria, String subcategoria, double precio, String imagen) {
        Document doc = new Document("nombre", nombre)
                .append("categoria", categoria)
                .append("subcategoria", subcategoria)
                .append("precio", precio)
                .append("disponible", true)
                .append("tipo", "menu_regular")
                .append("imagen", imagenPlato(imagen))
                .append("alergenos", new ArrayList<>());
        getColeccionMenuFijo().insertOne(doc);
    }

    /**
     * Actualiza los datos de un producto existente del menú fijo.
     * @param producto producto seleccionado para modificar.
     * @param nombre nuevo nombre del producto.
     * @param categoria nueva categoría del producto.
     * @param subcategoria nueva subcategoría del producto.
     * @param precio nuevo precio del producto.
     * @param imagen nueva imagen del producto.
     */
    public void actualizarProductoMenuFijo(Document producto, String nombre, String categoria, String subcategoria, double precio, String imagen) {
        Object id = producto == null ? null : producto.get("_id");
        if (id == null) {
            return;
        }

        getColeccionMenuFijo().updateOne(
                Filters.eq("_id", id),
                Updates.combine(
                        Updates.set("nombre", nombre),
                        Updates.set("categoria", categoria),
                        Updates.set("subcategoria", subcategoria),
                        Updates.set("precio", precio),
                        Updates.set("imagen", imagenPlato(imagen))
                )
        );
    }

    /**
     * Elimina el elemento indicado del flujo de datos de la aplicación.
     * @param producto producto o documento seleccionado.
     */
    public void eliminarProductoMenuFijo(Document producto) {
        Object id = producto.get("_id");
        if (id != null) {
            getColeccionMenuFijo().deleteOne(Filters.eq("_id", id));
        }
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param producto producto o documento seleccionado.
     */
    public void deshabilitarProducto24h(Document producto) {
        Object id = producto.get("_id");
        if (id != null) {
            Date hasta = fechaFinDiaSiguiente();
            getColeccionMenuFijo().updateOne(
                    Filters.eq("_id", id),
                    Updates.combine(
                            Updates.set("disponible", false),
                            Updates.set("deshabilitadoHasta", hasta)
                    )
            );
        }
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param producto producto o documento seleccionado.
     */
    public void rehabilitarProducto(Document producto) {
        Object id = producto.get("_id");
        if (id != null) {
            getColeccionMenuFijo().updateOne(
                    Filters.eq("_id", id),
                    Updates.combine(
                            Updates.set("disponible", true),
                            Updates.unset("deshabilitadoHasta")
                    )
            );
        }
    }

    /**
     * Agrega un nuevo elemento a la base de datos o a la vista.
     * @param nombre nombre del producto o elemento.
     * @param categoria categoría del producto.
     * @param subcategoria subcategoría del producto.
     * @param precio precio del producto.
     * @param imagen ruta o nombre de la imagen asociada.
     */
    public void agregarPlatoDelDia(String nombre, String categoria, String subcategoria, double precio, String imagen) {
        Date ahora = new Date();
        Date hasta = fechaFinDiaSiguiente();
        Document doc = new Document("nombre", nombre)
                .append("categoria", "PLATO DEL DIA")
                .append("subcategoria", textoOGeneral(subcategoria, "GENERAL"))
                .append("precio", precio)
                .append("disponible", true)
                .append("tipo", "plato_dia")
                .append("imagen", imagenPlato(imagen))
                .append("creadoEn", ahora)
                .append("disponibleHasta", hasta)
                .append("alergenos", new ArrayList<>());
        getColeccionPlatoDia().insertOne(doc);
    }

    /**
     * Obtiene datos desde la base de datos o desde el estado actual.
     * @param estado estado usado para filtrar la consulta.
     * @return resultado calculado por el método.
     */
    public List<Document> obtenerListaCompra(String estado) {
        limpiarEstadosTemporales();
        procesarComprasAutomaticas();
        Bson filtro = estado == null ? new Document() : Filters.eq("estado", estado);
        return getColeccionListaCompra().find(filtro).sort(Sorts.descending("fechaEnvio", "fechaCreacion")).into(new ArrayList<>());
    }

    /**
     * Agrega un nuevo elemento a la base de datos o a la vista.
     * @param ingrediente ingrediente de la lista de compra.
     * @param cantidad cantidad solicitada.
     */
    public void agregarItemListaCompra(String ingrediente, String cantidad) {
        Document doc = new Document("ingrediente", ingrediente)
                .append("cantidad", cantidad)
                .append("estado", "borrador")
                .append("fechaCreacion", new Date());
        getColeccionListaCompra().insertOne(doc);
    }

    /**
     * Método auxiliar usado por esta clase.
     */
    public void enviarListaCompraPendiente() {
        getColeccionListaCompra().updateMany(
                Filters.eq("estado", "borrador"),
                Updates.combine(
                        Updates.set("estado", "enviado"),
                        Updates.set("fechaEnvio", new Date())
                )
        );
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param item elemento seleccionado o línea de ticket.
     */
    public void marcarListaCompraGestionada(Document item) {
        Object id = item.get("_id");
        if (id != null) {
            getColeccionListaCompra().updateOne(
                    Filters.eq("_id", id),
                    Updates.combine(
                            Updates.set("estado", "recibido"),
                            Updates.set("fechaRevision", new Date())
                    )
            );
        }
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param item elemento seleccionado o línea de ticket.
     */
    public void comprarItemListaCompra(Document item) {
        Object id = item.get("_id");
        if (id != null) {
            getColeccionListaCompra().updateOne(
                    Filters.eq("_id", id),
                    Updates.combine(
                            Updates.set("estado", "comprado"),
                            Updates.set("fechaCompra", new Date()),
                            Updates.set("compraAutomatica", false)
                    )
            );
        }
    }

    /**
     * Agrega un nuevo elemento a la base de datos o a la vista.
     * @param numero número identificador.
     * @param capacidad capacidad de comensales de la mesa.
     */
    public void eliminarItemListaCompra(Document item) {
        Object id = item == null ? null : item.get("_id");
        if (id != null) {
            getColeccionListaCompra().deleteOne(Filters.eq("_id", id));
        }
    }

    public void agregarMesa(int numero, int capacidad) {
        Document mesa = new Document("numero", numero)
                .append("capacidad", capacidad)
                .append("estado", "libre")
                .append("productosActuales", new ArrayList<String>());
        getColeccionMesas().insertOne(mesa);
    }

    /**
     * Actualiza los datos básicos de una mesa existente.
     * @param numeroOriginal número actual de la mesa.
     * @param numeroNuevo nuevo número de mesa.
     * @param capacidad nueva capacidad de la mesa.
     */
    public void actualizarMesa(int numeroOriginal, int numeroNuevo, int capacidad) {
        getColeccionMesas().updateOne(
                filtroMesa(numeroOriginal),
                Updates.combine(
                        Updates.set("numero", numeroNuevo),
                        Updates.set("capacidad", capacidad)
                )
        );
    }

    /**
     * Actualiza la información indicada en la fuente de datos.
     * @param numero número identificador.
     * @param posicionX posición horizontal de la mesa.
     * @param posicionY posición vertical de la mesa.
     */
    public void actualizarPosicionMesa(int numero, double posicionX, double posicionY) {
        getColeccionMesas().updateOne(
                filtroMesa(numero),
                Updates.combine(
                        Updates.set("posicionX", posicionX),
                        Updates.set("posicionY", posicionY)
                )
        );
    }

    /**
     * Elimina el elemento indicado del flujo de datos de la aplicación.
     * @param numero número identificador.
     */
    public void eliminarMesa(int numero) {
        getColeccionMesas().deleteOne(filtroMesa(numero));
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param mesaOrigen mesa desde la que se trasladan productos.
     * @param mesaDestino mesa a la que se trasladan productos.
     * @param productosOrigen productos que quedan en la mesa de origen.
     */
    public void traspasarMesa(int mesaOrigen, int mesaDestino, List<String> productosOrigen) {
        try {
            MongoCollection<Document> coleccion = getColeccionMesas();
            List<String> productosDestino = obtenerProductosMesa(mesaDestino);
            productosDestino.addAll(productosOrigen);

            coleccion.updateOne(
                    filtroMesa(mesaDestino),
                    Updates.combine(
                            Updates.set("estado", "ocupada"),
                            Updates.set("productosActuales", productosDestino)
                    )
            );

            coleccion.updateOne(
                    filtroMesa(mesaOrigen),
                    Updates.combine(
                            Updates.set("estado", "libre"),
                            Updates.set("productosActuales", new ArrayList<String>())
                    )
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Agrega un nuevo elemento a la base de datos o a la vista.
     * @param numMesa número de mesa.
     * @param lineaTicket línea de ticket que se añade a la mesa.
     */
    public void agregarProductoAMesa(int numMesa, String lineaTicket) {
        try {
            getColeccionMesas().updateOne(
                    filtroMesa(numMesa),
                    Updates.combine(
                            Updates.push("productosActuales", lineaTicket),
                            Updates.set("estado", "ocupada")
                    )
            );
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Elimina el elemento indicado del flujo de datos de la aplicación.
     * @param numMesa número de mesa.
     * @param item elemento seleccionado o línea de ticket.
     */
    public void eliminarProductoDeMesa(int numMesa, String item) {
        Document mesa =  getColeccionMesas().find(filtroMesa(numMesa)).first();

        if (mesa == null) return;

        List<String> productos = (List<String>) mesa.get("productosActuales");

        if (productos == null || productos.isEmpty()) return;

        boolean eliminado = productos.remove(item);

        if (eliminado) {
            getColeccionMesas().updateOne(
                    filtroMesa(numMesa),
                    Updates.combine(
                            Updates.set("productosActuales", productos),
                            Updates.set("estado", productos.isEmpty() ? "libre" : "ocupada")
                    )
            );
        }
    }

    /**
     * Actualiza la información indicada en la fuente de datos.
     * @param numeroMesa número de la mesa.
     * @param nuevoEstado nuevo estado que se asigna a la mesa.
     */
    public void actualizarEstadoMesa(int numeroMesa, String nuevoEstado) {
        try {
            MongoCollection<Document> coleccion = getColeccionMesas();
            if (nuevoEstado.equalsIgnoreCase("libre")) {
                coleccion.updateOne(filtroMesa(numeroMesa),
                        Updates.combine(
                                Updates.set("estado", nuevoEstado),
                                Updates.set("productosActuales", new ArrayList<String>())
                        ));
            } else {
                coleccion.updateOne(filtroMesa(numeroMesa), Updates.set("estado", nuevoEstado));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param numMesa número de mesa.
     * @return resultado calculado por el método.
     */
    private Bson filtroMesa(int numMesa) {
        String mesaTexto = String.valueOf(numMesa);
        return Filters.or(
                Filters.eq("numero", numMesa),
                Filters.eq("numero", mesaTexto),
                Filters.eq("numMesa", numMesa),
                Filters.eq("numMesa", mesaTexto),
                Filters.eq("numeroMesa", numMesa),
                Filters.eq("numeroMesa", mesaTexto),
                Filters.eq("mesa", numMesa),
                Filters.eq("mesa", mesaTexto),
                Filters.eq("_id", numMesa),
                Filters.eq("_id", mesaTexto)
        );
    }

    /**
     * Convierte datos de MongoDB al modelo usado por la aplicación.
     * @param doc documento de MongoDB que se procesa.
     * @param tipoPorDefecto tipo usado cuando el documento no define uno.
     * @return resultado calculado por el método.
     */
    private Producto convertirProducto(Document doc, String tipoPorDefecto) {
        double precioExtraido = 0.0;
        Object p = doc.get("precio");
        if (p instanceof Number) precioExtraido = ((Number) p).doubleValue();
        String tipo = doc.getString("tipo") != null ? doc.getString("tipo") : tipoPorDefecto;

        return new Producto(
                textoOGeneral(doc.getString("nombre"), "Producto"),
                textoOGeneral(doc.getString("categoria"), tipo.equals("plato_dia") ? "PLATO DEL DIA" : "OTROS"),
                textoOGeneral(doc.getString("subcategoria"), "GENERAL"),
                precioExtraido,
                tipo,
                imagenPlato(doc.getString("imagen")),
                productoDisponible(doc)
        );
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param doc documento de MongoDB que se procesa.
     * @return resultado calculado por el método.
     */
    private Document normalizarPlatoDia(Document doc) {
        Document normalizado = new Document(doc);
        normalizado.put("categoria", textoOGeneral(doc.getString("categoria"), "PLATO DEL DIA"));
        normalizado.put("subcategoria", textoOGeneral(doc.getString("subcategoria"), "GENERAL"));
        normalizado.put("tipo", "plato_dia");
        normalizado.put("disponible", productoDisponible(doc));
        normalizado.put("imagen", imagenPlato(doc.getString("imagen")));
        return normalizado;
    }

    private List<Document> obtenerMenuFijoActivos() {
        List<Document> documentos = new ArrayList<>();
        for (Document doc : getColeccionMenuFijo().find().sort(Sorts.ascending("categoria", "subcategoria", "nombre"))) {
            if (!esPlatoDia(doc)) {
                documentos.add(doc);
            }
        }
        return documentos;
    }

    private List<Document> obtenerPlatosDiaActivos() {
        List<Document> platos = new ArrayList<>();
        for (MongoCollection<Document> coleccion : getColeccionesPlatoDia()) {
            for (Document doc : coleccion.find().sort(Sorts.ascending("nombre"))) {
                if (productoDisponible(doc)) {
                    platos.add(doc);
                }
            }
        }
        return platos;
    }

    private boolean esPlatoDia(Document doc) {
        String tipo = doc.getString("tipo");
        String categoria = doc.getString("categoria");
        return "plato_dia".equalsIgnoreCase(tipo)
                || "PLATO DEL DIA".equalsIgnoreCase(categoria)
                || "PLATO DEL DÍA".equalsIgnoreCase(categoria);
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param texto texto que se procesa.
     * @param fallback valor usado cuando el texto está vacío.
     * @return resultado calculado por el método.
     */
    private String textoOGeneral(String texto, String fallback) {
        return texto == null || texto.isBlank() ? fallback : texto.trim();
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param imagen ruta o nombre de la imagen asociada.
     * @return resultado calculado por el método.
     */
    private String imagenPlato(String imagen) {
        return imagen == null || imagen.isBlank() ? IMAGEN_PLATO_DEFAULT : imagen.trim();
    }

    /**
     * Calcula la fecha de caducidad temporal a las 00:00 del día siguiente.
     * @return fecha exacta de fin de vigencia.
     */
    private Date fechaFinDiaSiguiente() {
        return Date.from(LocalDate.now()
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param doc documento de MongoDB que se procesa.
     * @return resultado calculado por el método.
     */
    private boolean productoDisponible(Document doc) {
        Date ahora = new Date();
        Object deshabilitadoHasta = doc.get("deshabilitadoHasta");
        if (deshabilitadoHasta instanceof Date fecha && fecha.after(ahora)) {
            return false;
        }

        Object disponibleHasta = doc.get("disponibleHasta");
        if (disponibleHasta instanceof Date fecha && !fecha.after(ahora)) {
            return false;
        }

        return doc.getBoolean("disponible", true);
    }

    /**
     * Limpia datos temporales o campos de la interfaz.
     */
    private void limpiarEstadosTemporales() {
        Date ahora = new Date();
        getColeccionMenuFijo().updateMany(
                Filters.and(
                        Filters.lte("deshabilitadoHasta", ahora),
                        Filters.eq("disponible", false)
                ),
                Updates.combine(
                        Updates.set("disponible", true),
                        Updates.unset("deshabilitadoHasta")
                )
        );

        for (MongoCollection<Document> coleccion : getColeccionesPlatoDia()) {
            coleccion.deleteMany(Filters.lte("disponibleHasta", ahora));
        }
    }

    /**
     * Método auxiliar usado por esta clase.
     */
    private void procesarComprasAutomaticas() {
        Date haceSieteDias = new Date(System.currentTimeMillis() - 7L * 24L * 60L * 60L * 1000L);
        getColeccionListaCompra().updateMany(
                Filters.and(
                        Filters.eq("estado", "enviado"),
                        Filters.lte("fechaEnvio", haceSieteDias)
                ),
                Updates.combine(
                        Updates.set("estado", "comprado"),
                        Updates.set("fechaCompra", new Date()),
                        Updates.set("compraAutomatica", true)
                )
        );
    }

    /**
     * Devuelve la colección o valor asociado.
     * @return resultado calculado por el método.
     */
    private MongoCollection<Document> getColeccionMesas() {
        List<String> colecciones = database.listCollectionNames().into(new ArrayList<>());
        if (colecciones.contains("Mesas")) {
            return database.getCollection("Mesas");
        }
        return database.getCollection("mesas");
    }

    /**
     * Devuelve la colección o valor asociado.
     * @return resultado calculado por el método.
     */
    private MongoCollection<Document> getColeccionMenuFijo() {
        return database.getCollection("Menu_Principal");
    }

    /**
     * Devuelve la colección o valor asociado.
     * @return resultado calculado por el método.
     */
    private MongoCollection<Document> getColeccionPlatoDia() {
        List<String> colecciones = database.listCollectionNames().into(new ArrayList<>());
        if (colecciones.contains("Plato_Dia")) {
            return database.getCollection("Plato_Dia");
        }
        if (colecciones.contains("Platos_Dia")) {
            return database.getCollection("Platos_Dia");
        }
        if (colecciones.contains("plato_dia")) {
            return database.getCollection("plato_dia");
        }
        return database.getCollection("Plato_Dia");
    }

    private List<MongoCollection<Document>> getColeccionesPlatoDia() {
        List<String> colecciones = database.listCollectionNames().into(new ArrayList<>());
        List<MongoCollection<Document>> resultado = new ArrayList<>();
        String[] nombres = {"Plato_Dia", "Platos_Dia", "plato_dia"};
        for (String nombre : nombres) {
            if (colecciones.contains(nombre)) {
                resultado.add(database.getCollection(nombre));
            }
        }
        if (resultado.isEmpty()) {
            resultado.add(database.getCollection("Plato_Dia"));
        }
        return resultado;
    }

    /**
     * Devuelve la colección o valor asociado.
     * @return resultado calculado por el método.
     */
    private MongoCollection<Document> getColeccionListaCompra() {
        List<String> colecciones = database.listCollectionNames().into(new ArrayList<>());
        if (colecciones.contains("Lista_Compra")) {
            return database.getCollection("Lista_Compra");
        }
        return database.getCollection("Lista_Compra");
    }
}
