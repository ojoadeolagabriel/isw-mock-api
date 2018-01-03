package com.interswitchgroup.api;

import org.springframework.http.HttpHeaders;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class BaseController {
    public HttpHeaders addContentType(HttpHeaders headers) {
        if (headers != null & !headers.containsKey("Content-type"))
            headers.add("Content-type", "application/json");
        return headers;
    }
    public String hostIp(){
        String ip = "";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ip;
    }
}
