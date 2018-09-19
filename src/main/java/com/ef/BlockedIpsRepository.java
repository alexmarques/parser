package com.ef;

import java.sql.*;

public class BlockedIpsRepository {

    private static final String INSERT_STMT = "INSERT INTO BLOCKED_IPS(ip, reason, start_date_param, duration, threshold) values(?,?,?,?,?)";

    /**
     * Save the blocked ip into the database
     * @param blockedIpDTO
     * @return
     */
    public void save(BlockedIpDTO blockedIpDTO) {
        PreparedStatement ps = null;
        try {
            Connection conn = Database.getConnection();
            ps = conn.prepareStatement(INSERT_STMT);
            ps.setString(1, blockedIpDTO.getIp());
            ps.setString(2, blockedIpDTO.getReason());
            ps.setTimestamp(3, Timestamp.valueOf(blockedIpDTO.getStartDateParam()));
            ps.setString(4, blockedIpDTO.getDurationParam());
            ps.setInt(5, blockedIpDTO.getThresholdParam());
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException e1) {
            System.err.println(e1.getMessage());
        } finally {
            Database.closeQuietly(ps);
        }
    }
}
