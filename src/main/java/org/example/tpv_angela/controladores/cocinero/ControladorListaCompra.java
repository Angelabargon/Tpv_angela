package org.example.tpv_angela.controladores.cocinero;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.bson.Document;
import org.example.tpv_angela.DAO.DAOMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de la lista de compra creada y enviada por cocina.
 */
public class ControladorListaCompra {
    @FXML private TextField txtIngrediente;
    @FXML private TextField txtCantidad;
    @FXML private ListView<String> listaBorrador;
    @FXML private ListView<String> listaEnviada;
    @FXML private ListView<String> listaComprada;
    @FXML private Label lblEstado;

    private final DAOMenu daoMenu = new DAOMenu();
    private final List<Document> itemsBorrador = new ArrayList<>();

    /**
     * Inicializa la vista, configura sus controles y carga los datos necesarios.
     */
    @FXML
    public void initialize() {
        configurarListaBorrador();
        refrescar();
    }

    /**
     * Agrega el elemento indicado a la vista o a la base de datos.
     */
    @FXML
    private void agregarIngrediente() {
        String ingrediente = txtIngrediente.getText().trim();
        String cantidad = txtCantidad.getText().trim();
        if (ingrediente.isEmpty() || cantidad.isEmpty()) {
            lblEstado.setText("Escribe ingrediente y cantidad.");
            return;
        }
        daoMenu.agregarItemListaCompra(ingrediente, cantidad);
        txtIngrediente.clear();
        txtCantidad.clear();
        refrescar();
        lblEstado.setText("Ingrediente añadido al borrador.");
    }

    /**
     * Ejecuta la operación asociada a este controlador o servicio.
     */
    @FXML
    private void enviarLista() {
        daoMenu.enviarListaCompraPendiente();
        refrescar();
        lblEstado.setText("Lista enviada al administrador.");
    }

    /**
     * Actualiza la información mostrada consultando los datos más recientes.
     */
    @FXML
    private void refrescar() {
        itemsBorrador.clear();
        itemsBorrador.addAll(daoMenu.obtenerListaCompra("borrador"));
        listaBorrador.getItems().setAll(formatear(itemsBorrador));
        listaEnviada.getItems().setAll(formatear(daoMenu.obtenerListaCompra("enviado")));
        listaComprada.getItems().setAll(formatearComprados(daoMenu.obtenerListaCompra("comprado")));
        lblEstado.setText("Lista de compra sincronizada.");
    }

    /**
     * Configura las filas del borrador con accion de eliminar.
     */
    private void configurarListaBorrador() {
        listaBorrador.setCellFactory(listView -> new ListCell<>() {
            private final Label texto = new Label();
            private final Button btnEliminar = new Button("Eliminar");
            private final Region espacio = new Region();
            private final HBox fila = new HBox(10, texto, espacio, btnEliminar);

            {
                texto.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(espacio, Priority.ALWAYS);
                btnEliminar.getStyleClass().add("ticket-delete-button");
                btnEliminar.setFocusTraversable(false);
                btnEliminar.setOnAction(event -> eliminarBorrador(getIndex()));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                texto.setText(item);
                setText(null);
                setGraphic(fila);
            }
        });
    }

    /**
     * Elimina una linea del borrador.
     * @param indice posicion visual seleccionada.
     */
    private void eliminarBorrador(int indice) {
        if (indice < 0 || indice >= itemsBorrador.size()) {
            lblEstado.setText("Selecciona una linea del borrador.");
            return;
        }
        daoMenu.eliminarItemListaCompra(itemsBorrador.get(indice));
        refrescar();
        lblEstado.setText("Linea eliminada del borrador.");
    }

    /**
     * Convierte el valor recibido en un texto preparado para mostrarse en pantalla.
     * @param docs documento con los datos a procesar.
     * @return lista de datos solicitada.
     */
    private List<String> formatear(List<Document> docs) {
        return docs.stream()
                .map(doc -> doc.getString("ingrediente") + " | " + doc.getString("cantidad"))
                .toList();
    }

    /**
     * Convierte el valor recibido en un texto preparado para mostrarse en pantalla.
     * @param docs documento con los datos a procesar.
     * @return lista de datos solicitada.
     */
    private List<String> formatearComprados(List<Document> docs) {
        return docs.stream()
                .map(doc -> {
                    boolean auto = doc.getBoolean("compraAutomatica", false);
                    return doc.getString("ingrediente") + " | " + doc.getString("cantidad") + " | " + (auto ? "Comprado automático" : "Comprado");
                })
                .toList();
    }
}
