package dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import modelo.CheckInOut;
import modelo.Cliente;
import modelo.Contrato;
import util.DatabaseConnection;

public class ContratoDAO {

    /**
     * Inserta un contrato invocando el procedimiento almacenado
     * 'insertar_contrato'.
     *
     * @param existe     true si el cliente ya existe en la BD (1), false si es un
     *                   nuevo cliente (0).
     * @param contrato   objeto con las fechas, condiciones e importe del contrato.
     * @param cliente    datos del cliente que firma (si existe == false, el SP lo
     *                   inserta en Clientes).
     * @param checkInOut datos de salida del vehículo para la tabla CheckInOuts.
     * @param nroPEnt    empleado de entrada (puede ser null al generar el
     *                   contrato).
     */

    public void insertar(boolean existe, Contrato contrato, Cliente cliente, CheckInOut checkInOut, Integer nroPEnt) {
        String call = "{call insertar_contrato(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getConnection();
                CallableStatement cstmt = conn.prepareCall(call)) {

            // 1. IN existe BOOLEAN
            cstmt.setBoolean(1, existe);

            // 2. IN feHrEnt DATETIME
            setDateTimeOrNull(cstmt, 2, contrato != null ? contrato.getFeHrEnt() : null);

            // 3. IN feHrDev DATETIME
            setDateTimeOrNull(cstmt, 3, contrato != null ? contrato.getFeHrDev() : null);

            // 4. IN condUso VARCHAR(200)
            cstmt.setString(4,
                    (contrato != null && contrato.getCondUso() != null) ? contrato.getCondUso() : "Estándar");

            // 5. IN est VARCHAR(20)
            cstmt.setString(5, (contrato != null && contrato.getEst() != null) ? contrato.getEst() : "Activo");

            // 6. IN impFianza DECIMAL(8,2)
            if (contrato != null && contrato.getImpFianza() != null) {
                cstmt.setDouble(6, contrato.getImpFianza());
            } else {
                cstmt.setDouble(6, 0.0);
            }

            // 7. IN impAlq DECIMAL(8,2)
            cstmt.setDouble(7, (contrato != null && contrato.getImpAlq() != null) ? contrato.getImpAlq() : 0.0);

            // 8. IN id_A INT (vehículo asignado)
            Integer idA = (contrato != null) ? contrato.getIdAsigna() : null;
            if (idA != null) {
                cstmt.setInt(8, idA);
            } else {
                cstmt.setNull(8, Types.INTEGER);
            }

            // 9. IN id_F INT (cliente que firma si ya existe)
            Integer idF = (cliente != null && cliente.getId() != null) ? cliente.getId()
                    : (contrato != null ? contrato.getIdFirma() : null);
            if (existe && idF != null) {
                cstmt.setInt(9, idF);
            } else {
                cstmt.setNull(9, Types.INTEGER);
            }

            // 10. IN dni INT
            if (cliente != null && cliente.getDni() != null) {
                cstmt.setInt(10, cliente.getDni());
            } else {
                cstmt.setNull(10, Types.INTEGER);
            }

            // 11. IN nom VARCHAR(30)
            cstmt.setString(11, (cliente != null && cliente.getNom() != null) ? cliente.getNom() : "");

            // 12. IN apl VARCHAR(30)
            cstmt.setString(12, (cliente != null && cliente.getApl() != null) ? cliente.getApl() : "");

            // 13. IN dir VARCHAR(50)
            cstmt.setString(13, (cliente != null && cliente.getDir() != null) ? cliente.getDir() : "");

            // 14. IN tel INT
            if (cliente != null && cliente.getTel() != null) {
                cstmt.setInt(14, cliente.getTel().intValue());
            } else {
                cstmt.setNull(14, Types.INTEGER);
            }

            // 15. IN correo VARCHAR(40)
            cstmt.setString(15, (cliente != null && cliente.getCorreo() != null) ? cliente.getCorreo() : "");

            // 16. IN feHrSal DATETIME
            String fechaSalida = (checkInOut != null && checkInOut.getFeHrSal() != null)
                    ? checkInOut.getFeHrSal()
                    : (contrato != null ? contrato.getFeHrEnt() : null);
            setDateTimeOrNull(cstmt, 16, fechaSalida);

            // 17. IN kmjSal INT
            if (checkInOut != null && checkInOut.getKmjSal() != null) {
                cstmt.setInt(17, checkInOut.getKmjSal());
            } else {
                cstmt.setInt(17, 0);
            }

            // 18. IN nivCombSal DECIMAL(5,1)
            if (checkInOut != null && checkInOut.getNivCombSal() != null) {
                cstmt.setDouble(18, checkInOut.getNivCombSal());
            } else {
                cstmt.setDouble(18, 100.0);
            }

            // 19. IN daExist VARCHAR(100)
            cstmt.setString(19,
                    (checkInOut != null && checkInOut.getDaExist() != null) ? checkInOut.getDaExist() : "Ninguno");

            // 20. IN nroPSal INT
            if (checkInOut != null && checkInOut.getNroParticipa() != null) {
                cstmt.setInt(20, checkInOut.getNroParticipa());
            } else {
                cstmt.setInt(20, 1);
            }

            // 21. IN nroPEnt INT
            if (nroPEnt != null) {
                cstmt.setInt(21, nroPEnt);
            } else {
                cstmt.setNull(21, Types.INTEGER);
            }

            cstmt.execute();

            // Recuperar ID del contrato autogenerado por el SP
            if (contrato != null && idA != null) {
                recuperarIdContratoGenerado(conn, contrato, idA);
            }

            // Si se registró un cliente nuevo, recuperar su ID autogenerado
            if (!existe && cliente != null && cliente.getDni() != null) {
                recuperarIdClienteGenerado(conn, cliente);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al ejecutar el procedimiento insertar_contrato: " + e.getMessage(), e);
        }
    }

    public void insertar(boolean existe, Contrato contrato, Cliente cliente, CheckInOut checkInOut) {
        insertar(existe, contrato, cliente, checkInOut, null);
    }

    public void insertar(Contrato contrato) {
        insertar(true, contrato, null, null, null);
    }

    private void setDateTimeOrNull(CallableStatement cstmt, int parameterIndex, String fechaStr) throws SQLException {
        String formateada = formatearDateTime(fechaStr);
        if (formateada != null) {
            cstmt.setString(parameterIndex, formateada);
        } else {
            cstmt.setNull(parameterIndex, Types.TIMESTAMP);
        }
    }

    private String formatearDateTime(String fecha) {
        if (fecha == null || fecha.trim().isEmpty()) {
            return null;
        }
        String f = fecha.trim();
        if (f.length() == 10) { // Formato "yyyy-MM-dd"
            return f + " 00:00:00";
        }
        return f;
    }

    private void recuperarIdContratoGenerado(Connection conn, Contrato contrato, int idAsigna) {
        String sql = "SELECT Co_nro FROM Contratos WHERE id_Asigna = ? ORDER BY Co_nro DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAsigna);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    contrato.setNro(rs.getInt(1));
                }
            }
        } catch (SQLException ignored) {
        }
    }

    private void recuperarIdClienteGenerado(Connection conn, Cliente cliente) {
        String sql = "SELECT C_id FROM Clientes WHERE C_dni = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cliente.getDni());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    cliente.setId(rs.getInt(1));
                }
            }
        } catch (SQLException ignored) {
        }
    }

    public Contrato buscarPorId(int id) {
        String sql = "SELECT Co_nro, Co_feHrEnt, Co_feHrDev, Co_condUso, Co_est, "
                + "Co_impFianza, L_impAlq, L_cargos, id_Asigna, id_Firma "
                + "FROM Contratos WHERE Co_nro = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Double fianza = rs.getObject("Co_impFianza") != null ? rs.getDouble("Co_impFianza") : null;
                    Double cargos = rs.getObject("L_cargos") != null ? rs.getDouble("L_cargos") : null;
                    return new Contrato(
                            rs.getInt("Co_nro"),
                            rs.getString("Co_feHrEnt"),
                            rs.getString("Co_feHrDev"),
                            rs.getString("Co_condUso"),
                            rs.getString("Co_est"),
                            fianza,
                            rs.getDouble("L_impAlq"),
                            cargos,
                            rs.getInt("id_Asigna"),
                            rs.getInt("id_Firma"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el contrato por Co_nro: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Ejecuta el procedimiento almacenado 'registrar_devolucion' en la base de
     * datos.
     * Actualiza el contrato a estado 'Devuelto' con los cargos correspondientes y
     * completa la información de entrega en la tabla checkInOuts.
     *
     * @param nroContrato número de contrato (Co_nro)
     * @param kmj         kilometraje de entrada/devolución
     * @param nivComb     nivel de combustible de devolución (0.0 - 100.0)
     * @param daNuevos    daños nuevos detectados al devolver
     * @param cargos      cargos adicionales por demora o daños
     * @param sucEnt      código de la sucursal donde se entrega el vehículo
     */
    public void registrarDevolucion(int nroContrato, int kmj, double nivComb, String daNuevos, double cargos,
            int sucEnt) {
        String call = "{call registrar_devolucion(?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getConnection();
                CallableStatement cstmt = conn.prepareCall(call)) {

            cstmt.setInt(1, nroContrato);
            cstmt.setInt(2, kmj);
            cstmt.setDouble(3, nivComb);
            cstmt.setString(4, (daNuevos != null && !daNuevos.trim().isEmpty()) ? daNuevos.trim() : "Ninguno");
            cstmt.setDouble(5, cargos);
            cstmt.setInt(6, sucEnt);

            cstmt.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Error al ejecutar registrar_devolucion: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene la lista completa de contratos desde la base de datos,
     * incorporando mediante JOIN los datos del cliente y del vehículo asociado.
     */
    public List<Contrato> listarTodos() {
        List<Contrato> lista = new ArrayList<>();
        String sql = "SELECT c.*, "
                + "       cl.C_nom, cl.C_apl, cl.C_dni, "
                + "       v.V_pat, v.V_marca, v.V_mod "
                + "FROM Contratos c "
                + "LEFT JOIN Clientes cl ON c.id_Firma = cl.C_id "
                + "LEFT JOIN Vehiculos v ON c.id_Asigna = v.V_id "
                + "ORDER BY c.Co_nro DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearContrato(rs));
            }
        } catch (SQLException e) {
            // Consulta de respaldo sin JOIN en caso de que alguna tabla relacionada tenga
            // esquema modificado
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Contratos ORDER BY Co_nro DESC");
                    ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearContrato(rs));
                }
            } catch (SQLException ex) {
                throw new RuntimeException("Error al listar contratos desde la base de datos: " + ex.getMessage(), ex);
            }
        }
        return lista;
    }

    private Contrato mapearContrato(ResultSet rs) throws SQLException {
        int nro = rs.getInt("Co_nro");

        String feHrEnt = null;
        try {
            feHrEnt = rs.getString("Co_feHrEnt");
        } catch (SQLException e) {
            try {
                feHrEnt = rs.getString("C_feHrEnt");
            } catch (SQLException ignored) {
            }
        }

        String feHrDev = null;
        try {
            feHrDev = rs.getString("Co_feHrDev");
        } catch (SQLException e) {
            try {
                feHrDev = rs.getString("C_feHrDev");
            } catch (SQLException ignored) {
            }
        }

        String condUso = rs.getString("Co_condUso");
        String est = rs.getString("Co_est");
        Double fianza = rs.getObject("Co_impFianza") != null ? rs.getDouble("Co_impFianza") : null;
        Double alq = rs.getObject("L_impAlq") != null ? rs.getDouble("L_impAlq") : 0.0;
        Double cargos = rs.getObject("L_cargos") != null ? rs.getDouble("L_cargos") : null;
        Integer idAsigna = rs.getObject("id_Asigna") != null ? rs.getInt("id_Asigna") : null;
        Integer idFirma = rs.getObject("id_Firma") != null ? rs.getInt("id_Firma") : null;

        Contrato c = new Contrato(nro, feHrEnt, feHrDev, condUso, est, fianza, alq, cargos, idAsigna, idFirma);

        try {
            String nom = rs.getString("C_nom");
            String apl = rs.getString("C_apl");
            if (nom != null || apl != null) {
                c.setClienteNombre(((nom != null ? nom : "") + " " + (apl != null ? apl : "")).trim());
            }
            if (rs.getObject("C_dni") != null) {
                c.setClienteDni(rs.getInt("C_dni"));
            }
        } catch (SQLException ignored) {
        }

        try {
            String marca = rs.getString("V_marca");
            String mod = rs.getString("V_mod");
            if (marca != null || mod != null) {
                c.setVehiculoModelo(((marca != null ? marca : "") + " " + (mod != null ? mod : "")).trim());
            }
            c.setVehiculoPatente(rs.getString("V_pat"));
        } catch (SQLException ignored) {
        }

        return c;
    }

    /**
     * Obtiene los IDs de los vehículos (id_Asigna) que tienen contratos vigentes
     * cuyos períodos
     * se solapan con el rango [fechaInicio, fechaFin].
     */
    public Set<Integer> obtenerIdsVehiculosOcupadosEntreFechas(String fechaInicio, String fechaFin) {
        Set<Integer> ocupados = new HashSet<>();
        String sql = "SELECT DISTINCT id_Asigna FROM Contratos "
                + "WHERE Co_feHrEnt <= ? AND Co_feHrDev >= ? "
                + "AND (Co_est IS NULL OR LOWER(Co_est) NOT IN ('cancelado', 'finalizado'))";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fechaFin);
            stmt.setString(2, fechaInicio);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ocupados.add(rs.getInt("id_Asigna"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar disponibilidad entre fechas: " + e.getMessage(), e);
        }
        return ocupados;
    }
}
