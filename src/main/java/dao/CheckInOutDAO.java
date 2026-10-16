package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import modelo.CheckInOut;
import util.DatabaseConnection;

public class CheckInOutDAO {

    public void insertar(CheckInOut checkInOut) {
        String sql = "INSERT INTO CheckInOuts (Ch_id, Ch_feHrSal, Ch_kmjSal, Ch_nivCombSal, Ch_daExist, "
                   + "Ch_feHrEnt, Ch_kmjEnt, Ch_nivCombEnt, Ch_daNuevos, nro_Participa, nro_Corresponde) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, checkInOut.getId());
            stmt.setString(2, checkInOut.getFeHrSal());
            stmt.setInt(3, checkInOut.getKmjSal());
            stmt.setDouble(4, checkInOut.getNivCombSal());
            stmt.setString(5, checkInOut.getDaExist());
            if (checkInOut.getFeHrEnt() != null) {
                stmt.setString(6, checkInOut.getFeHrEnt());
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }
            if (checkInOut.getKmjEnt() != null) {
                stmt.setInt(7, checkInOut.getKmjEnt());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            if (checkInOut.getNivCombEnt() != null) {
                stmt.setDouble(8, checkInOut.getNivCombEnt());
            } else {
                stmt.setNull(8, Types.DOUBLE);
            }
            if (checkInOut.getDaNuevos() != null) {
                stmt.setString(9, checkInOut.getDaNuevos());
            } else {
                stmt.setNull(9, Types.VARCHAR);
            }
            stmt.setInt(10, checkInOut.getNroParticipa());
            stmt.setInt(11, checkInOut.getNroCorresponde());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar el CheckInOut: " + e.getMessage(), e);
        }
    }

    public CheckInOut buscarPorId(int id) {
        String sql = "SELECT Ch_id, Ch_feHrSal, Ch_kmjSal, Ch_nivCombSal, Ch_daExist, "
                   + "Ch_feHrEnt, Ch_kmjEnt, Ch_nivCombEnt, Ch_daNuevos, nro_Participa, nro_Corresponde "
                   + "FROM CheckInOuts WHERE Ch_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Integer kmjEnt = rs.getObject("Ch_kmjEnt") != null ? rs.getInt("Ch_kmjEnt") : null;
                    Double nivCombEnt = rs.getObject("Ch_nivCombEnt") != null ? rs.getDouble("Ch_nivCombEnt") : null;
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
                        rs.getInt("nro_Participa"),
                        rs.getInt("nro_Corresponde")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el CheckInOut por Ch_id: " + e.getMessage(), e);
        }
        return null;
    }
}
