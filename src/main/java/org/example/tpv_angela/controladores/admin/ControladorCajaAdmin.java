package org.example.tpv_angela.controladores.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import org.bson.Document;
import org.example.tpv_angela.ControladorAlertas;
import org.example.tpv_angela.DAO.DAOArqueoCaja;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Controlador de la vista de arqueo y movimientos de caja para el administrador.
 */
public class ControladorCajaAdmin {
    @FXML private Label lblInfo;
    @FXML private Label lblTotalTarjeta;
    @FXML private Label lblTotalEfectivo;
    @FXML private Label lblTotal;
    @FXML private Label lblCuentasDia;
    @FXML private TextField txtImporteRetirada;
    @FXML private TextField txtMotivoRetirada;
    @FXML private TableView<Document> tablaArqueos;
    @FXML private TableColumn<Document, String> colFechaArqueo;
    @FXML private TableColumn<Document, String> colVentasArqueo;
    @FXML private TableColumn<Document, String> colDeclaradoArqueo;
    @FXML private TableColumn<Document, String> colDescuadreArqueo;
    @FXML private TableView<Document> tablaMovimientos;
    @FXML private TableColumn<Document, String> colFechaMovimiento;
    @FXML private TableColumn<Document, String> colImporteMovimiento;
    @FXML private TableColumn<Document, String> colMotivoMovimiento;
    @FXML private TableView<Document> tablaCuentasDia;
    @FXML private TableColumn<Document, String> colCuentaFecha;
    @FXML private TableColumn<Document, String> colCuentaMesa;
    @FXML private TableColumn<Document, String> colCuentaMetodo;
    @FXML private TableColumn<Document, String> colCuentaTotal;

    private final DAOArqueoCaja daoArqueo = new DAOArqueoCaja();

    /**
     * Inicializa la vista, configura sus controles y carga los datos necesarios.
     */
    @FXML
    public void initialize() {
        configurarTablas();
        cargarDatos();
        tablaCuentasDia.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                mostrarDetalleCuentaDia(tablaCuentasDia.getSelectionModel().getSelectedItem());
            }
        });
    }

    /**
     * Registra la operación indicada en la capa de datos.
     */
    @FXML
    private void registrarRetirada() {
        try {
            double importe = parseImporte(txtImporteRetirada.getText());
            if (importe <= 0) {
                ControladorAlertas.mostrar("Dato incorrecto", "El importe debe ser mayor que cero.");
                return;
            }
            if (importe > daoArqueo.obtenerResumenHoy().efectivoEnCaja() + 0.01) {
                ControladorAlertas.mostrar("Caja insuficiente", "No hay tanto efectivo disponible en la caja de hoy.");
                return;
            }

            daoArqueo.registrarRetiradaAdmin(importe, txtMotivoRetirada.getText());
            txtImporteRetirada.clear();
            txtMotivoRetirada.clear();
            cargarDatos();
            ControladorAlertas.mostrar("Retirada guardada", "La retirada de caja se ha registrado correctamente.");
        } catch (NumberFormatException e) {
            ControladorAlertas.mostrar("Dato incorrecto", "Introduce un importe válido.");
        }
    }

    /**
     * Configura los controles y columnas necesarios para la vista.
     */
    private void configurarTablas() {
        colFechaArqueo.setCellValueFactory(data -> texto(formatearFecha(data.getValue().get("fecha"))));
        colVentasArqueo.setCellValueFactory(data -> texto(moneda(data.getValue().get("ventasTotales"))));
        colDeclaradoArqueo.setCellValueFactory(data -> texto(moneda(numero(data.getValue().get("efectivoDeclarado")) + numero(data.getValue().get("tarjetaDeclarada")))));
        colDescuadreArqueo.setCellValueFactory(data -> texto(moneda(numero(data.getValue().get("descuadreEfectivo")) + numero(data.getValue().get("descuadreTarjeta")))));

        colFechaMovimiento.setCellValueFactory(data -> texto(formatearFecha(data.getValue().get("fecha"))));
        colImporteMovimiento.setCellValueFactory(data -> texto(moneda(data.getValue().get("importe"))));
        colMotivoMovimiento.setCellValueFactory(data -> texto(String.valueOf(data.getValue().get("motivo", ""))));

        colCuentaFecha.setCellValueFactory(data -> texto(String.valueOf(data.getValue().get("nombre", ""))));
        colCuentaMesa.setCellValueFactory(data -> texto("Mesa " + valorTexto(data.getValue().get("mesa"))));
        colCuentaMetodo.setCellValueFactory(data -> texto(String.valueOf(data.getValue().get("metodoPago", ""))));
        colCuentaTotal.setCellValueFactory(data -> texto(moneda(data.getValue().get("total"))));
    }

    /**
     * Carga los datos necesarios y actualiza los controles de la pantalla.
     */
    private void cargarDatos() {
        DAOArqueoCaja.ResumenDia resumen = daoArqueo.obtenerResumenHoy();
        lblInfo.setText("Caja del día " + resumen.dia);
        lblTotalTarjeta.setText("Total tarjeta: " + moneda(resumen.ventasTarjeta));
        lblTotalEfectivo.setText("Total efectivo: " + moneda(resumen.efectivoEnCaja()));
        lblTotal.setText("Total: " + moneda(resumen.totalVentas));
        lblCuentasDia.setText("Cuentas de día " + resumen.dia);
        tablaArqueos.setItems(FXCollections.observableArrayList(daoArqueo.listarArqueos()));
        tablaMovimientos.setItems(FXCollections.observableArrayList(daoArqueo.listarMovimientosCaja()));
        tablaCuentasDia.setItems(FXCollections.observableArrayList(daoArqueo.listarCuentasHoy()));
    }

    /**
     * Devuelve una representación de texto segura para el valor recibido.
     * @param valor valor que se procesa.
     * @return propiedad de texto preparada para la tabla.
     */
    private SimpleStringProperty texto(String valor) {
        return new SimpleStringProperty(valor);
    }

    /**
     * Convierte el texto recibido al tipo numérico requerido.
     * @param texto texto que se procesa.
     * @return valor numérico calculado.
     */
    private double parseImporte(String texto) {
        if (texto == null || texto.isBlank()) {
            return 0.0;
        }
        return Double.parseDouble(texto.replace(",", ".").trim());
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
     * Formatea el importe recibido como texto de moneda.
     * @param valor valor que se procesa.
     * @return texto formateado o normalizado.
     */
    private String moneda(Object valor) {
        return moneda(numero(valor));
    }

    /**
     * Formatea el importe recibido como texto de moneda.
     * @param valor valor que se procesa.
     * @return texto formateado o normalizado.
     */
    private String moneda(double valor) {
        return String.format("%.2f \u20AC", valor);
    }

    /**
     * Convierte el valor recibido en un texto preparado para mostrarse en pantalla.
     * @param valor valor que se procesa.
     * @return texto formateado o normalizado.
     */
    private String formatearFecha(Object valor)
    {
        if (valor instanceof Date fecha)
        {
            SimpleDateFormat sdf =
                    new SimpleDateFormat("dd/MM/yyyy HH:mm");
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
            return sdf.format(fecha);
        }
        return "";
    }


    @SuppressWarnings("unchecked")
    /**
     * Muestra en pantalla la información indicada.
     * @param cuentaDia cuenta del día seleccionada.
     */
    private void mostrarDetalleCuentaDia(Document cuentaDia) {
        if (cuentaDia == null) {
            return;
        }

        StringBuilder detalle = new StringBuilder();
        detalle.append("Mesa: ").append(valorTexto(cuentaDia.get("mesa"))).append("\n");
        detalle.append("Tipo de cobro:\n");
        detalle.append("  EFECTIVO: ").append(moneda(cuentaDia.get("ventasEfectivo"))).append("\n");
        detalle.append("  TARJETA: ").append(moneda(cuentaDia.get("ventasTarjeta"))).append("\n");
        detalle.append("Total ticket: ").append(moneda(cuentaDia.get("total"))).append("\n\n");
        detalle.append("Pagos realizados:\n");

        Object pagos = cuentaDia.get("pagos");
        if (pagos instanceof List<?> listaPagos && !listaPagos.isEmpty()) {
            for (Object pago : listaPagos) {
                if (pago instanceof Document pagoDoc) {
                    detalle.append("  ")
                            .append(formatearFecha(pagoDoc.get("fecha")))
                            .append(" - ")
                            .append(pagoDoc.get("metodoPago", ""))
                            .append(" - ")
                            .append(moneda(pagoDoc.get("importe")))
                            .append("\n");
                }
            }
        } else {
            detalle.append("  Sin pagos detallados.\n");
        }

        mostrarMiniVentanaTicket(String.valueOf(cuentaDia.get("nombre", "Cuentas de día")), detalle.toString());
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
     * Muestra en pantalla la información indicada.
     * @param titulo título que se muestra al usuario.
     * @param mensaje mensaje que se muestra al usuario.
     */
    private void mostrarMiniVentanaTicket(String titulo, String mensaje) {
        if (tablaCuentasDia.getScene() == null || tablaCuentasDia.getScene().getWindow() == null) {
            ControladorAlertas.mostrar(titulo, mensaje);
            return;
        }

        Popup popup = new Popup();
        popup.setAutoHide(true);

        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("ticket-popup-title");

        Label lblMensaje = new Label(mensaje);
        lblMensaje.setWrapText(true);
        lblMensaje.getStyleClass().add("ticket-popup-message");

        ScrollPane scroll = new ScrollPane(lblMensaje);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(310);
        scroll.getStyleClass().add("ticket-popup-scroll");

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setPrefHeight(44);
        btnCerrar.setMaxWidth(Double.MAX_VALUE);
        btnCerrar.getStyleClass().add("ticket-popup-close");
        btnCerrar.setOnAction(event -> popup.hide());

        VBox contenido = new VBox(12, lblTitulo, scroll, btnCerrar);
        contenido.setAlignment(Pos.CENTER_LEFT);
        contenido.setPadding(new Insets(18));
        contenido.setPrefWidth(560);
        contenido.getStyleClass().add("ticket-popup-content");
        contenido.getStylesheets().add(getClass().getResource("/org/example/tpv_angela/estilo/style.css").toExternalForm());

        popup.getContent().add(contenido);
        Window ventana = tablaCuentasDia.getScene().getWindow();
        popup.show(ventana);
        popup.setX(ventana.getX() + (ventana.getWidth() - 560) / 2);
        popup.setY(ventana.getY() + 150);
    }
}
