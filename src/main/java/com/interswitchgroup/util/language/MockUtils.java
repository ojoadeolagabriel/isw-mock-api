package com.interswitchgroup.util.language;

import org.apache.commons.lang3.StringUtils;

public class MockUtils {
    public static String toUpper(String request){
        if(!StringUtils.isEmpty(request)){
            return request.toUpperCase();
        }
        return request;
    }
}
