package servicio;

import dao.EmpleadoDAO;
import dao.MantenimientoDAO;
import dao.VehiculoDAO;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import modelo.Empleado;
import modelo.Mantenimiento;
import modelo.Vehiculo;

public class MantenimientoService {

    private MantenimientoDAO mantenimientoDAO;
    private VehiculoDAO vehiculoDAO;
    private EmpleadoDAO empleadoDAO;

    public MantenimientoService() {
        this.mantenimientoDAO = new MantenimientoDAO();
        this.vehiculoDAO = new VehiculoDAO();
        this.empleadoDAO = new EmpleadoDAO();
    }

    public MantenimientoService(MantenimientoDAO mantenimientoDAO, VehiculoDAO vehiculoDAO, EmpleadoDAO empleadoDAO) {
        this.mantenimientoDAO = mantenimientoDAO;
        this.vehiculoDAO = vehiculoDAO;
        this.empleadoDAO = empleadoDAO;
    }

    /**
     * 4.4 Alta en la entidad Mantenimientos
     */
    public Mantenimiento altaMantenimiento(Mantenimiento mantenimiento) {
        // Inicio-ts
        try {
            if (mantenimiento == null) {
                throw new IllegalArgumentException("El mantenimiento no puede ser nulo.");
            }

            // Validar los atributos propios de Mantenimientos según el modelo relacional
            validarAtributos(mantenimiento);

            // Por cada relación saliente total:
            // 1. Generar la nupla correspondiente en Realizado (FK id_Realizado -> Vehículos(V_id))
            generarNuplaRealizado(mantenimiento);

            // 2. Generar la nupla correspondiente en Realiza (FK id_Realiza -> Empleados(E_id))
            generarNuplaRealiza(mantenimiento);

            // Grabar nupla en Mantenimientos
            mantenimientoDAO.insertar(mantenimiento);

            return mantenimiento;
        } catch (Exception e) {
            throw new RuntimeException("Error en la transacción de Alta de Mantenimiento: " + e.getMessage(), e);
        }
        // Fin-ts
    }

    public List<Mantenimiento> listarTodos() {
        return mantenimientoDAO.listarTodos();
    }

    public List<Mantenimiento> listarPorVehiculo(int idVehiculo) {
        return mantenimientoDAO.listarPorVehiculo(idVehiculo);
    }

    public List<Mantenimiento> listarPorEmpleado(int idEmpleado) {
        return mantenimientoDAO.listarPorEmpleado(idEmpleado);
    }

    public Mantenimiento buscarPorId(int id) {
        return mantenimientoDAO.buscarPorId(id);
    }

    /**
     * Costo total invertido en mantenimientos de un vehículo.
     */
    public double calcularCostoTotalVehiculo(int idVehiculo) {
        double total = 0.0;
        for (Mantenimiento m : mantenimientoDAO.listarPorVehiculo(idVehiculo)) {
            total += (m.getCosto() != null) ? m.getCosto() : 0.0;
        }
        return total;
    }

    // --- Validación de atributos NOT NULL / dominios de Mantenimientos ---
    private void validarAtributos(Mantenimiento m) {
        // M_fec DATE NOT NULL (no puede ser futura)
        if (m.getFec() == null || m.getFec().trim().isEmpty()) {
            throw new RuntimeException("Error: Debe especificar la fecha del mantenimiento (M_fec).");
        }
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(m.getFec().trim().length() > 10 ? m.getFec().trim().substring(0, 10) : m.getFec().trim());
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("Error: La fecha del mantenimiento tiene un formato inválido (se espera aaaa-mm-dd).");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new RuntimeException("Error: La fecha del mantenimiento no puede ser posterior a la fecha actual.");
        }

        // M_tipo ENUM NOT NULL
        if (m.getTipo() == null) {
            throw new RuntimeException("Error: Debe especificar el tipo de mantenimiento (M_tipo).");
        }

        // M_kmj INT NOT NULL
        if (m.getKmj() == null || m.getKmj() < 0) {
            throw new RuntimeException("Error: El kilometraje del mantenimiento (M_kmj) debe ser un valor mayor o igual a 0.");
        }

        // M_costo DECIMAL(8,2) NOT NULL
        if (m.getCosto() == null || m.getCosto() < 0) {
            throw new RuntimeException("Error: El costo del mantenimiento (M_costo) debe ser un valor mayor o igual a 0.");
        }
        if (m.getCosto() > 9999999999999.99) {
            throw new RuntimeException("Error: El costo del mantenimiento supera el máximo permitido (999999.99).");
        }

        // M_obs VARCHAR(100) (opcional)
        if (m.getObs() != null && m.getObs().trim().length() > 100) {
            throw new RuntimeException("Error: Las observaciones (M_obs) no pueden superar los 100 caracteres.");
        }
    }

    // --- 1 Generar la nupla correspondiente en Realizado ---
    private void generarNuplaRealizado(Mantenimiento mantenimiento) {
        // Inicio-ts
        // 1.1 Seleccionar el elemento (v) en la entidad Vehículos según Realizado saliente
        Vehiculo v = seleccionarVehiculoRealizado(mantenimiento.getIdRealizado());

        // En el modelo relacional, Realizado (N:1) se materializa mediante la FK id_Realizado en Mantenimientos
        mantenimiento.setIdRealizado(v.getId());
        // Fin-ts
    }

    // --- 1.1 Seleccionar el elemento (v) en la entidad Vehículos según Realizado saliente ---
    private Vehiculo seleccionarVehiculoRealizado(Integer idVehiculo) {
        // Inicio-ts
        if (idVehiculo == null) {
            throw new RuntimeException("Error: Debe especificar el vehículo al que se le realizó el mantenimiento.");
        }
        // Buscar (v) en Vehículos
        Vehiculo v = vehiculoDAO.buscarPorId(idVehiculo.intValue());
        if (v == null) {
            // Si (v) no existe -> Error
            throw new RuntimeException("Error: El Vehículo especificado (" + idVehiculo + ") no existe.");
        }
        // Sino -> (como es (N:1) no hace falta verificar) -> Retornar (v)
        // Fin-ts
        return v;
    }

    // --- 2 Generar la nupla correspondiente en Realiza ---
    private void generarNuplaRealiza(Mantenimiento mantenimiento) {
        // Inicio-ts
        // 2.1 Seleccionar el elemento (e) en la entidad Empleados según Realiza saliente
        Empleado e = seleccionarEmpleadoRealiza(mantenimiento.getIdRealiza());

        // En el modelo relacional, Realiza (N:1) se materializa mediante la FK id_Realiza en Mantenimientos
        mantenimiento.setIdRealiza(e.getId());
        // Fin-ts
    }

    // --- 2.1 Seleccionar el elemento (e) en la entidad Empleados según Realiza saliente ---
    private Empleado seleccionarEmpleadoRealiza(Integer idEmpleado) {
        // Inicio-ts
        if (idEmpleado == null) {
            throw new RuntimeException("Error: Debe especificar el empleado que realiza el mantenimiento.");
        }
        // Buscar (e) en Empleados
        Empleado e = empleadoDAO.buscarPorId(idEmpleado);
        if (e == null) {
            // Si (e) no existe -> Error
            throw new RuntimeException("Error: El Empleado especificado (" + idEmpleado + ") no existe.");
        }
        // Sino -> (como es (N:1) no hace falta verificar) -> Retornar (e)
        // Fin-ts
        return e;
    }
}
