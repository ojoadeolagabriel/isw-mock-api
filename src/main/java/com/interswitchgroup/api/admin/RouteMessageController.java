package com.interswitchgroup.api.admin;

import com.interswitchgroup.util.response.ResponseCodes;
import io.vertx.core.json.JsonObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("${route.admin.messages.url}")
@RestController
public class RouteMessageController {
    @RequestMapping(method = RequestMethod.GET)
    public String get(){
        return new JsonObject().put("responseCode", ResponseCodes.SUCCESS.code).toString();
    }
}
