package servicio;

import dao.AsignaDAO;
import dao.CheckInOutDAO;
import dao.ClienteDAO;
import dao.ContratoDAO;
import dao.CorrespondeDAO;
import dao.FirmaDAO;
import dao.VehiculoDAO;
import modelo.Asigna;
import modelo.CheckInOut;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Corresponde;
import modelo.Firma;
import modelo.Vehiculo;

public class ContratoService {

    // Dependencias de los DAOs (repositorios) y otros servicios
    private ContratoDAO contratoDAO;
    private ClienteDAO clienteDAO;
    private VehiculoDAO vehiculoDAO;
    private CheckInOutDAO checkInOutDAO;
    private FirmaDAO firmaDAO;
    private AsignaDAO asignaDAO;
    private CorrespondeDAO correspondeDAO;

    private ClienteService clienteService;
    private CheckInOutService checkInOutService;

    public ContratoService(ContratoDAO contratoDAO, ClienteDAO clienteDAO, VehiculoDAO vehiculoDAO,
                           CheckInOutDAO checkInOutDAO, FirmaDAO firmaDAO, AsignaDAO asignaDAO,
                           CorrespondeDAO correspondeDAO, ClienteService clienteService,
                           CheckInOutService checkInOutService) {
        this.contratoDAO = contratoDAO;
        this.clienteDAO = clienteDAO;
        this.vehiculoDAO = vehiculoDAO;
        this.checkInOutDAO = checkInOutDAO;
        this.firmaDAO = firmaDAO;
        this.asignaDAO = asignaDAO;
        this.correspondeDAO = correspondeDAO;
        this.clienteService = clienteService;
        this.checkInOutService = checkInOutService;
    }

    /**
     * Método principal de alta de Contrato.
     */
    public void altaContrato(Contrato contrato, Cliente cliente, Vehiculo vehiculo, CheckInOut checkInOut) throws Exception {
        // Inicio-ts
        try {
            // Ingresar los valores para los atributos de Contratos (ya vienen en el objeto 'contrato')
            
            // Grabar nupla en Contratos
            contratoDAO.insertar(contrato);
            
            // Por cada relación saliente total:
            // Generar la nupla correspondiente en Firma1
            generarNuplaFirma(contrato, cliente);
            
            // Generar la nupla correspondiente en Asigna2
            generarNuplaAsigna(contrato, vehiculo);
            
            // Por cada relación entrante sobreyectiva:
            // Generar la nupla correspondiente en Corresponde3
            generarNuplaCorresponde(contrato, checkInOut);
            
        } catch (Exception e) {
            // Rollback en caso de error (Manejo de transacción estándar)
            throw new Exception("Error en la transacción de Alta de Contrato: " + e.getMessage(), e);
        }
        // Fin-ts
    }

    // --- 1 Generar la nupla correspondiente en Firma ---
    private void generarNuplaFirma(Contrato contrato, Cliente cliente) {
        // Inicio-ts
        // 1.1 Seleccionar el elemento c en la entidad Clientes según Firma saliente
        Cliente c = seleccionarClienteFirma(cliente);
        
        // Grabar la nupla generada en Firma
        Firma firma = new Firma(contrato.getId(), c.getId()); // Asumiendo que el modelo Firma une los IDs
        firmaDAO.insertar(firma);
        // Fin-ts
    }

    // --- 1.1 Seleccionar el elemento c en la entidad Clientes ---
    private Cliente seleccionarClienteFirma(Cliente cliente) {
        // Inicio-ts
        // Buscar (c) en Clientes
        Cliente c = clienteDAO.buscarPorId(cliente.getId());
        
        if (c == null) {
            // Si (c) no existe -> Alta en la entidad Clientes (proceso recursivo/delegado)
            c = clienteService.altaCliente(cliente);
        }
        // Sino -> Ok
        
        // Retornar (c)
        // Fin-ts
        return c;
    }

    // --- 2 Generar la nupla correspondiente en Asigna ---
    private void generarNuplaAsigna(Contrato contrato, Vehiculo vehiculo) {
        // Inicio-ts
        // 2.1 Seleccionar el elemento (v) en la entidad Vehículos según Asigna saliente
        Vehiculo v = seleccionarVehiculoAsigna(vehiculo);
        
        // Grabar la nupla generada en Asigna
        Asigna asigna = new Asigna(contrato.getId(), v.getId());
        asignaDAO.insertar(asigna);
        // Fin-ts
    }

    // --- 2.1 Seleccionar el elemento (v) en la entidad Vehículos ---
    private Vehiculo seleccionarVehiculoAsigna(Vehiculo vehiculo) {
        // Inicio-ts
        // Buscar (v) en Vehículos
        Vehiculo v = vehiculoDAO.buscarPorId(vehiculo.getPat());
        
        if (v == null) {
            // Si (v) no existe -> Error
            throw new RuntimeException("Error: El Vehículo especificado no existe.");
        }
        // Sino -> Ok
        
        // Retornar (v)
        // Fin-ts
        return v;
    }

    // --- 3 Generar la nupla correspondiente en Corresponde ---
    private void generarNuplaCorresponde(Contrato contrato, CheckInOut checkInOut) {
        // Inicio-ts
        // 3.1 Seleccionar el elemento ch en la entidad Check_inout según Corresponde entrante
        CheckInOut ch = seleccionarCheckInOutCorresponde(checkInOut);
        
        // Grabar la nupla generada en Corresponde
        Corresponde corresponde = new Corresponde(contrato.getId(), ch.getId());
        correspondeDAO.insertar(corresponde);
        // Fin-ts
    }

    // --- 3.1 Seleccionar el elemento (ch) en la entidad Check_inout ---
    private CheckInOut seleccionarCheckInOutCorresponde(CheckInOut checkInOut) {
        // Inicio-ts
        // Buscar (ch) en Check_inout
        CheckInOut ch = checkInOutDAO.buscarPorId(checkInOut.getId());
        
        if (ch == null) {
            // Si (ch) no existe -> Alta en la entidad Check_inout
            ch = checkInOutService.altaCheckInOut(checkInOut);
        } else {
            // Sino -> Verificar que (ch) no está vinculado a ningún elemento de Contratos
            boolean estaVinculado = correspondeDAO.existeVinculo(ch.getId());
            if (estaVinculado) {
                // Si está -> Error
                throw new RuntimeException("Error: El Check_inout ya está vinculado a un contrato existente.");
            }
            // Sino -> Ok
        }
        
        // Retornar (ch)
        // Fin-ts
        return ch;
    }
}
