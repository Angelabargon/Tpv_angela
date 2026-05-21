package org.example.tpv_angela.controladores.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.bson.Document;
import org.example.tpv_angela.DAO.DAOMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de la lista de la compra recibida por el administrador.
 */
public class ControladorListaCompraAdmin {
    @FXML private ListView<String> listaCompra;
    @FXML private ListView<String> listaComprada;
    @FXML private Label lblEstado;

    private final DAOMenu daoMenu = new DAOMenu();
    private final List<Document> items = new ArrayList<>();
    private final List<Document> itemsComprados = new ArrayList<>();

    /**
     * Inicializa la vista, configura sus controles y carga los datos necesarios.
     */
    @FXML
    public void initialize() {
        refrescar();
    }

    /**
     * Ejecuta la operación asociada a este controlador o servicio.
     */
    @FXML
    private void comprarSeleccionado() {
        int indice = listaCompra.getSelectionModel().getSelectedIndex();
        if (indice < 0 || indice >= items.size()) {
            lblEstado.setText("Selecciona un elemento enviado.");
            return;
        }
        daoMenu.comprarItemListaCompra(items.get(indice));
        refrescar();
        lblEstado.setText("Elemento comprado por administración.");
    }

    /**
     * Actualiza la información mostrada consultando los datos más recientes.
     */
    @FXML
    private void refrescar() {
        items.clear();
        items.addAll(daoMenu.obtenerListaCompra("enviado"));
        itemsComprados.clear();
        itemsComprados.addAll(daoMenu.obtenerListaCompra("comprado"));
        listaCompra.getItems().setAll(items.stream().map(this::formatear).toList());
        listaComprada.getItems().setAll(itemsComprados.stream().map(this::formatearComprado).toList());
        lblEstado.setText("Pendientes: " + items.size() + " | Comprados: " + itemsComprados.size());
    }

    /**
     * Convierte el valor recibido en un texto preparado para mostrarse en pantalla.
     * @param item elemento seleccionado o procesado.
     * @return texto formateado o normalizado.
     */
    private String formatear(Document item) {
        return item.getString("ingrediente") + " | " + item.getString("cantidad") + " | Enviado";
    }

    /**
     * Convierte el valor recibido en un texto preparado para mostrarse en pantalla.
     * @param item elemento seleccionado o procesado.
     * @return texto formateado o normalizado.
     */
    private String formatearComprado(Document item) {
        boolean auto = item.getBoolean("compraAutomatica", false);
        return item.getString("ingrediente") + " | " + item.getString("cantidad") + " | " + (auto ? "Comprado automático" : "Comprado");
    }
}
