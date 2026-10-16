package servicio;

import dao.EmpleadoDAO;
import dao.SucursalDAO;
import modelo.Empleado;
import modelo.Sucursal;

public class EmpleadoService {

    private EmpleadoDAO empleadoDAO;
    private SucursalDAO sucursalDAO;

    public EmpleadoService() {
        this.empleadoDAO = new EmpleadoDAO();
        this.sucursalDAO = new SucursalDAO();
    }

    public EmpleadoService(EmpleadoDAO empleadoDAO, SucursalDAO sucursalDAO) {
        this.empleadoDAO = empleadoDAO;
        this.sucursalDAO = sucursalDAO;
    }

    /**
     * Alta en la entidad Empleados siguiendo el seudocódigo transaccional.
     */
    public Empleado altaEmpleado(Empleado empleado) {
        // Inicio-ts
        try {
            if (empleado == null) {
                throw new IllegalArgumentException("El empleado no puede ser nulo.");
            }

            // Verificar que el DNI (Unique) no esté ya registrado
            Empleado existente = empleadoDAO.buscarPorId(empleado.getDni());
            if (existente != null) {
                throw new RuntimeException("Error: Ya existe un empleado con el DNI " + empleado.getDni() + ".");
            }

            // Por cada relación saliente total:
            // 1. Generar la nupla correspondiente en Trabaja (relación N:1 representada por FK cod_Trabaja)
            generarNuplaTrabaja(empleado);

            // Grabar nupla en Empleados
            empleadoDAO.insertar(empleado);

            return empleado;
        } catch (Exception e) {
            throw new RuntimeException("Error en la transacción de Alta de Empleado: " + e.getMessage(), e);
        }
        // Fin-ts
    }

    // --- 1 Generar la nupla correspondiente en Trabaja ---
    private void generarNuplaTrabaja(Empleado empleado) {
        // Inicio-ts
        // 2. Seleccionar el elemento (s) en la entidad Sucursales según Trabaja saliente
        Sucursal s = seleccionarSucursalTrabaja(empleado.getCodTrabaja());

        // En el modelo relacional, la relación Trabaja (N:1) se materializa mediante la FK cod_Trabaja en Empleados
        empleado.setCodTrabaja(s.getCod());
        // Fin-ts
    }

    // --- 2 Seleccionar el elemento (s) en la entidad Sucursales según Trabaja saliente ---
    private Sucursal seleccionarSucursalTrabaja(Integer codSucursal) {
        // Inicio-ts
        if (codSucursal == null) {
            throw new RuntimeException("Error: Debe especificar el código de la sucursal donde trabaja.");
        }

        // Buscar (s) en Sucursales
        Sucursal s = sucursalDAO.buscarPorId(codSucursal);

        if (s == null) {
            // Si (s) no existe -> Error
            throw new RuntimeException("Error: La Sucursal especificada (" + codSucursal + ") no existe.");
        }
        // Sino -> (como es (N:1) no hace falta verificar) -> Retornar (s)
        // Fin-ts
        return s;
    }
}
