package org.example.tpv_angela.controladores;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import org.bson.Document;
import org.example.tpv_angela.DAO.DAOMenu;
import org.example.tpv_angela.controladores.MenuDesplegableCarta;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador de la pantalla de consulta de la carta y detalle de platos.
 */
public class ControladorMenuCarta {
    @FXML private Accordion acordeonMenu;
    @FXML private Label lblNombrePlato;
    @FXML private Label lblCategoriaPlato;
    @FXML private Label lblAlergenos;

    private final DAOMenu daoMenu = new DAOMenu();

    /**
     * Inicializa la vista, configura sus controles y carga los datos necesarios.
     */
    @FXML
    public void initialize() {
        MenuDesplegableCarta.generarDesdeDocumentosGrande(acordeonMenu, daoMenu.obtenerMenuFijoDocumentos(), this::mostrarAlergenos, getClass());
        limpiarPanelAlergenos();
    }

    /**
     * Muestra en pantalla la información indicada.
     * @param plato documento del plato seleccionado.
     */
    private void mostrarAlergenos(Document plato) {
        lblNombrePlato.setText(valor(plato.getString("nombre"), "Plato"));
        lblCategoriaPlato.setText(valor(plato.getString("categoria"), "Sin categoría") + " / " + valor(plato.getString("subcategoria"), "General"));
        lblAlergenos.setText(formatearAlergenos(plato));
    }

    /**
     * Limpia los campos del formulario para dejar la vista preparada.
     */
    private void limpiarPanelAlergenos() {
        lblNombrePlato.setText("Selecciona un plato");
        lblCategoriaPlato.setText("Menú fijo");
        lblAlergenos.setText("Sin plato seleccionado.");
    }

    /**
     * Convierte el valor recibido en un texto preparado para mostrarse en pantalla.
     * @param plato documento del plato seleccionado.
     * @return texto formateado o normalizado.
     */
    private String formatearAlergenos(Document plato) {
        Object valor = plato.get("alergenos");
        if (valor instanceof List<?> lista && !lista.isEmpty()) {
            return lista.stream()
                    .map(alergeno -> "- " + String.valueOf(alergeno))
                    .collect(Collectors.joining("\n"));
        }
        return "Sin alergenos registrados.";
    }

    /**
     * Devuelve una representación de texto segura para el valor recibido.
     * @param texto texto que se procesa.
     * @param fallback valor usado cuando el texto está vacío.
     * @return texto formateado o normalizado.
     */
    private String valor(String texto, String fallback) {
        return texto == null || texto.isBlank() ? fallback : texto;
    }
}
