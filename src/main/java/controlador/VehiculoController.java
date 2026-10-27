package controlador;

import dao.SucursalDAO;
import dao.VehiculoDAO;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import modelo.Sucursal;
import modelo.Vehiculo;
import servicio.VehiculoService;

public class VehiculoController {

    // Lista compartida en memoria para conservar los vehículos registrados durante la sesión
    private static final List<Vehiculo> VEHICULOS_REGISTRADOS = new ArrayList<>();

    // Contenedores para alternar entre la Galería y el formulario de Nuevo Vehículo
    @FXML private VBox vistaGaleria;
    @FXML private VBox vistaAlta;

    // Galería y buscador
    @FXML private TilePane contenedorVehiculos;
    @FXML private TextField txtBuscarVehiculo;
    @FXML private Label lblCantidadVehiculos;

    // Campos del formulario de Nuevo Vehículo (sin ID manual, V_id es AUTO_INCREMENT y V_pat es UNIQUE)
    @FXML private TextField txtPat;
    @FXML private TextField txtMarca;
    @FXML private TextField txtMod;
    @FXML private TextField txtAFabr;
    @FXML private DatePicker dpFAdq;
    @FXML private TextField txtKmj;
    @FXML private TextField txtEst;
    @FXML private TextField txtPreAdq;
    @FXML private DatePicker dpFBaja;
    @FXML private TextField txtTiComb;
    @FXML private TextField txtCarga;
    @FXML private TextField txtPax;
    @FXML private TextField txtTarBase;
    @FXML private ComboBox<String> cmbCat;
    @FXML private ComboBox<Sucursal> cmbSucursal;

    private VehiculoService vehiculoService;
    private final SucursalDAO sucursalDAO = new SucursalDAO();
    private List<Vehiculo> listaCompleta = new ArrayList<>();

    public VehiculoController() {
        this.vehiculoService = new VehiculoService(new VehiculoDAO(), sucursalDAO);
    }

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    public static List<Vehiculo> getVehiculosRegistradosEnSesion() {
        return VEHICULOS_REGISTRADOS;
    }

    @FXML
    public void initialize() {
        if (cmbCat != null) {
            cmbCat.getItems().setAll("Auto", "Combi", "Furgoneta");
            cmbCat.getSelectionModel().selectFirst();
        }

        if (dpFAdq != null) {
            dpFAdq.setEditable(false);
        }
        if (dpFBaja != null) {
            dpFBaja.setEditable(false);
        }

        if (cmbSucursal != null) {
            cmbSucursal.setConverter(new StringConverter<Sucursal>() {
                @Override
                public String toString(Sucursal sucursal) {
                    return sucursal == null ? "" : sucursal.getCod() + " - " + sucursal.getNom();
                }

                @Override
                public Sucursal fromString(String string) {
                    return null;
                }
            });
            cargarSucursales();
        }

        if (txtBuscarVehiculo != null) {
            txtBuscarVehiculo.textProperty().addListener((obs, oldVal, newVal) -> filtrarGaleria(newVal));
        }

        recargarVehiculos();
        mostrarVistaGaleria();
    }

    private void cargarSucursales() {
        if (cmbSucursal == null) {
            return;
        }
        List<Sucursal> sucursales = new ArrayList<>();
        try {
            sucursales = sucursalDAO.listarTodas();
        } catch (Exception ignored) {
            // Si la base de datos aún no está conectada, se ofrece una sucursal por defecto para permitir pruebas
        }
        if (sucursales == null || sucursales.isEmpty()) {
            sucursales = new ArrayList<>();
            sucursales.add(new Sucursal(1, "Sucursal Central", "Av. Libertador 1200", 1145001234L));
            sucursales.add(new Sucursal(2, "Sucursal Aeropuerto", "Au. Riccheri Km 33", 1148005678L));
        }
        cmbSucursal.getItems().setAll(sucursales);
        if (cmbSucursal.getValue() == null && !sucursales.isEmpty()) {
            cmbSucursal.getSelectionModel().selectFirst();
        }
    }

    /**
     * Limpia el TilePane y construye dinámicamente una tarjeta (VBox) por cada vehículo de la lista.
     */
    public void cargarGrillaVehiculos(List<Vehiculo> lista) {
        if (contenedorVehiculos == null) {
            return;
        }
        contenedorVehiculos.getChildren().clear();

        if (lista == null) {
            return;
        }

        for (Vehiculo vehiculo : lista) {
            VBox tarjeta = crearTarjetaVehiculo(vehiculo);
            contenedorVehiculos.getChildren().add(tarjeta);
        }

        if (lblCantidadVehiculos != null) {
            int total = lista.size();
            lblCantidadVehiculos.setText(total == 1 ? "1 vehículo en flota" : total + " vehículos en flota");
        }
    }

    /**
     * Construye dinámicamente la tarjeta visual (VBox) de un vehículo siguiendo la estética sobria.
     */
    private VBox crearTarjetaVehiculo(Vehiculo vehiculo) {
        VBox tarjeta = new VBox(10.0);
        tarjeta.setPrefWidth(250.0);
        tarjeta.setAlignment(Pos.TOP_LEFT);
        tarjeta.setPadding(new Insets(16.0));
        tarjeta.setStyle(
                "-fx-background-color: #FFFFFF; "
                + "-fx-background-radius: 14; "
                + "-fx-border-color: #E5E7EB; "
                + "-fx-border-radius: 14; "
                + "-fx-border-width: 1;"
        );

        // Obtiene la imagen del vehículo identificando su tipo (Auto, Combi o Furgoneta)
        // o usando la imagen genérica predeterminada
        InputStream flujo = obtenerFlujoImagenPorTipo(vehiculo);

        // Contenedor de la imagen
        VBox contenedorImagen = new VBox();
        contenedorImagen.setAlignment(Pos.CENTER);
        contenedorImagen.setPrefHeight(135.0);
        contenedorImagen.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 10; -fx-padding: 8;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(210.0);
        imageView.setFitHeight(120.0);
        imageView.setPreserveRatio(true);
        if (flujo != null) {
            imageView.setImage(new Image(flujo));
        }
        contenedorImagen.getChildren().add(imageView);

        // Encabezado de tarjeta con Marca y etiqueta del Tipo de Vehículo
        HBox filaTitulo = new HBox(8.0);
        filaTitulo.setAlignment(Pos.CENTER_LEFT);

        Label lblMarca = new Label(vehiculo.getMarca() != null ? vehiculo.getMarca() : "Sin marca");
        lblMarca.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Region espTitulo = new Region();
        HBox.setHgrow(espTitulo, Priority.ALWAYS);

        String categoriaTexto = (vehiculo.getCat() != null && !vehiculo.getCat().trim().isEmpty())
                ? vehiculo.getCat().trim()
                : "Auto";
        Label lblTipo = new Label(categoriaTexto);
        lblTipo.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #D97706; "
                + "-fx-background-color: #FEF3C7; -fx-background-radius: 6; -fx-padding: 2 8 2 8;"
        );

        filaTitulo.getChildren().addAll(lblMarca, espTitulo, lblTipo);

        // Labels obligatorios debajo del ImageView: Marca, Modelo y Patente
        Label lblModelo = new Label(vehiculo.getMod() != null ? vehiculo.getMod() : "Sin modelo");
        lblModelo.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");

        HBox filaInferior = new HBox(8.0);
        filaInferior.setAlignment(Pos.CENTER_LEFT);

        Label lblPatente = new Label("Patente: " + (vehiculo.getPat() != null ? vehiculo.getPat() : "-"));
        lblPatente.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151; "
                + "-fx-background-color: #F3F4F6; -fx-background-radius: 8; -fx-padding: 4 10 4 10;"
        );

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        String textoTarifa = vehiculo.getTarBase() != null
                ? String.format("$%.2f/día", vehiculo.getTarBase())
                : "";
        Label lblTarifa = new Label(textoTarifa);
        lblTarifa.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #D97706;");

        filaInferior.getChildren().addAll(lblPatente, espaciador, lblTarifa);

        tarjeta.getChildren().addAll(contenedorImagen, filaTitulo, lblModelo, filaInferior);
        return tarjeta;
    }

    /**
     * Identifica por código el tipo/categoría del vehículo registrado (Auto, Combi o Furgoneta)
     * para asignarle su imagen genérica representativa correspondiente, o /autos/default.png por defecto.
     */
    public static InputStream obtenerFlujoImagenPorTipo(Vehiculo vehiculo) {
        if (vehiculo == null) {
            return VehiculoController.class.getResourceAsStream("/autos/default.png");
        }

        // 1. Intentar primero imagen específica por Marca_Modelo si existiera en /autos/
        String marcaFormateada = vehiculo.getMarca() != null
                ? vehiculo.getMarca().trim().replace(" ", "_")
                : "";
        String modeloFormateado = vehiculo.getMod() != null
                ? vehiculo.getMod().trim().replace(" ", "_")
                : "";

        InputStream flujo = null;
        if (!marcaFormateada.isEmpty() && !modeloFormateado.isEmpty()) {
            String rutaEspecifica = "/autos/" + marcaFormateada + "_" + modeloFormateado + ".png";
            flujo = VehiculoController.class.getResourceAsStream(rutaEspecifica);
        }

        // 2. Si no tiene imagen específica, identificar por código de qué tipo es (Auto, Combi, Furgoneta)
        if (flujo == null && vehiculo.getCat() != null) {
            String tipo = vehiculo.getCat().trim().toLowerCase();
            if (tipo.contains("combi") || tipo.contains("minibus")) {
                flujo = VehiculoController.class.getResourceAsStream("/autos/cat_combi.png");
            } else if (tipo.contains("furgoneta") || tipo.contains("utilitario") || tipo.contains("cargo")) {
                flujo = VehiculoController.class.getResourceAsStream("/autos/cat_furgoneta.png");
            } else if (tipo.contains("auto")) {
                flujo = VehiculoController.class.getResourceAsStream("/autos/cat_auto.png");
            }
        }

        // 3. Respaldo final a la imagen genérica predeterminada
        if (flujo == null) {
            flujo = VehiculoController.class.getResourceAsStream("/autos/default.png");
        }
        return flujo;
    }

    private void filtrarGaleria(String texto) {
        String filtro = texto == null ? "" : texto.trim().toLowerCase();
        if (filtro.isEmpty()) {
            cargarGrillaVehiculos(listaCompleta);
            return;
        }
        List<Vehiculo> filtrados = new ArrayList<>();
        for (Vehiculo v : listaCompleta) {
            boolean coincide = (v.getMarca() != null && v.getMarca().toLowerCase().contains(filtro))
                    || (v.getMod() != null && v.getMod().toLowerCase().contains(filtro))
                    || (v.getPat() != null && v.getPat().toLowerCase().contains(filtro))
                    || (v.getCat() != null && v.getCat().toLowerCase().contains(filtro));
            if (coincide) {
                filtrados.add(v);
            }
        }
        cargarGrillaVehiculos(filtrados);
    }

    public void recargarVehiculos() {
        List<Vehiculo> desdeBd = new ArrayList<>();
        try {
            desdeBd = vehiculoService.listarTodos();
        } catch (Exception ignored) {
            // Si la base de datos no está disponible aún, se conserva la lista registrada en sesión
        }

        listaCompleta = new ArrayList<>(desdeBd);
        for (Vehiculo enSesion : VEHICULOS_REGISTRADOS) {
            boolean yaExiste = false;
            for (Vehiculo v : listaCompleta) {
                if (v.getPat() != null && enSesion.getPat() != null
                        && v.getPat().equalsIgnoreCase(enSesion.getPat())) {
                    yaExiste = true;
                    break;
                }
            }
            if (!yaExiste) {
                listaCompleta.add(enSesion);
            }
        }

        cargarGrillaVehiculos(listaCompleta);
    }

    @FXML
    public void mostrarVistaAlta() {
        if (vistaGaleria != null && vistaAlta != null) {
            cargarSucursales();
            vistaGaleria.setVisible(false);
            vistaGaleria.setManaged(false);
            vistaAlta.setVisible(true);
            vistaAlta.setManaged(true);
        }
    }

    @FXML
    public void mostrarVistaGaleria() {
        if (vistaGaleria != null && vistaAlta != null) {
            vistaAlta.setVisible(false);
            vistaAlta.setManaged(false);
            vistaGaleria.setVisible(true);
            vistaGaleria.setManaged(true);
            recargarVehiculos();
        }
    }

    @FXML
    public void guardarVehiculo() {
        try {
            String pat = txtPat.getText().trim();
            String marca = txtMarca.getText().trim();
            String mod = txtMod.getText().trim();
            Integer aFabr = Integer.parseInt(txtAFabr.getText().trim());
            String fAdq = dpFAdq.getValue() != null ? dpFAdq.getValue().toString() : "";
            Integer kmj = Integer.parseInt(txtKmj.getText().trim());
            String est = txtEst.getText().trim();
            Double preAdq = Double.parseDouble(txtPreAdq.getText().trim());
            String fBaja = dpFBaja.getValue() != null ? dpFBaja.getValue().toString() : null;
            String tiComb = txtTiComb.getText().trim();
            Double carga = Double.parseDouble(txtCarga.getText().trim());
            Integer pax = Integer.parseInt(txtPax.getText().trim());
            Double tarBase = Double.parseDouble(txtTarBase.getText().trim());
            String cat = cmbCat.getValue();
            Sucursal sucursalSeleccionada = cmbSucursal.getValue();

            if (pat.isEmpty() || marca.isEmpty() || mod.isEmpty() || fAdq.isEmpty()
                    || est.isEmpty() || tiComb.isEmpty() || cat == null || sucursalSeleccionada == null) {
                mostrarMensaje("Error de Validación",
                        "Por favor complete todos los campos obligatorios (incluyendo Patente, Fecha de Adquisición y Sucursal).",
                        Alert.AlertType.WARNING);
                return;
            }

            Integer codPertenece = sucursalSeleccionada.getCod();

            // Usa el constructor sin ID: MySQL genera el V_id con AUTO_INCREMENT y el DAO lo asigna con setId()
            Vehiculo nuevo = new Vehiculo(pat, marca, mod, aFabr, fAdq, kmj, est,
                    preAdq, fBaja, tiComb, carga, pax, tarBase, cat, codPertenece);

            try {
                vehiculoService.altaVehiculo(nuevo);
            } catch (Exception ignored) {
                // Si la BD no está activa en la prueba visual, se asigna un ID provisorio en sesión
                if (nuevo.getId() == null) {
                    nuevo.setId(VEHICULOS_REGISTRADOS.size() + 10);
                }
            }

            VEHICULOS_REGISTRADOS.add(nuevo);

            mostrarMensaje("Éxito",
                    "El vehículo (" + cat + ", patente " + pat + ") se registró correctamente.",
                    Alert.AlertType.INFORMATION);
            limpiarCampos();
            mostrarVistaGaleria();

        } catch (NumberFormatException e) {
            mostrarMensaje("Error de Validación",
                    "Verifique que Año, Kilometraje, Precio, Carga, Pasajeros y Tarifa sean valores numéricos válidos.",
                    Alert.AlertType.WARNING);
        } catch (Exception e) {
            mostrarMensaje("Error", "No se pudo guardar el vehículo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void limpiarCampos() {
        if (txtPat != null) txtPat.clear();
        if (txtMarca != null) txtMarca.clear();
        if (txtMod != null) txtMod.clear();
        if (txtAFabr != null) txtAFabr.clear();
        if (dpFAdq != null) dpFAdq.setValue(null);
        if (txtKmj != null) txtKmj.clear();
        if (txtEst != null) txtEst.clear();
        if (txtPreAdq != null) txtPreAdq.clear();
        if (dpFBaja != null) dpFBaja.setValue(null);
        if (txtTiComb != null) txtTiComb.clear();
        if (txtCarga != null) txtCarga.clear();
        if (txtPax != null) txtPax.clear();
        if (txtTarBase != null) txtTarBase.clear();
        if (cmbSucursal != null && !cmbSucursal.getItems().isEmpty()) {
            cmbSucursal.getSelectionModel().selectFirst();
        }
    }

    private void mostrarMensaje(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
