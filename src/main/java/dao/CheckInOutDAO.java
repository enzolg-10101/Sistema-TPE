package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import modelo.CheckInOut;
import util.DatabaseConnection;

public class CheckInOutDAO {

    public void insertar(CheckInOut checkInOut) {
        String sql = "INSERT INTO checkInOuts (Ch_feHrSal, Ch_kmjSal, Ch_nivCombSal, Ch_daExist, "
                   + "Ch_feHrEnt, Ch_kmjEnt, Ch_nivCombEnt, Ch_daNuevos, nro_ParticipaSal, nro_ParticipaEnt, nro_Corresponde) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, checkInOut.getFeHrSal());
            stmt.setInt(2, checkInOut.getKmjSal());
            stmt.setDouble(3, checkInOut.getNivCombSal());
            stmt.setString(4, checkInOut.getDaExist() != null ? checkInOut.getDaExist() : "Ninguno");
            if (checkInOut.getFeHrEnt() != null) {
                stmt.setString(5, checkInOut.getFeHrEnt());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }
            if (checkInOut.getKmjEnt() != null) {
                stmt.setInt(6, checkInOut.getKmjEnt());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            if (checkInOut.getNivCombEnt() != null) {
                stmt.setDouble(7, checkInOut.getNivCombEnt());
            } else {
                stmt.setNull(7, Types.DOUBLE);
            }
            if (checkInOut.getDaNuevos() != null) {
                stmt.setString(8, checkInOut.getDaNuevos());
            } else {
                stmt.setNull(8, Types.VARCHAR);
            }

            Integer nroSal = checkInOut.getNroParticipaSal() != null
                    ? checkInOut.getNroParticipaSal()
                    : checkInOut.getNroParticipa();
            stmt.setInt(9, nroSal != null ? nroSal : 1);

            if (checkInOut.getNroParticipaEnt() != null) {
                stmt.setInt(10, checkInOut.getNroParticipaEnt());
            } else {
                stmt.setNull(10, Types.INTEGER);
            }

            stmt.setInt(11, checkInOut.getNroCorresponde());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    checkInOut.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar el CheckInOut: " + e.getMessage(), e);
        }
    }

    public CheckInOut buscarPorId(int id) {
        String sql = "SELECT Ch_id, Ch_feHrSal, Ch_kmjSal, Ch_nivCombSal, Ch_daExist, "
                   + "Ch_feHrEnt, Ch_kmjEnt, Ch_nivCombEnt, Ch_daNuevos, nro_ParticipaSal, nro_ParticipaEnt, nro_Corresponde "
                   + "FROM checkInOuts WHERE Ch_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearCheckInOut(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el CheckInOut por Ch_id: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Busca el registro de check-in/out asociado a un contrato específico.
     */
    public CheckInOut buscarPorContrato(int nroContrato) {
        String sql = "SELECT Ch_id, Ch_feHrSal, Ch_kmjSal, Ch_nivCombSal, Ch_daExist, "
                   + "Ch_feHrEnt, Ch_kmjEnt, Ch_nivCombEnt, Ch_daNuevos, nro_ParticipaSal, nro_ParticipaEnt, nro_Corresponde "
                   + "FROM checkInOuts WHERE nro_Corresponde = ? ORDER BY Ch_id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nroContrato);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearCheckInOut(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar CheckInOut por número de contrato: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Actualiza los campos que eran nulos al registrarse la devolución del vehículo:
     * Ch_feHrEnt, Ch_kmjEnt, Ch_nivCombEnt, Ch_daNuevos y nro_ParticipaEnt.
     */
    public void actualizarDevolucion(int nroContrato, String feHrEnt, int kmjEnt, double nivCombEnt, String daNuevos, int sucEnt) {
        String sql = "UPDATE checkInOuts "
                   + "SET Ch_feHrEnt = ?, Ch_kmjEnt = ?, Ch_nivCombEnt = ?, Ch_daNuevos = ?, nro_ParticipaEnt = ? "
                   + "WHERE nro_Corresponde = ? AND Ch_feHrEnt IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, feHrEnt);
            stmt.setInt(2, kmjEnt);
            stmt.setDouble(3, nivCombEnt);
            stmt.setString(4, (daNuevos != null && !daNuevos.trim().isEmpty()) ? daNuevos.trim() : "Ninguno");
            stmt.setInt(5, sucEnt);
            stmt.setInt(6, nroContrato);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar devolución en CheckInOut: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza todos los campos de un registro de CheckInOut existente.
     */
    public void actualizar(CheckInOut checkInOut) {
        String sql = "UPDATE checkInOuts "
                   + "SET Ch_feHrSal = ?, Ch_kmjSal = ?, Ch_nivCombSal = ?, Ch_daExist = ?, "
                   + "Ch_feHrEnt = ?, Ch_kmjEnt = ?, Ch_nivCombEnt = ?, Ch_daNuevos = ?, "
                   + "nro_ParticipaSal = ?, nro_ParticipaEnt = ?, nro_Corresponde = ? "
                   + "WHERE Ch_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, checkInOut.getFeHrSal());
            stmt.setInt(2, checkInOut.getKmjSal());
            stmt.setDouble(3, checkInOut.getNivCombSal());
            stmt.setString(4, checkInOut.getDaExist());
            if (checkInOut.getFeHrEnt() != null) {
                stmt.setString(5, checkInOut.getFeHrEnt());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }
            if (checkInOut.getKmjEnt() != null) {
                stmt.setInt(6, checkInOut.getKmjEnt());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            if (checkInOut.getNivCombEnt() != null) {
                stmt.setDouble(7, checkInOut.getNivCombEnt());
            } else {
                stmt.setNull(7, Types.DOUBLE);
            }
            if (checkInOut.getDaNuevos() != null) {
                stmt.setString(8, checkInOut.getDaNuevos());
            } else {
                stmt.setNull(8, Types.VARCHAR);
            }

            Integer nroSal = checkInOut.getNroParticipaSal() != null
                    ? checkInOut.getNroParticipaSal()
                    : checkInOut.getNroParticipa();
            stmt.setInt(9, nroSal != null ? nroSal : 1);

            if (checkInOut.getNroParticipaEnt() != null) {
                stmt.setInt(10, checkInOut.getNroParticipaEnt());
            } else {
                stmt.setNull(10, Types.INTEGER);
            }

            stmt.setInt(11, checkInOut.getNroCorresponde());
            stmt.setInt(12, checkInOut.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar CheckInOut: " + e.getMessage(), e);
        }
    }

    private CheckInOut mapearCheckInOut(ResultSet rs) throws SQLException {
        Integer kmjEnt = rs.getObject("Ch_kmjEnt") != null ? rs.getInt("Ch_kmjEnt") : null;
        Double nivCombEnt = rs.getObject("Ch_nivCombEnt") != null ? rs.getDouble("Ch_nivCombEnt") : null;
        Integer nroParticipaEnt = rs.getObject("nro_ParticipaEnt") != null ? rs.getInt("nro_ParticipaEnt") : null;

        Integer nroParticipaSal = null;
        try {
            nroParticipaSal = rs.getObject("nro_ParticipaSal") != null ? rs.getInt("nro_ParticipaSal") : null;
        } catch (SQLException e) {
            try {
                nroParticipaSal = rs.getObject("nro_Participa") != null ? rs.getInt("nro_Participa") : null;
            } catch (SQLException ignored) {
            }
        }

        return new CheckInOut(
            rs.getInt("Ch_id"),
            rs.getString("Ch_feHrSal"),
            rs.getInt("Ch_kmjSal"),
            rs.getDouble("Ch_nivCombSal"),
            rs.getString("Ch_daExist"),
            rs.getString("Ch_feHrEnt"),
            kmjEnt,
            nivCombEnt,
            rs.getString("Ch_daNuevos"),
            nroParticipaSal,
            nroParticipaEnt,
            rs.getInt("nro_Corresponde")
        );
    }
}
