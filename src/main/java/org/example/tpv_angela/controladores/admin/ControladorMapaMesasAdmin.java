package org.example.tpv_angela.controladores.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.bson.Document;
import org.example.tpv_angela.DAO.DAOMenu;
import org.example.tpv_angela.controladores.camarero.ControladorMapaMesas;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de la gestion administrativa del mapa y alta de mesas.
 */
public class ControladorMapaMesasAdmin {
    @FXML private AnchorPane mapaHost;
    @FXML private ListView<String> listaMesas;
    @FXML private TextField txtNumeroMesa;
    @FXML private TextField txtCapacidad;
    @FXML private Label lblTituloFormulario;
    @FXML private Button btnGuardarMesa;
    @FXML private Button btnCancelarEdicion;
    @FXML private Label lblEstado;

    private final DAOMenu daoMenu = new DAOMenu();
    private final List<Document> mesas = new ArrayList<>();
    private Document mesaSeleccionada;
    private int numeroMesaOriginal = -1;

    /**
     * Inicializa la vista, configura sus controles y carga los datos necesarios.
     */
    @FXML
    public void initialize() {
        limpiarFormulario();
        refrescar();
    }

    /**
     * Agrega una mesa nueva o guarda los cambios de la mesa seleccionada.
     */
    @FXML
    private void agregarMesa() {
        try {
            int numero = Integer.parseInt(txtNumeroMesa.getText().trim());
            int capacidad = Integer.parseInt(txtCapacidad.getText().trim());
            String mensaje;

            if (mesaSeleccionada == null) {
                if (daoMenu.existeMesa(numero)) {
                    lblEstado.setText("La mesa " + numero + " ya existe.");
                    return;
                }
                daoMenu.agregarMesa(numero, capacidad);
                mensaje = "Mesa añadida correctamente.";
            } else {
                if (numero != numeroMesaOriginal && daoMenu.existeMesa(numero)) {
                    lblEstado.setText("La mesa " + numero + " ya existe.");
                    return;
                }
                daoMenu.actualizarMesa(numeroMesaOriginal, numero, capacidad);
                mensaje = "Mesa modificada correctamente.";
            }

            limpiarFormulario();
            refrescar();
            lblEstado.setText(mensaje);
        } catch (Exception e) {
            lblEstado.setText("Introduce número y capacidad válidos.");
        }
    }

    /**
     * Carga la mesa seleccionada en el formulario para modificarla.
     */
    @FXML
    private void modificarMesa() {
        int indice = listaMesas.getSelectionModel().getSelectedIndex();
        if (indice < 0 || indice >= mesas.size()) {
            lblEstado.setText("Selecciona una mesa para modificar.");
            return;
        }

        mesaSeleccionada = mesas.get(indice);
        numeroMesaOriginal = obtenerNumeroMesa(mesaSeleccionada);
        txtNumeroMesa.setText(String.valueOf(numeroMesaOriginal));
        txtCapacidad.setText(String.valueOf(mesaSeleccionada.get("capacidad", "")));
        lblTituloFormulario.setText("Modificar mesa");
        btnGuardarMesa.setText("Guardar cambios");
        btnCancelarEdicion.setVisible(true);
        btnCancelarEdicion.setManaged(true);
        lblEstado.setText("Editando: " + formatearMesa(mesaSeleccionada));
    }

    /**
     * Cancela la edición activa y limpia el formulario.
     */
    @FXML
    private void cancelarEdicion() {
        limpiarFormulario();
        lblEstado.setText("Edición cancelada.");
    }

    /**
     * Elimina el elemento seleccionado tras aplicar las validaciones necesarias.
     */
    @FXML
    private void eliminarMesa() {
        int indice = listaMesas.getSelectionModel().getSelectedIndex();
        if (indice < 0 || indice >= mesas.size()) {
            lblEstado.setText("Selecciona una mesa para eliminar.");
            return;
        }
        int numero = obtenerNumeroMesa(mesas.get(indice));
        daoMenu.eliminarMesa(numero);
        limpiarFormulario();
        refrescar();
        lblEstado.setText("Mesa " + numero + " eliminada.");
    }

    /**
     * Actualiza la información mostrada consultando los datos más recientes.
     */
    @FXML
    private void refrescar() {
        mesas.clear();
        mesas.addAll(daoMenu.obtenerMesas());
        listaMesas.getItems().setAll(mesas.stream().map(this::formatearMesa).toList());
        cargarMapaCompartido();
        lblEstado.setText("Mesas disponibles: " + mesas.size());
    }

    /**
     * Limpia los campos del formulario y desactiva el modo edición.
     */
    private void limpiarFormulario() {
        txtNumeroMesa.clear();
        txtCapacidad.clear();
        mesaSeleccionada = null;
        numeroMesaOriginal = -1;
        lblTituloFormulario.setText("Alta de mesa");
        btnGuardarMesa.setText("Añadir mesa");
        btnCancelarEdicion.setVisible(false);
        btnCancelarEdicion.setManaged(false);
    }

    /**
     * Convierte el valor recibido en un texto preparado para mostrarse en pantalla.
     * @param mesa número o documento de la mesa implicada.
     * @return texto formateado o normalizado.
     */
    private String formatearMesa(Document mesa) {
        return "Mesa " + obtenerNumeroMesa(mesa) + " | Capacidad " + mesa.get("capacidad") + " | Estado " + mesa.getString("estado");
    }

    /**
     * Obtiene el dato solicitado por la vista o por la lógica de negocio.
     * @param mesa número o documento de la mesa implicada.
     * @return valor numérico calculado.
     */
    private int obtenerNumeroMesa(Document mesa) {
        Object numero = mesa.get("numero");
        return numero instanceof Number ? ((Number) numero).intValue() : Integer.parseInt(String.valueOf(numero));
    }

    /**
     * Carga los datos necesarios y actualiza los controles de la pantalla.
     */
    private void cargarMapaCompartido() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/tpv_angela/vistas/VistaMapaMesas.fxml"));
            Parent mapa = loader.load();
            ControladorMapaMesas controladorMapa = loader.getController();
            controladorMapa.activarModoEdicionAdmin(this::refrescarLista);
            mapaHost.getChildren().setAll(mapa);
            AnchorPane.setTopAnchor(mapa, 0.0);
            AnchorPane.setBottomAnchor(mapa, 0.0);
            AnchorPane.setLeftAnchor(mapa, 0.0);
            AnchorPane.setRightAnchor(mapa, 0.0);
        } catch (Exception e) {
            lblEstado.setText("No se pudo cargar el mapa compartido.");
        }
    }

    /**
     * Actualiza la información mostrada consultando los datos más recientes.
     */
    private void refrescarLista() {
        mesas.clear();
        mesas.addAll(daoMenu.obtenerMesas());
        listaMesas.getItems().setAll(mesas.stream().map(this::formatearMesa).toList());
        lblEstado.setText("Posición de mesa guardada.");
    }
}
