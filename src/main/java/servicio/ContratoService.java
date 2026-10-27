package servicio;

import dao.AsignaDAO;
import dao.CheckInOutDAO;
import dao.ClienteDAO;
import dao.ContratoDAO;
import dao.CorrespondeDAO;
import dao.FirmaDAO;
import dao.VehiculoDAO;
import java.sql.Connection;
import java.sql.SQLException;
import modelo.Asigna;
import modelo.CheckInOut;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Corresponde;
import modelo.Firma;
import modelo.Vehiculo;
import util.DatabaseConnection;

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
     * Transacción estricta en JDBC para registrar un contrato que incluye dar de alta a un cliente nuevo.
     * Si falla la inserción del contrato, hace rollback de la conexión para cancelar la creación del cliente.
     */
    public void registrarAlquilerNuevoCliente(Cliente cliente, Contrato contrato) {
        Connection conexion = null;
        try {
            conexion = DatabaseConnection.getConnection();
            conexion.setAutoCommit(false);

            // 1. Insertar el nuevo cliente usando la conexión transaccional y recuperar su ID autogenerado
            clienteDAO.insertar(cliente, conexion);
            contrato.setIdCliente(cliente.getId());

            // 2. Insertar el contrato usando la misma conexión transaccional
            contratoDAO.insertar(contrato, conexion);

            // 3. Si ambas operaciones fueron exitosas, confirmar la transacción
            conexion.commit();

        } catch (Exception e) {
            if (conexion != null) {
                try {
                    conexion.rollback();
                } catch (SQLException exRollback) {
                    e.addSuppressed(exRollback);
                }
            }
            throw new RuntimeException("Error en la transacción de alquiler con nuevo cliente (se aplicó rollback): " + e.getMessage(), e);
        } finally {
            if (conexion != null) {
                try {
                    conexion.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
                try {
                    conexion.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Método principal de alta de Contrato (con soporte para claves primarias AUTO_INCREMENT).
     */
    public void altaContrato(Contrato contrato, Cliente cliente, Vehiculo vehiculo, CheckInOut checkInOut) throws Exception {
        // Inicio-ts
        try {
            // Resolver primero Cliente (por ID o DNI único) y Vehículo (por Patente única)
            // para asegurar que id_Firma e id_Asigna tengan su ID autogenerado antes de insertar el Contrato
            Cliente c = seleccionarClienteFirma(cliente);
            Vehiculo v = seleccionarVehiculoAsigna(vehiculo);

            contrato.setIdFirma(c.getId());
            contrato.setIdAsigna(v.getId());

            // Grabar nupla en Contratos (asigna Co_nro autogenerado via getGeneratedKeys)
            contratoDAO.insertar(contrato);

            // Generar la nupla correspondiente en Firma1
            Firma firma = new Firma(contrato.getId(), c.getId());
            firmaDAO.insertar(firma);

            // Generar la nupla correspondiente en Asigna2
            Asigna asigna = new Asigna(contrato.getId(), v.getId());
            asignaDAO.insertar(asigna);

            // Por cada relación entrante sobreyectiva:
            // Generar la nupla correspondiente en Corresponde3
            checkInOut.setNroCorresponde(contrato.getNro());
            generarNuplaCorresponde(contrato, checkInOut);

        } catch (Exception e) {
            throw new Exception("Error en la transacción de Alta de Contrato: " + e.getMessage(), e);
        }
        // Fin-ts
    }

    // --- 1.1 Seleccionar el elemento c en la entidad Clientes ---
    private Cliente seleccionarClienteFirma(Cliente cliente) {
        Integer criterio = cliente.getId() != null ? cliente.getId() : cliente.getDni();
        Cliente c = (criterio != null) ? clienteDAO.buscarPorId(criterio) : null;

        if (c == null) {
            c = clienteService.altaCliente(cliente);
        }
        return c;
    }

    // --- 2.1 Seleccionar el elemento (v) en la entidad Vehículos ---
    private Vehiculo seleccionarVehiculoAsigna(Vehiculo vehiculo) {
        Vehiculo v = vehiculoDAO.buscarPorId(vehiculo.getPat());

        if (v == null) {
            throw new RuntimeException("Error: El Vehículo especificado no existe.");
        }
        return v;
    }

    // --- 3 Generar la nupla correspondiente en Corresponde ---
    private void generarNuplaCorresponde(Contrato contrato, CheckInOut checkInOut) {
        CheckInOut ch = seleccionarCheckInOutCorresponde(checkInOut);

        Corresponde corresponde = new Corresponde(contrato.getId(), ch.getId());
        correspondeDAO.insertar(corresponde);
    }

    // --- 3.1 Seleccionar el elemento (ch) en la entidad Check_inout ---
    private CheckInOut seleccionarCheckInOutCorresponde(CheckInOut checkInOut) {
        CheckInOut ch = (checkInOut.getId() != null)
                ? checkInOutDAO.buscarPorId(checkInOut.getId())
                : null;

        if (ch == null) {
            ch = checkInOutService.altaCheckInOut(checkInOut);
        } else {
            boolean estaVinculado = correspondeDAO.existeVinculo(ch.getId());
            if (estaVinculado) {
                throw new RuntimeException("Error: El Check_inout ya está vinculado a un contrato existente.");
            }
        }
        return ch;
    }
}
