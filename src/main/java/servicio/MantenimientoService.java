package servicio;

import dao.EmpleadoDAO;
import dao.MantenimientoDAO;
import dao.VehiculoDAO;
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

            // Por cada relación saliente total:
            // 1. Generar la nupla correspondiente en Realizado (FK id_Realizado -> Vehículos(V_id))
            generarNuplaRealizado(mantenimiento);

            // Por cada relación entrante sobreyectiva:
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

    // --- 1 Generar la nupla correspondiente en Realizado ---
    private void generarNuplaRealizado(Mantenimiento mantenimiento) {
        // Inicio-ts
        Vehiculo v = seleccionarVehiculoRealizado(mantenimiento.getIdRealizado());
        mantenimiento.setIdRealizado(v.getId());
        // Fin-ts
    }

    // --- 1.1 Seleccionar el elemento (v) en la entidad Vehículos según Realizado saliente ---
    private Vehiculo seleccionarVehiculoRealizado(Integer idVehiculo) {
        // Inicio-ts
        if (idVehiculo == null) {
            throw new RuntimeException("Error: Debe especificar el ID del vehículo al que se realizó el mantenimiento.");
        }
        Vehiculo v = vehiculoDAO.buscarPorId(idVehiculo);
        if (v == null) {
            throw new RuntimeException("Error: El Vehículo especificado (" + idVehiculo + ") no existe.");
        }
        // Sino -> (como es (N:1) no hace falta verificar) -> Retornar (v)
        // Fin-ts
        return v;
    }

    // --- 2 Generar la nupla correspondiente en Realiza ---
    private void generarNuplaRealiza(Mantenimiento mantenimiento) {
        // Inicio-ts
        Empleado e = seleccionarEmpleadoRealiza(mantenimiento.getIdRealiza());
        mantenimiento.setIdRealiza(e.getId());
        // Fin-ts
    }

    // --- 2.1 Seleccionar el elemento (e) en la entidad Empleados según Realiza entrante ---
    private Empleado seleccionarEmpleadoRealiza(Integer idEmpleado) {
        // Inicio-ts
        if (idEmpleado == null) {
            throw new RuntimeException("Error: Debe especificar el empleado que realiza el mantenimiento.");
        }
        Empleado e = empleadoDAO.buscarPorId(idEmpleado);
        if (e == null) {
            throw new RuntimeException("Error: El Empleado especificado (" + idEmpleado + ") no existe.");
        }
        // Sino -> (como es (1:N) no hace falta verificar) -> Retornar (e)
        // Fin-ts
        return e;
    }
}
