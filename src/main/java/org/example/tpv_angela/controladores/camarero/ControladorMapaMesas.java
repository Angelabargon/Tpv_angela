package org.example.tpv_angela.controladores.camarero;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import org.bson.Document;
import org.example.tpv_angela.ControladorAlertas;
import org.example.tpv_angela.DAO.DAOArqueoCaja;
import org.example.tpv_angela.DAO.DAOMenu;
import org.example.tpv_angela.Navegacion;

import java.util.Comparator;
import java.util.List;

/**
 * Controlador del mapa visual de mesas y de sus estados de ocupacion.
 */
public class ControladorMapaMesas {
    @FXML private AnchorPane contenedorMesas;
    @FXML private Label lblResumen;

    private final DAOMenu menuDAO = new DAOMenu();
    private final DAOArqueoCaja daoArqueo = new DAOArqueoCaja();
    private boolean modoEdicionAdmin;
    private Runnable alGuardarPosicion;
    private double offsetArrastreX;
    private double offsetArrastreY;

    /**
     * Inicializa la vista, configura sus controles y carga los datos necesarios.
     */
    @FXML
    public void initialize() {
        cargarMesas();
    }

    /**
     * Activa el modo o comportamiento solicitado en la vista.
     * @param alGuardarPosicion acción ejecutada al guardar la posición de una mesa.
     */
    public void activarModoEdicionAdmin(Runnable alGuardarPosicion) {
        this.modoEdicionAdmin = true;
        this.alGuardarPosicion = alGuardarPosicion;
        cargarMesas();
    }

    /**
     * Carga los datos necesarios y actualiza los controles de la pantalla.
     */
    private void cargarMesas() {
        contenedorMesas.getChildren().removeIf(nodo -> nodo.getStyleClass().contains("mesa-slot"));
        List<Document> mesas = menuDAO.obtenerMesas();
        mesas.sort(Comparator.comparingInt(this::obtenerNumeroMesa));

        int libres = 0;
        int ocupadas = 0;
        int precuenta = 0;

        for (Document mesa : mesas) {
            int numero = obtenerNumeroMesa(mesa);
            String estadoVisual = obtenerEstadoVisual(mesa);

            if (estadoVisual.equals("precuenta")) {
                precuenta++;
            } else if (estadoVisual.equals("ocupada")) {
                ocupadas++;
            } else {
                libres++;
            }

            AnchorPane vistaMesa = crearMesa(numero, estadoVisual, mesa);
            double[] posicion = obtenerPosicionMesa(numero, mesa);
            AnchorPane.setLeftAnchor(vistaMesa, posicion[0]);
            AnchorPane.setTopAnchor(vistaMesa, posicion[1]);
            contenedorMesas.getChildren().add(vistaMesa);
        }

        lblResumen.setText("Libres: " + libres + "   Ocupadas: " + ocupadas + "   Precuenta: " + precuenta);
    }

    /**
     * Construye y devuelve el componente visual solicitado.
     * @param numero número identificador.
     * @param estadoVisual estado visual mostrado para la mesa.
     * @param mesa número o documento de la mesa implicada.
     * @return componente visual construido.
     */
    private AnchorPane crearMesa(int numero, String estadoVisual, Document mesa) {
        AnchorPane tarjeta = new AnchorPane();
        tarjeta.getStyleClass().add("mesa-slot");

        Button boton = new Button(String.valueOf(numero));
        boton.getStyleClass().addAll("mesa-boton", "mesa-" + estadoVisual);
        if (esMesaRedonda(numero)) {
            boton.getStyleClass().add("mesa-redonda");
        }
        if (esMesaCamarero(numero)) {
            boton.getStyleClass().add("mesa-camarero");
        }
        if (esMesaBarra(numero)) {
            boton.getStyleClass().add("mesa-barra");
        }
        boton.setOnAction(event -> abrirVentasConMesa(event, numero));
        if (modoEdicionAdmin) {
            boton.setOnAction(null);
            boton.setCursor(Cursor.MOVE);
            configurarArrastreAdmin(tarjeta, boton, numero);
        }
        AnchorPane.setLeftAnchor(boton, esMesaBarra(numero) ? 34.0 : 30.0);
        AnchorPane.setTopAnchor(boton, esMesaBarra(numero) ? 32.0 : 30.0);

        if (!esMesaBarra(numero) && !esMesaCamarero(numero)) {
            agregarSillas(tarjeta, obtenerCapacidad(mesa));
        }

        tarjeta.getChildren().add(boton);
        if (modoEdicionAdmin) {
            tarjeta.setCursor(Cursor.MOVE);
            configurarArrastreAdmin(tarjeta, tarjeta, numero);
        }
        return tarjeta;
    }

    /**
     * Configura los controles y columnas necesarios para la vista.
     * @param tarjeta importe de tarjeta o tarjeta visual, según el contexto.
     * @param zonaArrastre zona que activa el arrastre de la mesa.
     * @param numeroMesa número o documento de la mesa implicada.
     */
    private void configurarArrastreAdmin(AnchorPane tarjeta, Region zonaArrastre, int numeroMesa) {
        zonaArrastre.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            offsetArrastreX = event.getSceneX() - AnchorPane.getLeftAnchor(tarjeta);
            offsetArrastreY = event.getSceneY() - AnchorPane.getTopAnchor(tarjeta);
            event.consume();
        });

        zonaArrastre.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            double nuevaX = limitar(event.getSceneX() - offsetArrastreX, 0, contenedorMesas.getWidth() - tarjeta.getWidth());
            double nuevaY = limitar(event.getSceneY() - offsetArrastreY, 0, contenedorMesas.getHeight() - tarjeta.getHeight());
            AnchorPane.setLeftAnchor(tarjeta, nuevaX);
            AnchorPane.setTopAnchor(tarjeta, nuevaY);
            event.consume();
        });

        zonaArrastre.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            menuDAO.actualizarPosicionMesa(
                    numeroMesa,
                    AnchorPane.getLeftAnchor(tarjeta),
                    AnchorPane.getTopAnchor(tarjeta)
            );
            if (alGuardarPosicion != null) {
                alGuardarPosicion.run();
            }
            event.consume();
        });
    }

    /**
     * Agrega el elemento indicado a la vista o a la base de datos.
     * @param tarjeta importe de tarjeta o tarjeta visual, según el contexto.
     * @param capacidad capacidad de comensales de la mesa.
     */
    private void agregarSillas(AnchorPane tarjeta, int capacidad) {
        double[][] posiciones = capacidad <= 2
                ? new double[][]{{42, 8}, {42, 86}}
                : new double[][]{{42, 8}, {42, 86}, {8, 42}, {86, 42}, {18, 18}, {78, 78}};

        int total = Math.min(Math.max(capacidad, 2), posiciones.length);
        for (int i = 0; i < total; i++) {
            Region silla = new Region();
            silla.getStyleClass().add("silla-mesa");
            AnchorPane.setLeftAnchor(silla, posiciones[i][0]);
            AnchorPane.setTopAnchor(silla, posiciones[i][1]);
            tarjeta.getChildren().add(silla);
        }
    }

    /**
     * Obtiene el dato solicitado por la vista o por la lógica de negocio.
     * @param numero número identificador.
     * @return dato solicitado.
     */
    private double[] obtenerPosicionMesa(int numero) {
        return switch (numero) {
            case 98 -> new double[]{245, 34};
            case 99 -> new double[]{325, 34};
            case 13 -> new double[]{188, 158};
            case 14 -> new double[]{290, 158};
            case 15 -> new double[]{392, 158};
            case 1 -> new double[]{118, 280};
            case 2 -> new double[]{288, 280};
            case 3 -> new double[]{458, 280};
            case 4 -> new double[]{628, 280};
            case 5 -> new double[]{118, 450};
            case 6 -> new double[]{288, 450};
            case 7 -> new double[]{458, 450};
            case 8 -> new double[]{628, 450};
            case 9 -> new double[]{118, 620};
            case 10 -> new double[]{288, 620};
            case 11 -> new double[]{458, 620};
            case 12 -> new double[]{628, 620};
            case 16 -> new double[]{1088, 178};
            case 17 -> new double[]{1248, 178};
            case 18 -> new double[]{1088, 372};
            case 19 -> new double[]{1248, 372};
            case 20 -> new double[]{1168, 566};
            default -> {
                int baseNumero = numero > 20 ? numero - 21 : numero + 200;
                int indice = Math.max(0, baseNumero);
                int columna = indice % 3;
                int fila = indice / 3;
                yield new double[]{1088 + columna * 160.0, 760 + fila * 150.0};
            }
        };
    }

    /**
     * Obtiene el dato solicitado por la vista o por la lógica de negocio.
     * @param numero número identificador.
     * @param mesa número o documento de la mesa implicada.
     * @return dato solicitado.
     */
    private double[] obtenerPosicionMesa(int numero, Document mesa) {
        Object posicionX = mesa.get("posicionX");
        Object posicionY = mesa.get("posicionY");
        if (posicionX instanceof Number x && posicionY instanceof Number y) {
            return new double[]{x.doubleValue(), y.doubleValue()};
        }
        return obtenerPosicionMesa(numero);
    }

    /**
     * Limita el valor recibido dentro del rango indicado.
     * @param valor valor que se procesa.
     * @param minimo valor mínimo permitido.
     * @param maximo valor máximo permitido.
     * @return valor numérico calculado.
     */
    private double limitar(double valor, double minimo, double maximo) {
        return Math.max(minimo, Math.min(valor, Math.max(minimo, maximo)));
    }

    /**
     * Indica si la mesa recibida cumple el tipo visual comprobado.
     * @param numero número identificador.
     * @return true si la condición se cumple; false en caso contrario.
     */
    private boolean esMesaRedonda(int numero) {
        return numero == 9 || numero == 10 || numero == 11 || esMesaBarra(numero) || esMesaCamarero(numero);
    }

    /**
     * Indica si la mesa recibida cumple el tipo visual comprobado.
     * @param numero número identificador.
     * @return true si la condición se cumple; false en caso contrario.
     */
    private boolean esMesaBarra(int numero) {
        return numero >= 13 && numero <= 20;
    }

    /**
     * Indica si la mesa recibida cumple el tipo visual comprobado.
     * @param numero número identificador.
     * @return true si la condición se cumple; false en caso contrario.
     */
    private boolean esMesaCamarero(int numero) {
        return numero == 98 || numero == 99;
    }

    /**
     * Abre la pantalla correspondiente con los datos indicados.
     * @param event evento que dispara la acción.
     * @param numeroMesa número o documento de la mesa implicada.
     */
    private void abrirVentasConMesa(ActionEvent event, int numeroMesa) {
        if (daoArqueo.cajaCerradaCamareroHoy()) {
            ControladorAlertas.mostrar("VENTA BLOQUEADA", "La caja del dia ya esta cerrada. No se pueden realizar mas cobros.");
            return;
        }

        ControladorVentas.setMesaInicialPendiente(numeroMesa);

        FXMLLoader loader = Navegacion.cambiarVista(
                event,
                "/org/example/tpv_angela/vistas/camarero/VistaGeneralCamarero.fxml",
                "Sistema TPV - Camarero"
        );

        if (loader != null) {
            ControladorGeneralCamarero controladorGeneral = loader.getController();
            controladorGeneral.setVistaInicial("/org/example/tpv_angela/vistas/camarero/VistaVentas.fxml");
        }
    }

    /**
     * Obtiene el dato solicitado por la vista o por la lógica de negocio.
     * @param mesa número o documento de la mesa implicada.
     * @return texto formateado o normalizado.
     */
    private String obtenerEstadoVisual(Document mesa) {
        String estado = mesa.getString("estado");
        if (estado != null && (estado.equalsIgnoreCase("precuenta") || estado.equalsIgnoreCase("impresa"))) {
            return "precuenta";
        }

        List<?> productos = mesa.getList("productosActuales", Object.class);
        if (productos != null && !productos.isEmpty()) {
            return "ocupada";
        }

        return "libre";
    }

    /**
     * Obtiene el dato solicitado por la vista o por la lógica de negocio.
     * @param mesa número o documento de la mesa implicada.
     * @return valor numérico calculado.
     */
    private int obtenerNumeroMesa(Document mesa) {
        Object numero = mesa.get("numero");
        if (numero instanceof Number) return ((Number) numero).intValue();
        if (numero != null) return Integer.parseInt(numero.toString());
        return 0;
    }

    /**
     * Obtiene el dato solicitado por la vista o por la lógica de negocio.
     * @param mesa número o documento de la mesa implicada.
     * @return valor numérico calculado.
     */
    private int obtenerCapacidad(Document mesa) {
        Object capacidad = mesa.get("capacidad");
        if (capacidad instanceof Number) return ((Number) capacidad).intValue();
        if (capacidad != null) return Integer.parseInt(capacidad.toString());
        return 0;
    }
}
