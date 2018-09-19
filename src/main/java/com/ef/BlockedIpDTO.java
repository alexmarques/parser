package com.ef;

import java.time.LocalDateTime;

public class BlockedIpDTO {

    private long id;
    private LocalDateTime requestTime;
    private String ip;
    private String reason;
    private LocalDateTime startDateParam;
    private String durationParam;
    private int thresholdParam;
    private int count;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getStartDateParam() {
        return startDateParam;
    }

    public void setStartDateParam(LocalDateTime startDateParam) {
        this.startDateParam = startDateParam;
    }

    public String getDurationParam() {
        return durationParam;
    }

    public void setDurationParam(String durationParam) {
        this.durationParam = durationParam;
    }

    public int getThresholdParam() {
        return thresholdParam;
    }

    public void setThresholdParam(int thresholdParam) {
        this.thresholdParam = thresholdParam;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        this.count++;
    }
}
