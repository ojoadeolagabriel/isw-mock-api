package com.interswitchgroup.api;

import org.springframework.http.HttpHeaders;

public class BaseController {
    public HttpHeaders addContentType(HttpHeaders headers) {
        if (headers != null & !headers.containsKey("Content-type"))
            headers.add("Content-type", "application/json");
        return headers;
    }
}
