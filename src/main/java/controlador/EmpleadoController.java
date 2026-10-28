package controlador;

import dao.EmpleadoDAO;
import dao.SucursalDAO;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import modelo.Empleado;
import modelo.Sucursal;
import servicio.EmpleadoService;

public class EmpleadoController {

    // Contenedores para alternar entre el Buscador y la pantalla de Alta
    @FXML private VBox vistaBuscador;
    @FXML private VBox vistaAlta;

    // Componentes del Buscador dinámico
    @FXML private TextField txtBuscar;
    @FXML private Label lblCantidadResultados;
    @FXML private TableView<Empleado> tablaEmpleados;
    @FXML private TableColumn<Empleado, Integer> colId;
    @FXML private TableColumn<Empleado, Integer> colDni;
    @FXML private TableColumn<Empleado, String> colNom;
    @FXML private TableColumn<Empleado, String> colApl;
    @FXML private TableColumn<Empleado, String> colDir;
    @FXML private TableColumn<Empleado, Long> colTel;
    @FXML private TableColumn<Empleado, String> colCorreo;
    @FXML private TableColumn<Empleado, String> colFIng;
    @FXML private TableColumn<Empleado, Integer> colCodTrabaja;

    // Variables @FXML para el formulario de Alta de Empleado (sin campo ID, usa DNI único)
    @FXML private TextField txtDni;
    @FXML private TextField txtNom;
    @FXML private TextField txtApl;
    @FXML private TextField txtDir;
    @FXML private TextField txtTel;
    @FXML private TextField txtCorreo;
    @FXML private DatePicker dpFIng;
    @FXML private ComboBox<Sucursal> cmbSucursal;

    // Dependencias hacia la capa de servicio y DAO de sucursales
    private EmpleadoService empleadoService;
    private final SucursalDAO sucursalDAO = new SucursalDAO();
    private final ObservableList<Empleado> listaMaster = FXCollections.observableArrayList();
    private FilteredList<Empleado> listaFiltrada;

    public EmpleadoController() {
        this.empleadoService = new EmpleadoService(new EmpleadoDAO(), sucursalDAO);
    }

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    public void setEmpleadoService(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @FXML
    public void initialize() {
        configurarColumnasTabla();
        configurarBuscadorDinamico();
        configurarControlesAlta();
        recargarEmpleados();
        mostrarPantallaBuscador();
    }

    private void configurarControlesAlta() {
        if (dpFIng != null) {
            dpFIng.setEditable(false);
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
    }

    private void cargarSucursales() {
        if (cmbSucursal == null) {
            return;
        }
        List<Sucursal> sucursales = new ArrayList<>();
        try {
            sucursales = sucursalDAO.listarTodas();
        } catch (Exception ignored) {
            // Si la base de datos aún no está conectada, se ofrecen sucursales por defecto
        }
        if (sucursales == null || sucursales.isEmpty()) {
            sucursales = new ArrayList<>();
            sucursales.add(new Sucursal(1, "Sucursal Central", "Av. Libertador 1200", 1145001234L));
            sucursales.add(new Sucursal(2, "Sucursal Aeropuerto", "Au. Riccheri Km 33", 1148005678L));
        }
        cmbSucursal.getItems().setAll(sucursales);
    }

    private void configurarColumnasTabla() {
        if (tablaEmpleados == null) {
            return;
        }
        colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        colDni.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDni()));
        colNom.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNom()));
        colApl.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getApl()));
        colDir.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDir()));
        colTel.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getTel()));
        colCorreo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCorreo()));
        colFIng.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getfIng()));
        colCodTrabaja.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getCodTrabaja()));
    }

    private void configurarBuscadorDinamico() {
        if (tablaEmpleados == null) {
            return;
        }
        listaFiltrada = new FilteredList<>(listaMaster, e -> true);
        tablaEmpleados.setItems(listaFiltrada);

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, valorAnterior, nuevoTexto) -> {
                String filtro = (nuevoTexto == null) ? "" : nuevoTexto.trim().toLowerCase();
                listaFiltrada.setPredicate(emp -> {
                    if (filtro.isEmpty()) {
                        return true;
                    }
                    return coincide(emp.getId(), filtro)
                            || coincide(emp.getDni(), filtro)
                            || coincide(emp.getNom(), filtro)
                            || coincide(emp.getApl(), filtro)
                            || coincide(emp.getDir(), filtro)
                            || coincide(emp.getTel(), filtro)
                            || coincide(emp.getCorreo(), filtro)
                            || coincide(emp.getfIng(), filtro)
                            || coincide(emp.getCodTrabaja(), filtro);
                });
                actualizarContadorResultados();
            });
        }
    }

    private boolean coincide(Object valor, String filtro) {
        return valor != null && valor.toString().toLowerCase().contains(filtro);
    }

    public void recargarEmpleados() {
        try {
            List<Empleado> empleados = empleadoService.listarTodos();
            listaMaster.setAll(empleados);
        } catch (Exception e) {
            // Si la base de datos aún no está levantada o conectada, mantiene la lista actual
        }
        actualizarContadorResultados();
    }

    private void actualizarContadorResultados() {
        if (lblCantidadResultados != null && listaFiltrada != null) {
            int total = listaFiltrada.size();
            lblCantidadResultados.setText(total == 1 ? "1 empleado encontrado" : total + " empleados encontrados");
        }
    }

    @FXML
    public void mostrarPantallaAlta() {
        if (vistaBuscador != null && vistaAlta != null) {
            cargarSucursales();
            vistaBuscador.setVisible(false);
            vistaBuscador.setManaged(false);
            vistaAlta.setVisible(true);
            vistaAlta.setManaged(true);
        }
    }

    @FXML
    public void mostrarPantallaBuscador() {
        if (vistaBuscador != null && vistaAlta != null) {
            vistaAlta.setVisible(false);
            vistaAlta.setManaged(false);
            vistaBuscador.setVisible(true);
            vistaBuscador.setManaged(true);
            recargarEmpleados();
        }
    }

    @FXML
    public void limpiarBusqueda() {
        if (txtBuscar != null) {
            txtBuscar.clear();
        }
    }

    /**
     * Método vinculado al botón 'Guardar Empleado' (usa constructor sin ID para AUTO_INCREMENT).
     */
    @FXML
    public void guardarEmpleado() {
        try {
            Integer dni = Integer.parseInt(txtDni.getText().trim());
            String nom = txtNom.getText().trim();
            String apl = txtApl.getText().trim();
            String dir = txtDir.getText().trim();
            Long tel = Long.parseLong(txtTel.getText().trim());
            String correo = txtCorreo.getText().trim();
            String fIng = dpFIng.getValue() != null ? dpFIng.getValue().toString() : "";
            Sucursal sucursalSeleccionada = cmbSucursal.getValue();

            if (nom.isEmpty() || apl.isEmpty() || dir.isEmpty() || correo.isEmpty()
                    || fIng.isEmpty() || sucursalSeleccionada == null) {
                mostrarMensaje("Error de Validación",
                        "Todos los campos son obligatorios (incluyendo la Fecha de Ingreso y la Sucursal).",
                        Alert.AlertType.WARNING);
                return;
            }

            Integer codTrabaja = sucursalSeleccionada.getCod();

            // Armado del objeto Empleado con el constructor sin ID
            Empleado empleado = new Empleado(dni, nom, apl, dir, tel, correo, fIng, codTrabaja);

            // Llamada al servicio de alta (asigna el ID autogenerado por MySQL)
            empleadoService.altaEmpleado(empleado);
            listaMaster.add(empleado);
            actualizarContadorResultados();

            mostrarMensaje("Éxito", "El empleado ha sido guardado correctamente.", Alert.AlertType.INFORMATION);
            limpiarCampos();
            mostrarPantallaBuscador();

        } catch (NumberFormatException e) {
            mostrarMensaje("Error de Validación", "Por favor, verifique que DNI y Teléfono sean números válidos.", Alert.AlertType.WARNING);
        } catch (Exception e) {
            mostrarMensaje("Error", "Ocurrió un error al guardar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void mostrarMensaje(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }

    @FXML
    public void limpiarCampos() {
        if (txtDni != null) txtDni.clear();
        if (txtNom != null) txtNom.clear();
        if (txtApl != null) txtApl.clear();
        if (txtDir != null) txtDir.clear();
        if (txtTel != null) txtTel.clear();
        if (txtCorreo != null) txtCorreo.clear();
        if (dpFIng != null) dpFIng.setValue(null);
        if (cmbSucursal != null) cmbSucursal.setValue(null);
    }
}
