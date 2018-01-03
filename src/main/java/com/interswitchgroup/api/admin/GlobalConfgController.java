package com.interswitchgroup.api.admin;

import com.interswitchgroup.App;
import com.interswitchgroup.api.BaseController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("${route.admin.url}")
@RestController
public class GlobalConfgController extends BaseController {
    @RequestMapping("/config")
    public ResponseEntity<String> get(){
        HttpHeaders responseHeaders = new HttpHeaders();
        addContentType(responseHeaders);
        return new ResponseEntity<>(App.adhocConfig.toString(), responseHeaders, HttpStatus.OK);
    }
}
