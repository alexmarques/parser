package com.ef;

import java.sql.*;

public class BlockedIpsRepository {

    private static final String INSERT_STMT = "INSERT INTO record(request_time, ip, reason, start_date_param, duration, threshold) values(?,?,?,?,?,?)";

    /**
     * Save the record into the database and return the primary key
     * @param blockedIpDTO
     * @return
     */
    public int save(BlockedIpDTO blockedIpDTO) {

        try (
                Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(INSERT_STMT, Statement.RETURN_GENERATED_KEYS);
        ) {
            ps.setTimestamp(1, Timestamp.valueOf(blockedIpDTO.getRequestTime()));
            ps.setString(2, blockedIpDTO.getIp());
            ps.setString(3, blockedIpDTO.getReason());
            ps.setTimestamp(4, Timestamp.valueOf(blockedIpDTO.getStartDateParam()));
            ps.setString(5, blockedIpDTO.getDurationParam());
            ps.setInt(6, blockedIpDTO.getThresholdParam());
            ps.executeUpdate();
            conn.commit();
            ResultSet rs = ps.getGeneratedKeys();
            return rs.getInt(1);

        } catch (SQLException e1) {
            System.err.println(e1.getMessage());
        }
        return -1;
    }
}
