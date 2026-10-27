package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;
import modelo.Contrato;
import util.DatabaseConnection;

public class ContratoDAO {

    public void insertar(Contrato contrato) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            insertar(contrato, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar el contrato: " + e.getMessage(), e);
        }
    }

    /**
     * Versión sobrecargada para participar en una transacción externa.
     * No abre ni cierra la conexión recibida; solo ejecuta el INSERT y asigna el número autogenerado.
     */
    public void insertar(Contrato contrato, Connection conn) throws SQLException {
        String sql = "INSERT INTO Contratos (Co_feHrEnt, Co_feHrDev, Co_condUso, Co_est, "
                   + "Co_impFianza, L_impAlq, L_cargos, id_Asigna, id_Firma) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, contrato.getFeHrEnt());
            stmt.setString(2, contrato.getFeHrDev());
            stmt.setString(3, contrato.getCondUso());
            stmt.setString(4, contrato.getEst());
            if (contrato.getImpFianza() != null) {
                stmt.setDouble(5, contrato.getImpFianza());
            } else {
                stmt.setNull(5, Types.DOUBLE);
            }
            stmt.setDouble(6, contrato.getImpAlq());
            if (contrato.getCargos() != null) {
                stmt.setDouble(7, contrato.getCargos());
            } else {
                stmt.setNull(7, Types.DOUBLE);
            }
            stmt.setInt(8, contrato.getIdAsigna());
            stmt.setInt(9, contrato.getIdFirma());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    contrato.setNro(keys.getInt(1));
                }
            }
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
                        rs.getInt("id_Firma")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el contrato por Co_nro: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Obtiene los IDs de los vehículos (id_Asigna) que tienen contratos vigentes cuyos períodos
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
