package com.interswitchgroup.util.consts;

public enum LogType {
    ENABLE_ROUTE("enable-route-event", "route has been enabled"),
    STARTING_SERVICE("process-restarted", "service start/restart detected."),
    UPDATE_ROUTE_INFORMATION("route-info-updated", "route information has been updated!"),
    DISABLE_ROUTE("disable-route-event", "route has been disabled");

    public String getLogType() {
        return logType;
    }

    String logType, defaultMessage;

    LogType(String logType, String defaultMessage) {
        this.logType = logType;
        this.defaultMessage = defaultMessage;
    }
}
