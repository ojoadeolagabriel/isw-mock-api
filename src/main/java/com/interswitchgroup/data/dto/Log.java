package com.interswitchgroup.data.dto;

import org.joda.time.DateTime;

public class Log {
    private String logMessage;
    private String logType;

    public long getLogDate() {
        return logDate;
    }

    public void setLogDate(long logDate) {
        this.logDate = logDate;
    }

    private long logDate;

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getLogStackTrace() {
        return logStackTrace;
    }

    public void setLogStackTrace(String logStackTrace) {
        this.logStackTrace = logStackTrace;
    }

    private String logStackTrace;

    public String getDateTimeCreatedMessage() {
        return new DateTime(logDate).toLocalDateTime().toString("dd/MM/yyyy @ hh:mm:ss a");
    }
}
