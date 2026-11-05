package dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.MedioPago;
import modelo.Pago;
import util.DatabaseConnection;

public class PagoDAO {

    // Lista en memoria para conservar pagos realizados durante la sesión actual (o si BD no está disponible)
    private static final List<Pago> PAGOS_EN_MEMORIA = new ArrayList<>();

    public static List<Pago> getPagosEnMemoria() {
        return Collections.unmodifiableList(PAGOS_EN_MEMORIA);
    }

    public static void registrarPagoEnMemoria(Pago pago) {
        if (pago != null) {
            PAGOS_EN_MEMORIA.add(pago);
        }
    }

    /**
     * Inserta un pago ejecutando el procedimiento almacenado 'insertar_pago' (con fallback a INSERT directo).
     */
    public void insertar(Pago pago) {
        if (pago == null) {
            throw new IllegalArgumentException("El pago no puede ser nulo.");
        }

        String medioBd = pago.getMedio() != null ? pago.getMedio().getNombreBd() : "Efectivo";
        Timestamp fechaTs = convertirATimestamp(pago.getFec());

        boolean bdConectada = false;
        try (Connection conn = DatabaseConnection.getConnection()) {
            bdConectada = true;

            // 1. Intentar ejecutar el procedimiento almacenado insertar_pago
            try (CallableStatement cstmt = conn.prepareCall("{call insertar_pago(?, ?, ?, ?)}")) {
                cstmt.setTimestamp(1, fechaTs);
                cstmt.setDouble(2, pago.getImp());
                cstmt.setString(3, medioBd);
                cstmt.setInt(4, pago.getNroRegistra());
                cstmt.execute();
            } catch (SQLException spEx) {
                // Si el procedimiento no existe en la base de datos (error 1305), recurrir a INSERT directo
                if (spEx.getErrorCode() == 1305 || (spEx.getMessage() != null && spEx.getMessage().toLowerCase().contains("does not exist"))) {
                    insertarDirecto(conn, pago, fechaTs, medioBd);
                } else {
                    throw spEx;
                }
            }

            // Recuperar el ID autogenerado para el objeto Pago
            recuperarIdGenerado(conn, pago);
            registrarPagoEnMemoria(pago);
            // El registro en memoria solo se realiza si la inserción en BD fue exitosa

        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar el pago en la base de datos: " + e.getMessage(), e);
        } finally {
            // Guardar también en la sesión en memoria
            // El registro en memoria solo se realiza si la inserción en BD fue exitosa
        }
    }

    private void insertarDirecto(Connection conn, Pago pago, Timestamp fechaTs, String medioBd) throws SQLException {
        String sql = "INSERT INTO Pagos (P_fec, P_imp, P_medio, nro_Registra) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, fechaTs);
            stmt.setDouble(2, pago.getImp());
            stmt.setString(3, medioBd);
            stmt.setInt(4, pago.getNroRegistra());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    pago.setId(keys.getInt(1));
                }
            }
        }

        // Si se saldó la totalidad del contrato, actualizar Co_est a 'Pagado'
        verificarYActualizarEstadoContrato(conn, pago.getNroRegistra());
    }

    private void verificarYActualizarEstadoContrato(Connection conn, int nroContrato) {
        String sqlTotal = "SELECT (L_impAlq + COALESCE(L_cargos, 0)) AS total FROM Contratos WHERE Co_nro = ?";
        String sqlPagado = "SELECT COALESCE(SUM(P_imp), 0) AS pagado FROM Pagos WHERE nro_Registra = ?";
        try (PreparedStatement stmtTotal = conn.prepareStatement(sqlTotal);
             PreparedStatement stmtPagado = conn.prepareStatement(sqlPagado)) {

            stmtTotal.setInt(1, nroContrato);
            double total = 0.0;
            try (ResultSet rs = stmtTotal.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble("total");
                }
            }

            stmtPagado.setInt(1, nroContrato);
            double pagado = 0.0;
            try (ResultSet rs = stmtPagado.executeQuery()) {
                if (rs.next()) {
                    pagado = rs.getDouble("pagado");
                }
            }

            if (pagado >= total && total > 0) {
                try (PreparedStatement stmtUpd = conn.prepareStatement("UPDATE Contratos SET Co_est = 'Pagado' WHERE Co_nro = ?")) {
                    stmtUpd.setInt(1, nroContrato);
                    stmtUpd.executeUpdate();
                }
            }
        } catch (SQLException ignored) {
        }
    }

    private void recuperarIdGenerado(Connection conn, Pago pago) {
        if (pago.getId() != null) {
            return;
        }
        String sql = "SELECT P_id FROM Pagos WHERE nro_Registra = ? ORDER BY P_id DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pago.getNroRegistra());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    pago.setId(rs.getInt(1));
                }
            }
        } catch (SQLException ignored) {
        }
    }

    private Timestamp convertirATimestamp(String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            return Timestamp.valueOf(LocalDateTime.now());
        }
        String f = fechaStr.trim();
        if (f.length() == 10) { // yyyy-MM-dd
            LocalDate ld = LocalDate.parse(f);
            return Timestamp.valueOf(ld.atTime(12, 0, 0));
        }
        try {
            return Timestamp.valueOf(f);
        } catch (Exception e) {
            return Timestamp.valueOf(LocalDateTime.now());
        }
    }

    public Pago buscarPorId(int id) {
        String sql = "SELECT P_id, P_fec, P_imp, P_medio, nro_Registra FROM Pagos WHERE P_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPago(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el pago por P_id: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Pago> listarPorContrato(int nroContrato) {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT P_id, P_fec, P_imp, P_medio, nro_Registra FROM Pagos WHERE nro_Registra = ? ORDER BY P_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nroContrato);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearPago(rs));
                }
            }
        } catch (Exception e) {
            // Si la base de datos no está disponible, utilizar la sesión en memoria
        }

        // Incorporar pagos de la sesión en memoria si aún no están en la lista
        for (Pago enMemoria : PAGOS_EN_MEMORIA) {
            if (enMemoria.getNroRegistra() != null && enMemoria.getNroRegistra() == nroContrato) {
                boolean yaExiste = false;
                for (Pago p : lista) {
                    if (p.getId() != null && p.getId().equals(enMemoria.getId())) {
                        yaExiste = true;
                        break;
                    }
                }
                if (!yaExiste) {
                    lista.add(enMemoria);
                }
            }
        }

        return lista;
    }

    public double obtenerTotalPagado(int nroContrato) {
        double total = 0.0;
        String sql = "SELECT COALESCE(SUM(P_imp), 0) FROM Pagos WHERE nro_Registra = ?";
        boolean bdConsultada = false;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nroContrato);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble(1);
                    bdConsultada = true;
                }
            }
        } catch (Exception ignored) {
        }

        if (!bdConsultada) {
            for (Pago p : PAGOS_EN_MEMORIA) {
                if (p.getNroRegistra() != null && p.getNroRegistra() == nroContrato) {
                    total += (p.getImp() != null ? p.getImp() : 0.0);
                }
            }
        }
        return total;
    }

    private Pago mapearPago(ResultSet rs) throws SQLException {
        return new Pago(
            rs.getInt("P_id"),
            rs.getString("P_fec"),
            rs.getDouble("P_imp"),
            MedioPago.fromString(rs.getString("P_medio")),
            rs.getInt("nro_Registra")
        );
    }
}
