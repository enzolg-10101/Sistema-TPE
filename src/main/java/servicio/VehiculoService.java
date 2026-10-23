package servicio;

import dao.SucursalDAO;
import dao.VehiculoDAO;
import modelo.Sucursal;
import modelo.Vehiculo;

public class VehiculoService {

    private VehiculoDAO vehiculoDAO;
    private SucursalDAO sucursalDAO;

    public VehiculoService() {
        this.vehiculoDAO = new VehiculoDAO();
        this.sucursalDAO = new SucursalDAO();
    }

    public VehiculoService(VehiculoDAO vehiculoDAO, SucursalDAO sucursalDAO) {
        this.vehiculoDAO = vehiculoDAO;
        this.sucursalDAO = sucursalDAO;
    }

    /**
     * 4.1 Alta en la entidad Vehículos
     */
    public Vehiculo altaVehiculo(Vehiculo vehiculo) {
        // Inicio-ts
        try {
            if (vehiculo == null) {
                throw new IllegalArgumentException("El vehículo no puede ser nulo.");
            }

            // Verificar que la patente (Unique) no exista previamente
            Vehiculo existente = vehiculoDAO.buscarPorId(vehiculo.getPat());
            if (existente != null) {
                throw new RuntimeException("Error: Ya existe un vehículo con la patente " + vehiculo.getPat() + ".");
            }

            // Por cada relación saliente total:
            // 1. Generar la nupla correspondiente en Pertenece (FK cod_Pertenece -> Sucursales(S_cod))
            generarNuplaPertenece(vehiculo);

            // Grabar nupla en Vehículos
            vehiculoDAO.insertar(vehiculo);

            return vehiculo;
        } catch (Exception e) {
            throw new RuntimeException("Error en la transacción de Alta de Vehículo: " + e.getMessage(), e);
        }
        // Fin-ts
    }

    // --- 1 Generar la nupla correspondiente en Pertenece ---
    private void generarNuplaPertenece(Vehiculo vehiculo) {
        // Inicio-ts
        // 2. Seleccionar el elemento (s) en la entidad Sucursales según Pertenece saliente
        Sucursal s = seleccionarSucursalPertenece(vehiculo.getCodPertenece());
        vehiculo.setCodPertenece(s.getCod());
        // Fin-ts
    }

    // --- 2 Seleccionar el elemento (s) en la entidad Sucursales según Pertenece saliente ---
    private Sucursal seleccionarSucursalPertenece(Integer codSucursal) {
        // Inicio-ts
        if (codSucursal == null) {
            throw new RuntimeException("Error: Debe especificar el código de la sucursal a la que pertenece el vehículo.");
        }
        Sucursal s = sucursalDAO.buscarPorId(codSucursal);
        if (s == null) {
            throw new RuntimeException("Error: La Sucursal especificada (" + codSucursal + ") no existe.");
        }
        // Sino -> (como es (1:N) no hace falta verificar) -> Retornar (s)
        // Fin-ts
        return s;
    }
}
