package com.interswitchgroup.api.admin;

import com.interswitchgroup.api.BaseController;
import com.interswitchgroup.data.dao.LoggerRouteDao;
import com.interswitchgroup.data.dto.Log;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${route.admin.url}/logs")
public class LogController extends BaseController {
    @RequestMapping("/log-detail")
    public ResponseEntity<Log> getDetail(@RequestParam (required = true, name = "uuid") String uuid){
        Log log = LoggerRouteDao.fetchByGuid(uuid);
        HttpHeaders httpHeaders = new HttpHeaders();
        addContentType(httpHeaders);
        return new ResponseEntity<>(log, httpHeaders, HttpStatus.OK);
    }
}
