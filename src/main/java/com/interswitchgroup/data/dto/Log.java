package com.interswitchgroup.data.dto;

import org.joda.time.DateTime;

public class Log {
    private String logUrlInformation;
    private String logMessageExtra;
    private String logRequestData;

    public String getLogRequestData() {
        return logRequestData;
    }

    public void setLogRequestData(String logRequestData) {
        this.logRequestData = logRequestData;
    }

    public String getLogResponseData() {
        return logResponseData;
    }

    public void setLogResponseData(String logResponseData) {
        this.logResponseData = logResponseData;
    }

    private String logResponseData;

    public String getLogUrlInformation() {
        return logUrlInformation;
    }

    public void setLogUrlInformation(String logUrlInformation) {
        this.logUrlInformation = logUrlInformation;
    }

    public String getLogMessageExtra() {
        return logMessageExtra;
    }

    public void setLogMessageExtra(String logMessageExtra) {
        this.logMessageExtra = logMessageExtra;
    }

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
        return new DateTime(logDate).toLocalDateTime().toString("dd MMM, hh:mm:ss");
    }
}
