package hydroflow.dao;

import hydroflow.db.DBConnection;
import hydroflow.db.DatabaseException;
import hydroflow.model.Settings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// TABLE: settings (one row per setting: key + value)
public class SettingsDAO {

    // reads every row of the settings table into a SETTINGS object
	
    public Settings load() {
        Settings settings = new Settings();
        String sql = "SELECT setting_key, setting_value FROM settings";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                apply(settings, rs.getString("setting_key"), rs.getString("setting_value"));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the settings.", e);
        }
        return settings;
    }

    private void apply(Settings s, String key, String value) {
        switch (key) {
            case "delivery_fee":           s.setDeliveryFee(Double.parseDouble(value)); break;
            case "deposit_per_container":  s.setDepositPerContainer(Double.parseDouble(value)); break;
            case "price_alkaline_round":   s.setAlkalineRound(Double.parseDouble(value)); break;
            case "price_alkaline_slim":    s.setAlkalineSlim(Double.parseDouble(value)); break;
            case "price_mineral_round":    s.setMineralRound(Double.parseDouble(value)); break;
            case "price_mineral_slim":     s.setMineralSlim(Double.parseDouble(value)); break;
            case "total_containers":       s.setTotalContainers(Integer.parseInt(value)); break;
            case "daily_sales_quota":      s.setDailySalesQuota(Double.parseDouble(value)); break;
            default: break;   // unknown key: ignore
        }
    }

    // saves all settings (REPLACE INTO = update the row, or insert it if it is missing)
    
    public void save(Settings s) {
        String sql = "REPLACE INTO settings (setting_key, setting_value) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            put(ps, "delivery_fee", String.valueOf(s.getDeliveryFee()));
            put(ps, "deposit_per_container", String.valueOf(s.getDepositPerContainer()));
            put(ps, "price_alkaline_round", String.valueOf(s.getAlkalineRound()));
            put(ps, "price_alkaline_slim", String.valueOf(s.getAlkalineSlim()));
            put(ps, "price_mineral_round", String.valueOf(s.getMineralRound()));
            put(ps, "price_mineral_slim", String.valueOf(s.getMineralSlim()));
            put(ps, "total_containers", String.valueOf(s.getTotalContainers()));
            put(ps, "daily_sales_quota", String.valueOf(s.getDailySalesQuota()));
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the settings.", e);
        }
    }

    private void put(PreparedStatement ps, String key, String value) throws SQLException {
        ps.setString(1, key);
        ps.setString(2, value);
        ps.executeUpdate();
    }
}
