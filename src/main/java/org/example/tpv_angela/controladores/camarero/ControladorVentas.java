package org.example.tpv_angela.controladores.camarero;

import com.lowagie.text.Element;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.collections.ListChangeListener;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.tpv_angela.ControladorAlertas;
import org.example.tpv_angela.DAO.DAOMenu;
import org.example.tpv_angela.DAO.DAOVentas;
import org.example.tpv_angela.IconoApp;
import org.example.tpv_angela.controladores.MenuDesplegableCarta;
import org.example.tpv_angela.modelos.Producto;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

/**
 * Controlador principal de ventas del camarero: gestiona mesas, ticket, cobros y PDF.
 */
public class ControladorVentas {
    @FXML private Accordion acordeonMenu;
    @FXML private ListView<String> listaTicket;
    @FXML private Label lblDisplay, lblTotal, lblMesaActiva;

    private static Integer mesaInicialPendiente;

    private String buffer = "";
    private double totalAcumulado = 0.0;
    private double totalInicialMesa = 0.0;
    private double pagadoEfectivoAcumulado = 0.0;
    private double pagadoTarjetaAcumulado = 0.0;
    private double efectivoRecibidoAcumulado = 0.0;
    private double cambioDevueltoAcumulado = 0.0;
    private boolean mesaCerrada = false;
    private int mesaActual = -1;
    private String ticketActualId;

    private DAOMenu menuDAO = new DAOMenu();
    private DAOVentas ventasDAO = new DAOVentas();

    /**
     * Inicializa la vista y prepara los datos necesarios al cargar el FXML.
     */
    @FXML
    public void initialize() {
        generarMenuDinamico();
        configurarActualizacionInstantanea();
        configurarEliminacionPorDobleClick();
        if (mesaInicialPendiente != null) {
            abrirMesa(mesaInicialPendiente);
            mesaInicialPendiente = null;
        }
    }

    /**
     * Actualiza el valor asociado a este campo.
     * @param numMesa número de mesa.
     */
    public static void setMesaInicialPendiente(int numMesa) {
        mesaInicialPendiente = numMesa;
    }

    /**
     * Genera contenido visual o documentos a partir de los datos disponibles.
     */
    private void generarMenuDinamico() {
        MenuDesplegableCarta.generarDesdeProductos(acordeonMenu, menuDAO.obtenerTodosLosProductos(), this::anadirAlTicket, getClass());
    }

    /**
     * Método auxiliar usado por esta clase.
     */
    private void configurarActualizacionInstantanea() {
        listaTicket.getItems().addListener((ListChangeListener<String>) cambio -> actualizarTotalDesdeLista());
    }

    /**
     * Método auxiliar usado por esta clase.
     */
    private void configurarEliminacionPorDobleClick() {
        listaTicket.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                eliminarLineaSeleccionadaConConfirmacion();
            }
        });
    }

    /**
     * Atiende la acción disparada desde la interfaz de usuario.
     * @param event evento lanzado por la interfaz.
     */
    @FXML
    private void handleTeclado(javafx.event.ActionEvent event) {
        String tecla = ((Button)event.getSource()).getText();
        if (tecla.equals("C")) {
            buffer = "";
            lblDisplay.setText("0");
        } else {
            if (buffer.equals("0")) buffer = "";
            buffer += tecla;
            lblDisplay.setText(buffer);
        }
    }

    /**
     * Atiende la acción disparada desde la interfaz de usuario.
     */
    @FXML
    private void handleMesa() {
        if (buffer.isEmpty()) return;
        try {
            int numMesa = Integer.parseInt(buffer);
            abrirMesa(numMesa);
        } catch (Exception e) { buffer = ""; lblDisplay.setText("0"); }
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param numMesa número de mesa.
     */
    private void abrirMesa(int numMesa) {
        if (!menuDAO.existeMesa(numMesa)) {
            ControladorAlertas.mostrar("ERROR", "La mesa " + numMesa + " no existe.");
            buffer = "";
            lblDisplay.setText("0");
            return;
        }
        lblMesaActiva.setText("MESA: " + numMesa);
        mesaActual = numMesa;
        ticketActualId = "mesa-" + numMesa + "-" + System.currentTimeMillis();

        pagadoEfectivoAcumulado = 0.0;
        pagadoTarjetaAcumulado = 0.0;
        efectivoRecibidoAcumulado = 0.0;
        cambioDevueltoAcumulado = 0.0;

        cargarCuentaMesa(numMesa);
        mesaCerrada = false;
        acordeonMenu.setDisable(false);
        buffer = "";
        lblDisplay.setText("0");
    }

    /**
     * Carga datos y actualiza la vista con la información disponible.
     * @param numMesa número de mesa.
     */
    private void cargarCuentaMesa(int numMesa) {
        listaTicket.getItems().clear();
        List<String> items = menuDAO.obtenerProductosMesa(numMesa);
        listaTicket.getItems().addAll(items);
        actualizarTotalDesdeLista();
        totalInicialMesa = totalAcumulado; // Guardamos el valor total original
    }

    /**
     * Actualiza la información indicada en la fuente de datos.
     */
    private void actualizarTotalDesdeLista() {
        double totalSinDescuento = 0.0;
        for (String linea : listaTicket.getItems()) {
            totalSinDescuento += extraerPrecioLineaTicket(linea);
        }
        totalAcumulado = aplicarDescuentoCamarero(totalSinDescuento);
        lblTotal.setText(formatearTotal());
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param linea línea de ticket que se procesa.
     * @return resultado calculado por el método.
     */
    private double extraerPrecioLineaTicket(String linea) {
        if (linea == null || linea.isBlank()) return 0.0;

        String precio = separarLineaTicket(linea)[1]
                .replace("EUR", "")
                .replace("\u20AC", "")
                .replaceAll("[^0-9,.-]", "")
                .replace(",", ".")
                .trim();

        if (precio.isBlank()) return 0.0;

        try {
            return Double.parseDouble(precio);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Agrega un nuevo elemento a la base de datos o a la vista.
     * @param p producto seleccionado.
     */
    private void anadirAlTicket(Producto p) {
        if (lblMesaActiva.getText().equals("MESA: -") || mesaCerrada) return;
        String linea = p.getNombre() + " .... " + String.format("%.2f", p.getPrecio()) + " \u20AC";
        listaTicket.getItems().add(linea);
        totalInicialMesa = totalAcumulado;
        int numMesa = Integer.parseInt(lblMesaActiva.getText().replace("MESA: ", ""));
        menuDAO.agregarProductoAMesa(numMesa, linea);
    }

    /**
     * Atiende la acción disparada desde la interfaz de usuario.
     */
    @FXML
    private void handleEliminarLinea() {
        eliminarLineaSeleccionadaConConfirmacion();
    }

    /**
     * Atiende la acción disparada desde la interfaz de usuario.
     */
    @FXML
    private void handleEliminarCuenta() {
        eliminarCuentaConConfirmacion();
    }

    /**
     * Elimina el elemento indicado del flujo de datos de la aplicación.
     */
    private void eliminarLineaSeleccionadaConConfirmacion() {
        if (lblMesaActiva.getText().contains("-") || mesaCerrada) {
            return;
        }

        int indice = listaTicket.getSelectionModel().getSelectedIndex();
        if (indice < 0) {
            ControladorAlertas.mostrar("ERROR", "Selecciona una linea para eliminar.");
            return;
        }

        String linea = listaTicket.getItems().get(indice);
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar linea");
        confirmacion.setHeaderText("Seguro que quieres eliminar esta linea?");
        confirmacion.setContentText(linea);
        ButtonType si = new ButtonType("SI", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("NO", ButtonBar.ButtonData.NO);
        confirmacion.getButtonTypes().setAll(si, no);
        aplicarEstiloDialogo(confirmacion);

        if (confirmacion.showAndWait().orElse(no) == si) {
            int numMesa = Integer.parseInt(lblMesaActiva.getText().replace("MESA: ", ""));
            listaTicket.getItems().remove(indice);
            menuDAO.eliminarProductoDeMesa(numMesa, linea);
            totalInicialMesa = totalAcumulado;
        }
    }

    /**
     * Elimina todos los productos de la mesa activa después de confirmar la acción.
     */
    private void eliminarCuentaConConfirmacion() {
        if (lblMesaActiva.getText().contains("-") || mesaCerrada) {
            return;
        }

        if (listaTicket.getItems().isEmpty()) {
            ControladorAlertas.mostrar("ERROR", "La cuenta no tiene lineas para eliminar.");
            return;
        }

        int numMesa = Integer.parseInt(lblMesaActiva.getText().replace("MESA: ", ""));
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar cuenta");
        confirmacion.setHeaderText("Seguro que quieres eliminar esta cuenta?");
        confirmacion.setContentText("Se borraran todas las lineas de la mesa " + numMesa + ".");
        ButtonType si = new ButtonType("SI", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("NO", ButtonBar.ButtonData.NO);
        confirmacion.getButtonTypes().setAll(si, no);
        aplicarEstiloDialogo(confirmacion);

        if (confirmacion.showAndWait().orElse(no) == si) {
            menuDAO.actualizarEstadoMesa(numMesa, "libre");
            listaTicket.getItems().clear();
            lblMesaActiva.setText("MESA: -");
            mesaActual = -1;
            ticketActualId = null;
            mesaCerrada = false;
            acordeonMenu.setDisable(false);
            totalAcumulado = 0.0;
            totalInicialMesa = 0.0;
            pagadoEfectivoAcumulado = 0.0;
            pagadoTarjetaAcumulado = 0.0;
            efectivoRecibidoAcumulado = 0.0;
            cambioDevueltoAcumulado = 0.0;
            limpiarBuffer();
            actualizarTotalDesdeLista();
            ControladorAlertas.mostrar("CUENTA ELIMINADA", "La cuenta se ha eliminado correctamente.");
        }
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param total importe total usado para el cálculo.
     * @return resultado calculado por el método.
     */
    private double aplicarDescuentoCamarero(double total) {
        return esMesaCamarero() ? total * 0.80 : total;
    }

    /**
     * Indica si se cumple la condición evaluada por el método.
     * @return resultado calculado por el método.
     */
    private boolean esMesaCamarero() {
        return mesaActual == 98 || mesaActual == 99;
    }

    /**
     * Formatea datos para mostrarlos en pantalla o en documentos.
     * @return resultado calculado por el método.
     */
    private String formatearTotal() {
        String total = String.format("%.2f \u20AC", totalAcumulado);
        return esMesaCamarero() ? total + "  (-20%)" : total;
    }

    /**
     * Atiende la acción disparada desde la interfaz de usuario.
     */
    @FXML
    private void handleCerrarMesaYBloquear() {
        if (lblMesaActiva.getText().contains("-") || listaTicket.getItems().isEmpty()) {
            ControladorAlertas.mostrar("ERROR", "Mesa vacia.");
            return;
        }
        mesaCerrada = true;
        acordeonMenu.setDisable(true);
        ControladorAlertas.mostrar("CUENTA CERRADA", "Lista para cobro y cierre.");
    }

    @FXML private void handlePagoEfectivo() { ejecutarCobro("EFECTIVO"); }
    @FXML private void handlePagoTarjeta() { ejecutarCobro("TARJETA"); }

    /**
     * Método auxiliar usado por esta clase.
     * @param metodo método de pago utilizado.
     */
    private void ejecutarCobro(String metodo) {
        try {
            double montoEntregado = buffer.isEmpty() ? totalAcumulado : Double.parseDouble(buffer.replace(",", "."));
            double cambio = Math.max(0.0, montoEntregado - totalAcumulado);
            double montoAPagarAhora = Math.min(montoEntregado, totalAcumulado);

            // Acumulamos el pago según el método
            if (metodo.equals("EFECTIVO")) {
                pagadoEfectivoAcumulado += montoAPagarAhora;
                efectivoRecibidoAcumulado += montoEntregado;
                cambioDevueltoAcumulado += cambio;
            } else {
                pagadoTarjetaAcumulado += montoAPagarAhora;
            }

            int numMesa = Integer.parseInt(lblMesaActiva.getText().replace("MESA: ", ""));
            if (ticketActualId == null || ticketActualId.isBlank()) {
                ticketActualId = "mesa-" + numMesa + "-" + System.currentTimeMillis();
            }
            ventasDAO.registrarVenta(numMesa, new ArrayList<>(listaTicket.getItems()), montoAPagarAhora, metodo, ticketActualId);

            totalAcumulado -= montoAPagarAhora;
            lblTotal.setText(formatearTotal());
            lblDisplay.setText("0");
            buffer = "";

            // SOLO SE GENERA RECIBO SI LA MESA SE PAGA ENTERA (Saldo 0)
            if (totalAcumulado <= 0.05) {
                generarPDF(numMesa, "pdf/recibos", "recibo");
                menuDAO.actualizarEstadoMesa(numMesa, "libre");
                listaTicket.getItems().clear();
                lblMesaActiva.setText("MESA: -");
                mesaActual = -1;
                ticketActualId = null;
                mesaCerrada = false;
                acordeonMenu.setDisable(false);
                if (metodo.equals("EFECTIVO")) {
                    ControladorAlertas.mostrar(
                            "FINALIZADO",
                            "Cobrado en efectivo: " + String.format("%.2f \u20AC", montoEntregado)
                                    + "\nDevuelto: " + String.format("%.2f \u20AC", cambio)
                    );
                } else {
                    ControladorAlertas.mostrar("FINALIZADO", "Mesa cobrada integramente. Recibo en /recibos.");
                }
            } else {
                if (metodo.equals("EFECTIVO")) {
                    ControladorAlertas.mostrar(
                            "PAGO PARCIAL",
                            "Cobrado en efectivo: " + String.format("%.2f \u20AC", montoEntregado)
                                    + "\nDevuelto: " + String.format("%.2f \u20AC", cambio)
                                    + "\nFaltan: " + String.format("%.2f \u20AC", totalAcumulado)
                    );
                } else {
                    ControladorAlertas.mostrar("PAGO PARCIAL", "Cobro anotado. Faltan: " + String.format("%.2f", totalAcumulado) + " \u20AC");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Genera contenido visual o documentos a partir de los datos disponibles.
     * @param numMesa número de mesa.
     * @param carpeta carpeta donde se genera el documento.
     * @param tipo tipo de documento o cobro.
     */
    private void generarPDF(int numMesa, String carpeta, String tipo) {
        try {
            File folder = new File(carpeta);
            if (!folder.exists()) folder.mkdirs();

            String nombre = String.format("%s/Ticket_Mesa%d_%s.pdf", carpeta, numMesa, new java.text.SimpleDateFormat("MMddHHmm").format(new Date()));
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(nombre));

            doc.setMargins(20, 20, 20, 20);
            doc.open();

            com.lowagie.text.Font fBold = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font fNormal = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL);
            com.lowagie.text.Font fSmall = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9, com.lowagie.text.Font.NORMAL);

            Paragraph titulo = new Paragraph("TPV_ABG - TICKET\n", fBold);
            titulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(titulo);
            doc.add(new Paragraph("Mesa: " + numMesa + " | " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), fSmall));
            doc.add(new Paragraph("------------------------------------------------------------------"));

            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{4f, 1.2f}); // 80% para el nombre, 20% para el precio
            tabla.setSpacingBefore(5f);

            PdfPCell h1 = new PdfPCell(new Paragraph("ITEM", fBold));
            PdfPCell h2 = new PdfPCell(new Paragraph("PRECIO", fBold));
            h1.setBorder(0); h2.setBorder(0);
            h2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tabla.addCell(h1);
            tabla.addCell(h2);

            for (String linea : listaTicket.getItems()) {
                String[] partes = separarLineaTicket(linea);
                if (!partes[0].isBlank()) {
                    PdfPCell cellNombre = new PdfPCell(new Paragraph(partes[0], fNormal));
                    PdfPCell cellPrecio = new PdfPCell(new Paragraph(partes[1], fNormal));

                    cellNombre.setBorder(0);
                    cellPrecio.setBorder(0);
                    cellPrecio.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cellNombre.setPaddingBottom(5f);

                    tabla.addCell(cellNombre);
                    tabla.addCell(cellPrecio);
                }
            }
            doc.add(tabla);

            doc.add(new Paragraph("------------------------------------------------------------------"));

            double base = totalInicialMesa / 1.10;
            double iva = totalInicialMesa - base;

            PdfPTable tablaTotales = new PdfPTable(2);
            tablaTotales.setWidthPercentage(100);
            tablaTotales.setWidths(new float[]{4f, 1.2f});

            agregarFilaTotal(tablaTotales, "Subtotal (10%):", String.format("%.2f \u20AC", base), fNormal);
            agregarFilaTotal(tablaTotales, "IVA:", String.format("%.2f \u20AC", iva), fNormal);
            if (numMesa == 98 || numMesa == 99) {
                agregarFilaTotal(tablaTotales, "DESCUENTO CAMARERO:", "20%", fNormal);
            }
            if(tipo.equals("recibo"))
            {agregarFilaTotal(tablaTotales, "TOTAL PAGADO:", String.format("%.2f \u20AC", totalInicialMesa), fBold);}
            else
            {agregarFilaTotal(tablaTotales, "TOTAL A PAGAR:", String.format("%.2f \u20AC", totalInicialMesa), fBold);}

            doc.add(tablaTotales);
            doc.add(new Paragraph("------------------------------------------------------------------"));

            double efectivoMostrado = efectivoRecibidoAcumulado > 0 ? efectivoRecibidoAcumulado : pagadoEfectivoAcumulado;
            doc.add(new Paragraph("COBRADO EN EFECTIVO: " + String.format("%.2f", efectivoMostrado) + " \u20AC", fSmall));
            if (efectivoRecibidoAcumulado > 0) {
                doc.add(new Paragraph("DEVUELTO: " + String.format("%.2f", cambioDevueltoAcumulado) + " \u20AC", fSmall));
            }
            doc.add(new Paragraph("COBRADO EN TARJETA: " + String.format("%.2f", pagadoTarjetaAcumulado) + " \u20AC", fSmall));

            doc.add(new Paragraph("\n"));
            Paragraph g = new Paragraph("Gracias por su visita!", fNormal);
            g.setAlignment(Element.ALIGN_CENTER);
            doc.add(g);

            doc.close();
        }
        catch (Exception e)
        {e.printStackTrace();}
    }
    /**
     * Separa una línea de ticket en nombre de producto e importe.
     * @param linea línea de ticket que se procesa.
     * @return array con nombre e importe separados.
     */
    private String[] separarLineaTicket(String linea) {
        if (linea == null) {
            return new String[]{"", ""};
        }

        int separador = linea.lastIndexOf("....");
        if (separador < 0) {
            return new String[]{linea.trim(), ""};
        }

        return new String[]{
                linea.substring(0, separador).trim(),
                linea.substring(separador + 4).trim()
        };
    }

    /**
     * Agrega una fila de total a la tabla PDF del ticket.
     * @param tabla tabla PDF donde se añade la fila.
     * @param etiqueta etiqueta de la fila.
     * @param valor valor que se muestra o convierte.
     * @param fuente fuente usada para dibujar el texto.
     */
    private void agregarFilaTotal(PdfPTable tabla, String etiqueta, String valor, com.lowagie.text.Font fuente) {
        PdfPCell c1 = new PdfPCell(new Paragraph(etiqueta, fuente));
        PdfPCell c2 = new PdfPCell(new Paragraph(valor, fuente));
        c1.setBorder(0);
        c2.setBorder(0);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c1.setPaddingTop(2f);
        tabla.addCell(c1);
        tabla.addCell(c2);
    }
    /**
     * Atiende la acción disparada desde la interfaz de usuario.
     */
    @FXML
    private void handleSepararMesa() {
        try {
            if (lblMesaActiva.getText().contains("-")) {
                ControladorAlertas.mostrar("ERROR", "Selecciona primero la mesa origen.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/tpv_angela/vistas/VistaSepararMesa.fxml"));
            Parent root = loader.load();

            ControladorSepararMesa ctrl = loader.getController();

            int numMesa = Integer.parseInt(lblMesaActiva.getText().replace("MESA: ", ""));
            ctrl.setDatosMesa(numMesa, listaTicket.getItems(), totalAcumulado, ticketActualId);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            IconoApp.aplicar(stage);
            stage.showAndWait();

            cargarCuentaMesa(numMesa);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Atiende la acción disparada desde la interfaz de usuario.
     * @param actionEvent evento lanzado por la interfaz.
     */
    public void handleTraspasarMesa(ActionEvent actionEvent) {
        if (lblMesaActiva.getText().contains("-")) {
            ControladorAlertas.mostrar("ERROR", "Selecciona primero la mesa origen.");
            return;
        }

        if (listaTicket.getItems().isEmpty()) {
            ControladorAlertas.mostrar("ERROR", "La mesa origen no tiene productos para traspasar.");
            return;
        }

        if (buffer.isEmpty()) {
            ControladorAlertas.mostrar("ERROR", "Marca el número de la mesa destino y pulsa TRASLADAR.");
            return;
        }

        try {
            int mesaOrigen = Integer.parseInt(lblMesaActiva.getText().replace("MESA: ", ""));
            int mesaDestino = Integer.parseInt(buffer);

            if (mesaOrigen == mesaDestino) {
                ControladorAlertas.mostrar("ERROR", "La mesa destino no puede ser la misma que la mesa origen.");
                limpiarBuffer();
                return;
            }

            if (!menuDAO.existeMesa(mesaDestino)) {
                ControladorAlertas.mostrar("ERROR", "La mesa " + mesaDestino + " no existe.");
                limpiarBuffer();
                return;
            }

            if (menuDAO.mesaTieneProductos(mesaDestino) && !confirmarTraspasoMesaLlena(mesaDestino)) {
                limpiarBuffer();
                return;
            }

            menuDAO.traspasarMesa(mesaOrigen, mesaDestino, new ArrayList<>(listaTicket.getItems()));

            listaTicket.getItems().clear();
            lblMesaActiva.setText("MESA: " + mesaDestino);
            mesaActual = mesaDestino;
            cargarCuentaMesa(mesaDestino);
            mesaCerrada = false;
            acordeonMenu.setDisable(false);
            limpiarBuffer();

            ControladorAlertas.mostrar("TRASPASO REALIZADO", "La mesa " + mesaOrigen + " se ha traspasado a la mesa " + mesaDestino + ".");
        } catch (NumberFormatException e) {
            ControladorAlertas.mostrar("ERROR", "Introduce un número de mesa válido.");
            limpiarBuffer();
        }
    }

    /**
     * Método auxiliar usado por esta clase.
     * @param mesaDestino mesa a la que se trasladan productos.
     * @return resultado calculado por el método.
     */
    private boolean confirmarTraspasoMesaLlena(int mesaDestino) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Mesa ocupada");
        confirmacion.setHeaderText("La mesa " + mesaDestino + " ya tiene productos.");
        confirmacion.setContentText("Seguro que quieres traspasar la cuenta a esta mesa?");
        ButtonType si = new ButtonType("Si", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
        confirmacion.getButtonTypes().setAll(si, no);
        aplicarEstiloDialogo(confirmacion);
        return confirmacion.showAndWait().orElse(no) == si;
    }

    /**
     * Aplica el CSS comun de la app a los dialogos nativos.
     * @param alerta alerta a estilizar.
     */
    private void aplicarEstiloDialogo(Alert alerta) {
        alerta.getDialogPane().getStylesheets().add(
                getClass().getResource("/org/example/tpv_angela/estilo/style.css").toExternalForm()
        );
        IconoApp.aplicar(alerta);
    }

    /**
     * Limpia datos temporales o campos de la interfaz.
     */
    private void limpiarBuffer() {
        buffer = "";
        lblDisplay.setText("0");
    }
    /**
     * Atiende la acción disparada desde la interfaz de usuario.
     */
    @FXML
    private void handleImprimir() {
        if (lblMesaActiva.getText().contains("-")) return;
        int numMesa = Integer.parseInt(lblMesaActiva.getText().replace("MESA: ", ""));
        generarPDF(numMesa, "pdf/tiquets", "tiquets");
        menuDAO.actualizarEstadoMesa(numMesa, "precuenta");
        ControladorAlertas.mostrar("IMPRESION", "Precuenta impresa.");
    }
}
