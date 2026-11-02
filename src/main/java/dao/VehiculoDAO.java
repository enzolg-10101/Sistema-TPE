package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import modelo.Vehiculo;
import util.DatabaseConnection;

public class VehiculoDAO {

    /**
     * Normaliza el valor de transmisión para el ENUM('Manual','Automatico') de la base de datos.
     */
    private String normalizarTransmisionParaBd(String tran) {
        if (tran == null || tran.trim().isEmpty()) {
            return "Manual";
        }
        String t = tran.trim().toLowerCase();
        if (t.startsWith("auto")) {
            return "Automatico";
        }
        return "Manual";
    }

    /**
     * Formatea el valor de transmisión leído desde la BD para mostrar en la interfaz ("Manual" o "Automática").
     */
    private String formatearTransmisionDesdeBd(String tranBd) {
        if (tranBd == null || tranBd.trim().isEmpty()) {
            return "Manual";
        }
        if (tranBd.trim().toLowerCase().startsWith("auto")) {
            return "Automática";
        }
        return "Manual";
    }

    public void insertar(Vehiculo vehiculo) {
        String sqlConTran = "INSERT INTO Vehiculos (V_pat, V_marca, V_mod, V_tran, V_aFabr, V_fAdq, V_kmj, V_est, "
                          + "V_preAdq, V_fBaja, V_tiComb, T_carga, T_pax, T_tarBase, T_cat, cod_Pertenece) "
                          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlConTran, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, vehiculo.getPat());
            stmt.setString(2, vehiculo.getMarca());
            stmt.setString(3, vehiculo.getMod());
            stmt.setString(4, normalizarTransmisionParaBd(vehiculo.getTran()));
            stmt.setInt(5, vehiculo.getaFabr());
            stmt.setString(6, vehiculo.getfAdq());
            stmt.setInt(7, vehiculo.getKmj());
            stmt.setString(8, vehiculo.getEst());
            stmt.setDouble(9, vehiculo.getPreAdq());
            if (vehiculo.getfBaja() != null && !vehiculo.getfBaja().trim().isEmpty()) {
                stmt.setString(10, vehiculo.getfBaja());
            } else {
                stmt.setNull(10, Types.VARCHAR);
            }
            stmt.setString(11, vehiculo.getTiComb());
            stmt.setDouble(12, vehiculo.getCarga());
            stmt.setInt(13, vehiculo.getPax());
            stmt.setDouble(14, vehiculo.getTarBase());
            stmt.setString(15, vehiculo.getCat());
            stmt.setInt(16, vehiculo.getCodPertenece());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    vehiculo.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("v_tran")) {
                insertarSinColumnaTransmision(vehiculo);
            } else {
                throw new RuntimeException("Error al insertar el vehículo: " + e.getMessage(), e);
            }
        }
    }

    private void insertarSinColumnaTransmision(Vehiculo vehiculo) {
        String sql = "INSERT INTO Vehiculos (V_pat, V_marca, V_mod, V_aFabr, V_fAdq, V_kmj, V_est, "
                   + "V_preAdq, V_fBaja, V_tiComb, T_carga, T_pax, T_tarBase, T_cat, cod_Pertenece) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, vehiculo.getPat());
            stmt.setString(2, vehiculo.getMarca());
            stmt.setString(3, vehiculo.getMod());
            stmt.setInt(4, vehiculo.getaFabr());
            stmt.setString(5, vehiculo.getfAdq());
            stmt.setInt(6, vehiculo.getKmj());
            stmt.setString(7, vehiculo.getEst());
            stmt.setDouble(8, vehiculo.getPreAdq());
            if (vehiculo.getfBaja() != null && !vehiculo.getfBaja().trim().isEmpty()) {
                stmt.setString(9, vehiculo.getfBaja());
            } else {
                stmt.setNull(9, Types.VARCHAR);
            }
            stmt.setString(10, vehiculo.getTiComb());
            stmt.setDouble(11, vehiculo.getCarga());
            stmt.setInt(12, vehiculo.getPax());
            stmt.setDouble(13, vehiculo.getTarBase());
            stmt.setString(14, vehiculo.getCat());
            stmt.setInt(15, vehiculo.getCodPertenece());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    vehiculo.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar el vehículo: " + e.getMessage(), e);
        }
    }

    public void actualizar(Vehiculo vehiculo) {
        String sqlPorId = "UPDATE Vehiculos SET V_pat = ?, V_marca = ?, V_mod = ?, V_tran = ?, V_aFabr = ?, "
                        + "V_fAdq = ?, V_kmj = ?, V_est = ?, V_preAdq = ?, V_fBaja = ?, "
                        + "V_tiComb = ?, T_carga = ?, T_pax = ?, T_tarBase = ?, T_cat = ?, cod_Pertenece = ? "
                        + "WHERE V_id = ?";
        String sqlPorPat = "UPDATE Vehiculos SET V_marca = ?, V_mod = ?, V_tran = ?, V_aFabr = ?, "
                         + "V_fAdq = ?, V_kmj = ?, V_est = ?, V_preAdq = ?, V_fBaja = ?, "
                         + "V_tiComb = ?, T_carga = ?, T_pax = ?, T_tarBase = ?, T_cat = ?, cod_Pertenece = ? "
                         + "WHERE V_pat = ?";

        boolean tieneId = vehiculo.getId() != null;
        String sql = tieneId ? sqlPorId : sqlPorPat;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int idx = 1;
            if (tieneId) {
                stmt.setString(idx++, vehiculo.getPat());
            }
            stmt.setString(idx++, vehiculo.getMarca());
            stmt.setString(idx++, vehiculo.getMod());
            stmt.setString(idx++, normalizarTransmisionParaBd(vehiculo.getTran()));
            stmt.setInt(idx++, vehiculo.getaFabr());
            stmt.setString(idx++, vehiculo.getfAdq());
            stmt.setInt(idx++, vehiculo.getKmj());
            stmt.setString(idx++, vehiculo.getEst());
            stmt.setDouble(idx++, vehiculo.getPreAdq());
            if (vehiculo.getfBaja() != null && !vehiculo.getfBaja().trim().isEmpty()) {
                stmt.setString(idx++, vehiculo.getfBaja());
            } else {
                stmt.setNull(idx++, Types.VARCHAR);
            }
            stmt.setString(idx++, vehiculo.getTiComb());
            stmt.setDouble(idx++, vehiculo.getCarga());
            stmt.setInt(idx++, vehiculo.getPax());
            stmt.setDouble(idx++, vehiculo.getTarBase());
            stmt.setString(idx++, vehiculo.getCat());
            stmt.setInt(idx++, vehiculo.getCodPertenece());

            if (tieneId) {
                stmt.setInt(idx, vehiculo.getId());
            } else {
                stmt.setString(idx, vehiculo.getPat());
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("v_tran")) {
                actualizarSinColumnaTransmision(vehiculo);
            } else {
                throw new RuntimeException("Error al actualizar el vehículo: " + e.getMessage(), e);
            }
        }
    }

    private void actualizarSinColumnaTransmision(Vehiculo vehiculo) {
        String sqlPorId = "UPDATE Vehiculos SET V_pat = ?, V_marca = ?, V_mod = ?, V_aFabr = ?, "
                        + "V_fAdq = ?, V_kmj = ?, V_est = ?, V_preAdq = ?, V_fBaja = ?, "
                        + "V_tiComb = ?, T_carga = ?, T_pax = ?, T_tarBase = ?, T_cat = ?, cod_Pertenece = ? "
                        + "WHERE V_id = ?";
        String sqlPorPat = "UPDATE Vehiculos SET V_marca = ?, V_mod = ?, V_aFabr = ?, "
                         + "V_fAdq = ?, V_kmj = ?, V_est = ?, V_preAdq = ?, V_fBaja = ?, "
                         + "V_tiComb = ?, T_carga = ?, T_pax = ?, T_tarBase = ?, T_cat = ?, cod_Pertenece = ? "
                         + "WHERE V_pat = ?";

        boolean tieneId = vehiculo.getId() != null;
        String sql = tieneId ? sqlPorId : sqlPorPat;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int idx = 1;
            if (tieneId) {
                stmt.setString(idx++, vehiculo.getPat());
            }
            stmt.setString(idx++, vehiculo.getMarca());
            stmt.setString(idx++, vehiculo.getMod());
            stmt.setInt(idx++, vehiculo.getaFabr());
            stmt.setString(idx++, vehiculo.getfAdq());
            stmt.setInt(idx++, vehiculo.getKmj());
            stmt.setString(idx++, vehiculo.getEst());
            stmt.setDouble(idx++, vehiculo.getPreAdq());
            if (vehiculo.getfBaja() != null && !vehiculo.getfBaja().trim().isEmpty()) {
                stmt.setString(idx++, vehiculo.getfBaja());
            } else {
                stmt.setNull(idx++, Types.VARCHAR);
            }
            stmt.setString(idx++, vehiculo.getTiComb());
            stmt.setDouble(idx++, vehiculo.getCarga());
            stmt.setInt(idx++, vehiculo.getPax());
            stmt.setDouble(idx++, vehiculo.getTarBase());
            stmt.setString(idx++, vehiculo.getCat());
            stmt.setInt(idx++, vehiculo.getCodPertenece());

            if (tieneId) {
                stmt.setInt(idx, vehiculo.getId());
            } else {
                stmt.setString(idx, vehiculo.getPat());
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el vehículo: " + e.getMessage(), e);
        }
    }

    public Vehiculo buscarPorId(String pat) {
        String sql = "SELECT * FROM Vehiculos WHERE V_pat = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pat);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearVehiculo(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el vehículo por V_pat: " + e.getMessage(), e);
        }
        return null;
    }

    public Vehiculo buscarPorId(int id) {
        String sql = "SELECT * FROM Vehiculos WHERE V_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearVehiculo(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el vehículo por V_id: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Vehiculo> listarTodos() {
        List<Vehiculo> lista = new ArrayList<>();
        String sql = "SELECT * FROM Vehiculos ORDER BY V_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearVehiculo(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar vehículos: " + e.getMessage(), e);
        }
        return lista;
    }

    private Vehiculo mapearVehiculo(ResultSet rs) throws SQLException {
        String tran = "Manual";
        try {
            tran = formatearTransmisionDesdeBd(rs.getString("V_tran"));
        } catch (SQLException ignored) {
            // Si la tabla en la base de datos no tiene columna V_tran, usa Manual por defecto
        }

        return new Vehiculo(
            rs.getInt("V_id"),
            rs.getString("V_pat"),
            rs.getString("V_marca"),
            rs.getString("V_mod"),
            tran,
            rs.getInt("V_aFabr"),
            rs.getString("V_fAdq"),
            rs.getInt("V_kmj"),
            rs.getString("V_est"),
            rs.getDouble("V_preAdq"),
            rs.getString("V_fBaja"),
            rs.getString("V_tiComb"),
            rs.getDouble("T_carga"),
            rs.getInt("T_pax"),
            rs.getDouble("T_tarBase"),
            rs.getString("T_cat"),
            rs.getInt("cod_Pertenece")
        );
    }
}
