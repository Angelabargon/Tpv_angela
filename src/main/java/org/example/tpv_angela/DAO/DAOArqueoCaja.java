package org.example.tpv_angela.DAO;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.example.tpv_angela.conexion.MongoDBConexion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO responsable de guardar y consultar arqueos y movimientos de caja.
 */
public class DAOArqueoCaja {
    private final MongoCollection<Document> coleccionVentas;
    private final MongoCollection<Document> coleccionArqueos;
    private final MongoCollection<Document> coleccionMovimientosCaja;
    private final SimpleDateFormat formatoDia = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Crea una instancia de DAOArqueoCaja con los datos necesarios para su uso.
     */
    public DAOArqueoCaja() {
        MongoDatabase db = MongoDBConexion.getDatabase();
        this.coleccionVentas = db.getCollection("ventas");
        this.coleccionArqueos = db.getCollection("arqueos_caja");
        this.coleccionMovimientosCaja = db.getCollection("movimientos_caja");
    }

    /**
     * Obtiene el dato solicitado por la vista o por la lógica de negocio.
     * @return resumen del día con los importes calculados.
     */
    public ResumenDia obtenerResumenHoy() {
        Date inicio = inicioDia(new Date());
        Date fin = finDia(new Date());
        ResumenDia resumen = obtenerResumenVentas(inicio, fin);
        resumen.retiradoEfectivo = obtenerRetiradoEfectivo(inicio, fin);
        return resumen;
    }

    /**
     * Guarda la información introducida por el usuario tras validarla.
     * @param efectivoDeclarado importe de efectivo declarado.
     * @param tarjetaDeclarada importe declarado como cobrado con tarjeta.
     * @param observaciones observaciones introducidas en el cierre.
     * @return documento con la información solicitada.
     */
    public Document guardarArqueoCamarero(double efectivoDeclarado, double tarjetaDeclarada, String observaciones) {
        if (existeArqueoCamareroHoy()) {
            throw new IllegalStateException("El arqueo del día ya esta guardado.");
        }
        ResumenDia resumen = obtenerResumenHoy();
        Document arqueo = new Document("fecha", new Date())
                .append("dia", formatoDia.format(new Date()))
                .append("nombre", "cuentas de día " + formatoDia.format(new Date()))
                .append("rol", "CAMARERO")
                .append("ventasTotales", resumen.totalVentas)
                .append("ventasEfectivo", resumen.ventasEfectivo)
                .append("ventasTarjeta", resumen.ventasTarjeta)
                .append("retiradoEfectivo", resumen.retiradoEfectivo)
                .append("efectivoEsperado", resumen.efectivoEnCaja())
                .append("efectivoDeclarado", efectivoDeclarado)
                .append("tarjetaDeclarada", tarjetaDeclarada)
                .append("descuadreEfectivo", efectivoDeclarado - resumen.efectivoEnCaja())
                .append("descuadreTarjeta", tarjetaDeclarada - resumen.ventasTarjeta)
                .append("numeroVentas", resumen.numeroVentas)
                .append("observaciones", observaciones == null ? "" : observaciones.trim());
        coleccionArqueos.insertOne(arqueo);
        return arqueo;
    }

    /**
     * Comprueba si el camarero ya ha guardado el arqueo del día.
     * @return true si existe un arqueo de camarero para hoy; false en caso contrario.
     */
    public boolean existeArqueoCamareroHoy() {
        return coleccionArqueos.countDocuments(Filters.and(
                Filters.eq("dia", formatoDia.format(new Date())),
                Filters.eq("rol", "CAMARERO"),
                Filters.exists("efectivoDeclarado", true)
        )) > 0;
    }

    /**
     * Cierra la vista o finaliza el flujo activo.
     * @return documento con la información solicitada.
     */
    public Document cerrarCajaCamarero() {
        ResumenDia resumen = obtenerResumenHoy();
        Document cierre = new Document("fecha", new Date())
                .append("dia", resumen.dia)
                .append("nombre", "cuentas de día " + resumen.dia)
                .append("rol", "CAMARERO")
                .append("estado", "CERRADA")
                .append("ventasTotales", resumen.totalVentas)
                .append("ventasEfectivo", resumen.ventasEfectivo)
                .append("ventasTarjeta", resumen.ventasTarjeta)
                .append("retiradoEfectivo", resumen.retiradoEfectivo)
                .append("efectivoEsperado", resumen.efectivoEnCaja())
                .append("numeroVentas", resumen.numeroVentas)
                .append("cuentasDia", listarCuentasHoy());
        coleccionArqueos.insertOne(cierre);
        return cierre;
    }

    /**
     * Registra la operación indicada en la capa de datos.
     * @param importe importe de la operación.
     * @param motivo motivo registrado para el movimiento.
     * @return documento con la información solicitada.
     */
    public Document registrarRetiradaAdmin(double importe, String motivo) {
        Document movimiento = new Document("fecha", new Date())
                .append("dia", formatoDia.format(new Date()))
                .append("tipo", "RETIRADA")
                .append("rol", "ADMIN")
                .append("importe", importe)
                .append("motivo", motivo == null ? "" : motivo.trim());
        coleccionMovimientosCaja.insertOne(movimiento);
        return movimiento;
    }

    /**
     * Devuelve la lista de documentos solicitada desde la base de datos.
     * @return lista de datos solicitada.
     */
    public List<Document> listarArqueos() {
        return coleccionArqueos.find().sort(Sorts.descending("fecha")).limit(60).into(new ArrayList<>());
    }

    /**
     * Devuelve la lista de documentos solicitada desde la base de datos.
     * @return lista de datos solicitada.
     */
    public List<Document> listarMovimientosCaja() {
        return coleccionMovimientosCaja.find().sort(Sorts.descending("fecha")).limit(60).into(new ArrayList<>());
    }

    /**
     * Devuelve la lista de documentos solicitada desde la base de datos.
     * @return lista de datos solicitada.
     */
    public List<Document> listarCuentasHoy() {
        Date inicio = inicioDia(new Date());
        Date fin = finDia(new Date());
        return agruparCuentasPorTicket(coleccionVentas.find(Filters.and(
                Filters.gte("fecha", inicio),
                Filters.lte("fecha", fin)
        )).sort(Sorts.descending("fecha")).into(new ArrayList<>()));
    }

    /**
     * Agrupa los documentos recibidos según el criterio de ticket.
     * @param cuentas cuentas que se agrupan.
     * @return lista de datos solicitada.
     */
    private List<Document> agruparCuentasPorTicket(List<Document> cuentas) {
        Map<String, Document> agrupadas = new LinkedHashMap<>();
        for (Document cuenta : cuentas) {
            String clave = claveTicket(cuenta);
            Document agrupada = agrupadas.get(clave);

            if (agrupada == null) {
                Document nueva = new Document(cuenta);
                nueva.put("ventasEfectivo", valorPorMetodo(cuenta, "EFECTIVO"));
                nueva.put("ventasTarjeta", valorPorMetodo(cuenta, "TARJETA"));
                nueva.put("pagos", pagosCuenta(cuenta));
                nueva.put("metodoPago", metodoResumen(numero(nueva.get("ventasEfectivo")), numero(nueva.get("ventasTarjeta"))));
                nueva.put("nombre", nombreTicket(nueva));
                nueva.put("numeroPagos", pagosCuenta(cuenta).size());
                agrupadas.put(clave, nueva);
            } else {
                double total = numero(agrupada.get("total")) + numero(cuenta.get("total"));
                double efectivo = numero(agrupada.get("ventasEfectivo")) + valorPorMetodo(cuenta, "EFECTIVO");
                double tarjeta = numero(agrupada.get("ventasTarjeta")) + valorPorMetodo(cuenta, "TARJETA");
                List<Document> pagos = (List<Document>) agrupada.get("pagos");
                pagos.addAll(pagosCuenta(cuenta));

                agrupada.put("total", total);
                agrupada.put("ventasEfectivo", efectivo);
                agrupada.put("ventasTarjeta", tarjeta);
                agrupada.put("metodoPago", metodoResumen(efectivo, tarjeta));
                agrupada.put("pagos", pagos);
                agrupada.put("nombre", nombreTicket(agrupada));
                agrupada.put("numeroPagos", pagos.size());
            }
        }
        return new ArrayList<>(agrupadas.values());
    }

    /**
     * Genera la clave usada para identificar el ticket de una cuenta.
     * @param cuenta documento de la cuenta que se procesa.
     * @return texto formateado o normalizado.
     */
    private String claveTicket(Document cuenta) {
        Object ticketId = cuenta.get("ticketId");
        if (ticketId != null && !String.valueOf(ticketId).isBlank()) {
            return String.valueOf(ticketId);
        }
        return valorTexto(cuenta.get("mesa")) + "|" + String.valueOf(cuenta.get("productos"));
    }

    /**
     * Genera un nombre seguro para usarlo como archivo local.
     * @param cuenta documento de la cuenta que se procesa.
     * @return texto formateado o normalizado.
     */
    private String nombreTicket(Document cuenta) {
        return "Ticket mesa " + valorTexto(cuenta.get("mesa"));
    }

    /**
     * Ejecuta la operación asociada a este controlador o servicio.
     * @param cuenta documento de la cuenta que se procesa.
     * @param metodoBuscado método de pago o criterio usado.
     * @return valor numérico calculado.
     */
    private double valorPorMetodo(Document cuenta, String metodoBuscado) {
        Object valorDirecto = cuenta.get(metodoBuscado.equals("EFECTIVO") ? "ventasEfectivo" : "ventasTarjeta");
        if (valorDirecto instanceof Number) {
            return ((Number) valorDirecto).doubleValue();
        }

        String metodo = String.valueOf(cuenta.get("metodoPago", "")).toUpperCase();
        return metodo.contains(metodoBuscado) ? numero(cuenta.get("total")) : 0.0;
    }

    /**
     * Obtiene la lista de pagos asociados a la cuenta indicada.
     * @param cuenta documento de la cuenta que se procesa.
     * @return lista de datos solicitada.
     */
    private List<Document> pagosCuenta(Document cuenta) {
        Object pagos = cuenta.get("pagos");
        if (pagos instanceof List<?>) {
            List<Document> pagosConvertidos = new ArrayList<>();
            for (Object pago : (List<?>) pagos) {
                if (pago instanceof Document document) {
                    pagosConvertidos.add(document);
                }
            }
            if (!pagosConvertidos.isEmpty()) {
                return pagosConvertidos;
            }
        }

        List<Document> pagoUnico = new ArrayList<>();
        pagoUnico.add(new Document("fecha", cuenta.get("fecha"))
                .append("metodoPago", cuenta.get("metodoPago", ""))
                .append("importe", numero(cuenta.get("total"))));
        return pagoUnico;
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
     * Obtiene el dato solicitado por la vista o por la lógica de negocio.
     * @param inicio fecha inicial del intervalo.
     * @param fin fecha final del intervalo.
     * @return resumen del día con los importes calculados.
     */
    private ResumenDia obtenerResumenVentas(Date inicio, Date fin) {
        ResumenDia resumen = new ResumenDia();
        resumen.dia = formatoDia.format(inicio);

        for (Document venta : coleccionVentas.find(Filters.and(
                Filters.gte("fecha", inicio),
                Filters.lte("fecha", fin)
        ))) {
            double total = numero(venta.get("total"));
            String metodo = String.valueOf(venta.get("metodoPago", "")).toUpperCase();

            resumen.totalVentas += total;
            resumen.numeroVentas++;
            if (metodo.contains("EFECTIVO")) {
                resumen.ventasEfectivo += total;
            } else if (metodo.contains("TARJETA")) {
                resumen.ventasTarjeta += total;
            }
        }
        return resumen;
    }

    /**
     * Obtiene el dato solicitado por la vista o por la lógica de negocio.
     * @param inicio fecha inicial del intervalo.
     * @param fin fecha final del intervalo.
     * @return valor numérico calculado.
     */
    private double obtenerRetiradoEfectivo(Date inicio, Date fin) {
        double total = 0.0;
        for (Document movimiento : coleccionMovimientosCaja.find(Filters.and(
                Filters.eq("tipo", "RETIRADA"),
                Filters.gte("fecha", inicio),
                Filters.lte("fecha", fin)
        ))) {
            total += numero(movimiento.get("importe"));
        }
        return total;
    }

    /**
     * Calcula el inicio del día para la fecha recibida.
     * @param fecha fecha usada para calcular o filtrar datos.
     * @return fecha calculada.
     */
    private Date inicioDia(Date fecha) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Calcula el final del día para la fecha recibida.
     * @param fecha fecha usada para calcular o filtrar datos.
     * @return fecha calculada.
     */
    private Date finDia(Date fecha) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(inicioDia(fecha));
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        return calendar.getTime();
    }

    /**
     * Convierte el valor recibido a número decimal de forma segura.
     * @param valor valor que se procesa.
     * @return valor numérico calculado.
     */
    private double numero(Object valor) {
        return valor instanceof Number ? ((Number) valor).doubleValue() : 0.0;
    }

    /**
     * Devuelve una representación de texto segura para el valor recibido.
     * @param valor valor que se procesa.
     * @return texto formateado o normalizado.
     */
    private String valorTexto(Object valor) {
        return valor == null ? "" : String.valueOf(valor);
    }

    /**
     * Resumen calculado con ventas, cobros y movimientos del día.
     */
    public static class ResumenDia {
        /**
         * Día resumido en formato yyyy-MM-dd.
         */
        public String dia = "";
        /**
         * Total vendido durante el día.
         */
        public double totalVentas;
        /**
         * Total cobrado en efectivo.
         */
        public double ventasEfectivo;
        /**
         * Total cobrado con tarjeta.
         */
        public double ventasTarjeta;
        /**
         * Efectivo retirado de caja.
         */
        public double retiradoEfectivo;
        /**
         * Número de ventas registradas.
         */
        public int numeroVentas;

        /**
         * Ejecuta la operación asociada a este controlador o servicio.
         * @return valor numérico calculado.
         */
        public double efectivoEnCaja() {
            return ventasEfectivo - retiradoEfectivo;
        }
    }
}
