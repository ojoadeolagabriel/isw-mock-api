package com.interswitchgroup.util.generator;

public class NameGen {
    public static String generateUnloadRouteHandlerDescriptor(String prefix){
        return String.format("%s_unload_handler", prefix);
    }
    public static String generatePauseRouteHandlerDescriptor(String prefix){
        return String.format("%s_pause_handler", prefix);
    }
    public static String generateRouteStatusDescriptor(String prefix){
        return String.format("%s_status_handler", prefix);
    }
}
