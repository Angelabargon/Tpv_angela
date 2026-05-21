package org.example.tpv_angela.DAO;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.example.tpv_angela.conexion.MongoDBConexion;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * DAO responsable de consultar, guardar y actualizar ventas de mesas y tickets.
 */
public class DAOVentas {
    private final MongoCollection<Document> coleccionVentas;
    private final MongoCollection<Document> coleccionMesas;
    private final MongoCollection<Document> coleccionCuentasDia;
    private final SimpleDateFormat formatoDia = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Crea una instancia de DAOVentas con los datos necesarios para su uso.
     */
    public DAOVentas() {
        MongoDatabase db = MongoDBConexion.getDatabase();
        this.coleccionVentas = db.getCollection("ventas");
        this.coleccionMesas = db.listCollectionNames().into(new ArrayList<>()).contains("Mesas")
                ? db.getCollection("Mesas")
                : db.getCollection("mesas");
        this.coleccionCuentasDia = db.getCollection("cuentas_dia");
    }

    /**
     * Registra la operación indicada en la capa de datos.
     * @param numMesa número o documento de la mesa implicada.
     * @param items elemento seleccionado o procesado.
     * @param total importe total asociado a la operación.
     * @param metodo método de pago o criterio usado.
     */
    public void registrarVenta(int numMesa, List<String> items, double total, String metodo) {
        registrarVenta(numMesa, items, total, metodo, null);
    }

    /**
     * Registra la operación indicada en la capa de datos.
     * @param numMesa número o documento de la mesa implicada.
     * @param items elemento seleccionado o procesado.
     * @param total importe total asociado a la operación.
     * @param metodo método de pago o criterio usado.
     * @param ticketId identificador del ticket.
     */
    public void registrarVenta(int numMesa, List<String> items, double total, String metodo, String ticketId) {
        Date fecha = new Date();
        Document venta = new Document("fecha", fecha)
                .append("mesa", numMesa)
                .append("productos", items)
                .append("total", total)
                .append("metodoPago", metodo)
                .append("tipo", "NORMAL");
        if (ticketId != null && !ticketId.isBlank()) {
            venta.append("ticketId", ticketId);
        }
        coleccionVentas.insertOne(venta);
        registrarCuentaDia(fecha, numMesa, items, total, metodo, "NORMAL", ticketId);
    }

    /**
     * Registra la operación indicada en la capa de datos.
     * @param numMesa número o documento de la mesa implicada.
     * @param items elemento seleccionado o procesado.
     * @param total importe total asociado a la operación.
     * @param tipoSeparacion tipo de separación o cobro realizado.
     */
    public void registrarSeparacion(int numMesa, List<String> items, double total, String tipoSeparacion) {
        Date fecha = new Date();
        Document venta = new Document("fecha", fecha)
                .append("mesa", numMesa)
                .append("productos", items)
                .append("total", total)
                .append("metodoPago", "SEPARADO")
                .append("tipo", tipoSeparacion);
        coleccionVentas.insertOne(venta);
        registrarCuentaDia(fecha, numMesa, items, total, "SEPARADO", tipoSeparacion, null);
    }

    /**
     * Registra la operación indicada en la capa de datos.
     * @param fecha fecha usada para calcular o filtrar datos.
     * @param numMesa número o documento de la mesa implicada.
     * @param items elemento seleccionado o procesado.
     * @param total importe total asociado a la operación.
     * @param metodo método de pago o criterio usado.
     * @param tipo tipo de documento o cobro.
     * @param ticketId identificador del ticket.
     */
    private void registrarCuentaDia(Date fecha, int numMesa, List<String> items, double total, String metodo, String tipo, String ticketId) {
        String dia = formatoDia.format(fecha);
        String idTicket = ticketId == null || ticketId.isBlank()
                ? "ticket-" + numMesa + "-" + fecha.getTime()
                : ticketId;

        Document pago = new Document("fecha", fecha)
                .append("metodoPago", metodo)
                .append("importe", total);

        Document cuentaExistente = coleccionCuentasDia.find(Filters.eq("ticketId", idTicket)).first();
        if (cuentaExistente == null) {
            Document cuenta = new Document("nombre", "cuentas de día " + dia)
                    .append("ticketId", idTicket)
                    .append("fecha", fecha)
                    .append("ultimaFecha", fecha)
                    .append("dia", dia)
                    .append("mesa", numMesa)
                    .append("productos", items)
                    .append("total", total)
                    .append("ventasEfectivo", metodo.equalsIgnoreCase("EFECTIVO") ? total : 0.0)
                    .append("ventasTarjeta", metodo.equalsIgnoreCase("TARJETA") ? total : 0.0)
                    .append("metodoPago", metodo)
                    .append("tipo", tipo)
                    .append("pagos", new ArrayList<>(List.of(pago)));
            coleccionCuentasDia.insertOne(cuenta);
            return;
        }

        double efectivo = numero(cuentaExistente.get("ventasEfectivo"));
        double tarjeta = numero(cuentaExistente.get("ventasTarjeta"));
        if (metodo.equalsIgnoreCase("EFECTIVO")) {
            efectivo += total;
        } else if (metodo.equalsIgnoreCase("TARJETA")) {
            tarjeta += total;
        }

        List<Document> pagos = (List<Document>) cuentaExistente.get("pagos");
        if (pagos == null) {
            pagos = new ArrayList<>();
        }
        pagos.add(pago);

        List<String> productos = (List<String>) cuentaExistente.get("productos");
        if (productos == null) {
            productos = new ArrayList<>();
        }
        productos.addAll(items);

        cuentaExistente
                .append("ultimaFecha", fecha)
                .append("productos", productos)
                .append("total", numero(cuentaExistente.get("total")) + total)
                .append("ventasEfectivo", efectivo)
                .append("ventasTarjeta", tarjeta)
                .append("metodoPago", metodoResumen(efectivo, tarjeta))
                .append("pagos", pagos);
        coleccionCuentasDia.replaceOne(Filters.eq("ticketId", idTicket), cuentaExistente);
    }

    /**
     * Calcula el texto resumen del método de pago a partir de los importes.
     * @param efectivo importe en efectivo.
     * @param tarjeta importe de tarjeta o tarjeta visual, según el contexto.
     * @return texto formateado o normalizado.
     */
    private String metodoResumen(double efectivo, double tarjeta) {
        if (efectivo > 0 && tarjeta > 0) {
            return "EFECTIVO + TARJETA";
        }
        if (tarjeta > 0) {
            return "TARJETA";
        }
        return "EFECTIVO";
    }

    /**
     * Convierte el valor recibido a número decimal de forma segura.
     * @param valor valor que se procesa.
     * @return valor numérico calculado.
     */
    private double numero(Object valor) {
        return valor instanceof Number ? ((Number) valor).doubleValue() : 0.0;
    }
    // Método para obtener el ticket actual de una mesa (Para que no se borre al navegar)
    /**
     * Obtiene el dato solicitado por la vista o por la lógica de negocio.
     * @param numMesa número o documento de la mesa implicada.
     * @return documento con la información solicitada.
     */
    public Document obtenerTicketMesa(int numMesa) {
        return coleccionMesas.find(Filters.or(
                Filters.eq("numero", numMesa),
                Filters.eq("numero", String.valueOf(numMesa))
        )).first();
    }
}
