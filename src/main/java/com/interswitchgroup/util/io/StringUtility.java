package com.interswitchgroup.util.io;

import org.apache.commons.lang3.StringUtils;

public class StringUtility {
    public static String isEmpty(String data, String altData) {
        return StringUtils.isEmpty(data) ? altData : data;
    }

    public static String parseString(Comparable data) {
        try {
            return data.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String parseString(Comparable data, String defaultValue) {
        try {
            if (!StringUtils.isEmpty(data.toString()))
                return data.toString();
            return
                    defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
