package com.interswitchgroup.api.admin;

import com.interswitchgroup.data.dao.LoggerRouteDao;
import com.interswitchgroup.data.dao.ProxyRouteDao;
import com.interswitchgroup.data.dto.Log;
import com.interswitchgroup.data.dto.Route;
import com.interswitchgroup.proxy.MockContext;
import com.interswitchgroup.util.consts.AppConstants;
import com.interswitchgroup.util.generator.NameGen;
import com.interswitchgroup.util.response.ResponseCodes;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@RestController
@RequestMapping("${route.admin.url}")
public class RouteController {

    @RequestMapping(value = "/routes/{id}", method = RequestMethod.GET)
    public ResponseEntity<List<Route>> getRoutes(@PathVariable(name = "id") String id) {
        List<Route> routeById = ProxyRouteDao.getRouteById(id);
        return new ResponseEntity<>(routeById, HttpStatus.OK);
    }

    @RequestMapping(value = "/routes", method = RequestMethod.GET)
    public Future<ResponseEntity<List<Route>>> getRoutes() {
        List<Route> routes = ProxyRouteDao.routes();
        CompletableFuture<ResponseEntity<List<Route>>> future = new CompletableFuture<>();
        List<CompletableFuture<String>> tasks = new ArrayList<>();

        for (Route route : routes) {
            CompletableFuture<String> status = getStatus(route.getRequestId());
            tasks.add(status);

            status.whenComplete((b, q) -> {
                String statusStr = new JsonObject(b).getString(AppConstants.Status);
                route.setOnline(statusStr.equals("active") ? true : false);
            });
        }

        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[tasks.size()])).join();
        ResponseEntity<List<Route>> response = new ResponseEntity<>(routes, HttpStatus.OK);
        future.complete(response);
        return future;
    }

    //update route
    @ResponseBody
    @RequestMapping(value = "/routes", method = RequestMethod.PUT)
    public ResponseEntity<String> updateRoute(@RequestBody Route data) {
        if (!StringUtils.isEmpty(data.getRequestId())) {
            Route route = new Route();
            route.setRequestId(data.getRequestId());
            route.setRouteType(data.getRouteType());
            route.setVerb(data.getVerb().toLowerCase());
            route.setResponseStatus(data.getResponseStatus());
            route.setResponseBody(data.getResponseBody());
            boolean isSuccess = ProxyRouteDao.updateRoute(route);

            JsonObject jsonRequest = JsonObject.mapFrom(data);
            Log log = new Log();
            log.setLogType("route-update");
            log.setLogMessage("API call to update route detected : " + jsonRequest.toString());
            log.setLogStackTrace("TRIGGERED_BY_SYS_ADMIN");
            log.setLogDate(DateTime.now().toDate().getTime());
            LoggerRouteDao.log(log);

            String msg = new JsonObject().put(AppConstants.ResponseCode, isSuccess ? ResponseCodes.SUCCESS.code : ResponseCodes.NOT_FOUND.code).toString();
            return new ResponseEntity<>(msg, HttpStatus.OK);
        } else {
            String msg = new JsonObject().put(AppConstants.ResponseCode, ResponseCodes.NOT_PROCESSABLE.code).toString();
            return new ResponseEntity<>(msg, HttpStatus.OK);
        }
    }

    public CompletableFuture<String> getStatus(String id) {
        CompletableFuture<String> resp = new CompletableFuture<>();
        String statId = NameGen.generateRouteStatusDescriptor(id);
        MockContext.vertx.eventBus().send(statId, "", new DeliveryOptions().setSendTimeout(1000), handler -> {
            if (handler.succeeded()) {
                Message<Object> result = handler.result();
                Object body = result.body();
                resp.complete(body.toString());
            } else {
                resp.complete("inactive");
            }
        });
        return resp;
    }

    @RequestMapping(value = "/call-home", method = RequestMethod.GET)
    public ResponseEntity<String> callHome() {
        String data = new JsonObject() {{
            put("responseCode", "90000");
        }}.toString();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-type", "application/json");
        return new ResponseEntity<String>(data, responseHeaders, HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/routes/disable")
    public String disableRoute(@RequestParam(name = "id") String routeId, HttpServletResponse response) {
        MockContext.vertx.eventBus().publish(NameGen.generatePauseRouteHandlerDescriptor(routeId), "disable");
        response.addHeader("Content-type", "application/json");

        Log log = new Log();
        log.setLogType("disable-route-event");
        log.setLogMessage("API call to [disable route] detected : " + routeId);
        log.setLogStackTrace("TRIGGERED_BY_SYS_ADMIN");
        log.setLogDate(DateTime.now().toDate().getTime());
        LoggerRouteDao.log(log);

        return new JsonObject()
                .put("responseCode", ResponseCodes.SUCCESS.code)
                .put("responseMessage", "Successfully processed request").toString();
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/routes/enable")
    public String enableRoute(@RequestParam(name = "id") String routeId, HttpServletResponse response) {
        MockContext.vertx.eventBus().publish(NameGen.generatePauseRouteHandlerDescriptor(routeId), "enable");
        response.addHeader("Content-type", "application/json");

        Log log = new Log();
        log.setLogType("enable-route-event");
        log.setLogMessage("API call to [enable route] detected : " + routeId);
        log.setLogStackTrace("TRIGGERED_BY_SYS_ADMIN");
        log.setLogDate(DateTime.now().toDate().getTime());
        LoggerRouteDao.log(log);

        return new JsonObject()
                .put("responseCode", ResponseCodes.SUCCESS.code)
                .put("responseMessage", "Successfully processed request").toString();
    }
}
