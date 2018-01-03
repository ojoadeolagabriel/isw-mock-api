package com.interswitchgroup.web;

import com.interswitchgroup.config.ComponentConfig;
import com.interswitchgroup.data.dao.HttpStatusCodeDao;
import com.interswitchgroup.data.dao.ProxyRouteDao;
import com.interswitchgroup.data.dto.HttpStatusCodeInfo;
import com.interswitchgroup.data.dto.Route;
import com.interswitchgroup.proxy.MockContext;
import com.interswitchgroup.util.generator.NameGen;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Controller
@RequestMapping("/web/isw-api-mock/public")
public class HomeController {

    @Value("${route.admin.url}")
    String routeAdminUrl;

    @Autowired
    ComponentConfig componentConfig;
    @Autowired
    HttpStatusCodeDao httpStatusCodeDao;

    @RequestMapping("/login")
    public String login(){
        return "login";
    }

    @RequestMapping("/home")
    public Future<ModelAndView> home(@RequestParam(value = "btnFilterRoute", required = false) String filterCriteria){
        CompletableFuture<ModelAndView> future = new CompletableFuture<>();
        List<CompletableFuture<String>> tasks = new ArrayList<>();

        ModelAndView modelAndView = new ModelAndView("index");
        String ip = "<host_url>";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        modelAndView.addObject("routesUrl", "http://" + ip + ":" + componentConfig.adminServerPort + routeAdminUrl + "/routes");
        List<HttpStatusCodeInfo> codes = httpStatusCodeDao.codes();
        modelAndView.addObject("httpStatusCode", codes);

        List<Route> routes;
        if(StringUtils.isEmpty(filterCriteria)) {
            routes = listRoutes();
            modelAndView.addObject("data", routes);
        }else{
            routes = listRoutes(filterCriteria);
            modelAndView.addObject("data", routes);
        }

        for (Route route : routes) {
            CompletableFuture<String> status = getStatus(route.getRequestId());
            tasks.add(status);

            status.whenComplete((b, q) -> {
                String statusStr = new JsonObject(b).getString("status");
                route.setOnline(statusStr.equals("active") ? true : false);
            });
        }

        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[tasks.size()])).join();
        future.complete(modelAndView);
        return future;
    }

    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public String error() {
        return "error";
    }

    public CompletableFuture<String> getStatus(String id) {
        CompletableFuture<String> resp = new CompletableFuture<>();
        String statId = NameGen.generateRouteStatusDescriptor(id);
        MockContext.vertx.eventBus().send(statId, "", new DeliveryOptions().setSendTimeout(500), handler -> {
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

    @ModelAttribute("listRoutes")
    public List<Route> listRoutes () {
        return ProxyRouteDao.routes();
    }

    @ModelAttribute("listRoutesById")
    public List<Route> listRoutes (String id) {
        return ProxyRouteDao.getRouteById(id);
    }
}
