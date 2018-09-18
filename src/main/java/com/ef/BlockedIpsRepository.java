package com.ef;

import java.sql.*;

public class BlockedIpsRepository {

    private static final String INSERT_STMT = "INSERT INTO BLOCKED_IPS(request_time, ip, reason, start_date_param, duration, threshold) values(?,?,?,?,?,?)";

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
            ps.setTimestamp(1, Timestamp.valueOf(blockedIpDTO.getRequestTime()));
            ps.setString(2, blockedIpDTO.getIp());
            ps.setString(3, blockedIpDTO.getReason());
            ps.setTimestamp(4, Timestamp.valueOf(blockedIpDTO.getStartDateParam()));
            ps.setString(5, blockedIpDTO.getDurationParam());
            ps.setInt(6, blockedIpDTO.getThresholdParam());
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException e1) {
            System.err.println(e1.getMessage());
        } finally {
            Database.closeQuietly(ps);
        }
    }
}
