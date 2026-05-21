package org.example.tpv_angela.controladores.camarero;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.example.tpv_angela.ControladorAlertas;
import org.example.tpv_angela.DAO.DAOMenu;
import org.example.tpv_angela.DAO.DAOVentas;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para cobrar productos concretos de una mesa.
 */
public class ControladorSepararMesa {

    @FXML private ListView<String> listaItems;
    @FXML private TextField txtEfectivoRecibido;

    private int numMesa;
    private String ticketId;
    private ObservableList<String> itemsMesa;
    private final DAOVentas ventasDAO = new DAOVentas();
    private final DAOMenu menuDAO = new DAOMenu();

    /**
     * Inicializa la lista de productos cobrables y su selección táctil múltiple.
     */
    @FXML
    public void initialize() {
        if (listaItems == null) {
            return;
        }

        listaItems.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listaItems.setCellFactory(lista -> {
            ListCell<String> celda = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };

            celda.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (celda.isEmpty()) {
                    return;
                }

                int indice = celda.getIndex();
                if (listaItems.getSelectionModel().isSelected(indice)) {
                    listaItems.getSelectionModel().clearSelection(indice);
                } else {
                    listaItems.getSelectionModel().select(indice);
                }
                event.consume();
            });

            return celda;
        });
    }

    /**
     * Carga la mesa y sus productos en la ventana de cobro parcial.
     * @param numMesa número de mesa activa.
     * @param items productos actuales de la mesa.
     * @param total total actual, mantenido por compatibilidad con llamadas existentes.
     */
    public void setDatosMesa(int numMesa, ObservableList<String> items, double total) {
        setDatosMesa(numMesa, items, total, null);
    }

    /**
     * Carga la mesa y sus productos en la ventana de cobro parcial.
     * @param numMesa número de mesa activa.
     * @param items productos actuales de la mesa.
     * @param total total actual, mantenido por compatibilidad con llamadas existentes.
     * @param ticketId identificador de la cuenta abierta.
     */
    public void setDatosMesa(int numMesa, ObservableList<String> items, double total, String ticketId) {
        this.numMesa = numMesa;
        this.ticketId = ticketId;
        this.itemsMesa = FXCollections.observableArrayList(items);

        if (listaItems != null) {
            listaItems.setItems(itemsMesa);
        }
    }

    @FXML
    private void handlePagoEfectivo() {
        cobrarSeleccionados("EFECTIVO");
    }

    @FXML
    private void handlePagoTarjeta() {
        cobrarSeleccionados("TARJETA");
    }

    private void cobrarSeleccionados(String metodo) {
        List<String> seleccionados = listaItems.getSelectionModel().getSelectedItems();
        if (seleccionados == null || seleccionados.isEmpty()) {
            ControladorAlertas.mostrar("Sin selección", "Selecciona al menos un producto para cobrar.");
            return;
        }

        ArrayList<String> cobrados = new ArrayList<>(seleccionados);
        double totalSeleccionado = 0.0;
        for (String linea : cobrados) {
            totalSeleccionado += extraerPrecioLineaTicket(linea);
        }
        double recibido = totalSeleccionado;
        double devuelto = 0.0;
        if (metodo.equals("EFECTIVO")) {
            recibido = obtenerEfectivoRecibido(totalSeleccionado);
            if (recibido < 0) {
                return;
            }
            devuelto = recibido - totalSeleccionado;
        }

        ventasDAO.registrarVenta(numMesa, cobrados, totalSeleccionado, metodo, ticketId);
        for (String item : cobrados) {
            menuDAO.eliminarProductoDeMesa(numMesa, item);
        }

        if (metodo.equals("EFECTIVO")) {
            ControladorAlertas.mostrar(
                    "Cobro registrado",
                    "Cobrado en efectivo: " + String.format("%.2f \u20AC", recibido)
                            + "\nDevuelto: " + String.format("%.2f \u20AC", devuelto)
            );
        } else {
            ControladorAlertas.mostrar("Cobro registrado", "Tarjeta: " + String.format("%.2f \u20AC", totalSeleccionado));
        }
        cerrarVentana();
    }

    private double obtenerEfectivoRecibido(double totalSeleccionado) {
        String texto = txtEfectivoRecibido == null ? "" : txtEfectivoRecibido.getText();
        if (texto == null || texto.isBlank()) {
            return totalSeleccionado;
        }

        try {
            double recibido = Double.parseDouble(texto.replace(",", ".").trim());
            if (recibido + 0.001 < totalSeleccionado) {
                ControladorAlertas.mostrar("Efectivo insuficiente", "Faltan " + String.format("%.2f \u20AC", totalSeleccionado - recibido));
                return -1;
            }
            return recibido;
        } catch (NumberFormatException e) {
            ControladorAlertas.mostrar("Efectivo no válido", "Introduce una cantidad válida.");
            return -1;
        }
    }

    private double extraerPrecioLineaTicket(String linea) {
        String precio = separarLineaTicket(linea)[1]
                .replace("EUR", "")
                .replace("\u20AC", "")
                .replaceAll("[^0-9,.-]", "")
                .replace(",", ".")
                .trim();

        if (precio.isBlank()) {
            return 0.0;
        }

        try {
            return Double.parseDouble(precio);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

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

    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) listaItems.getScene().getWindow();
        stage.close();
    }
}
