package com.interswitchgroup.proxy;

import com.interswitchgroup.config.ComponentConfig;
import com.interswitchgroup.data.dao.ProxyRouteDao;
import com.interswitchgroup.data.dto.Route;
import com.interswitchgroup.util.SpringContext;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class MockProxyGatewayInitializer {

    public static void init() {
        ComponentConfig componentConfig = SpringContext.getConfig();
        MockContext.init(componentConfig.mockPort, componentConfig.adminServerPort, componentConfig.xodusEntityStoreDirPath);
        List<Route> existingRoutes = ProxyRouteDao.routes();
        startExistingRoutes(existingRoutes, componentConfig.routeRuntimeDefaultEnvironment, componentConfig.processorsPerVerticle
        );
        startListeners(5000, componentConfig.routeRuntimeDefaultEnvironment);
    }

    private static void startListeners(int defaultListenerCounter, String routeRuntimeDefaultEnvironment) {
        JsonObject config = new JsonObject();
        config.put("Environment", routeRuntimeDefaultEnvironment);
        MockContext.vertx.deployVerticle(RestMockServiceVerticle.class.getName(),
                new DeploymentOptions()
                        .setConfig(config)
                        .setInstances(defaultListenerCounter));
    }

    private static void startExistingRoutes(List<Route> existingRoutes, String routeRuntimeDefaultEnvironment, int processorsPerVerticle) {
        for (Route route : existingRoutes) {
            JsonObject config = new JsonObject();
            config.put("Route", JsonObject.mapFrom(route));
            config.put("Environment", routeRuntimeDefaultEnvironment);

            MockContext.vertx.deployVerticle(RestMockServiceVerticle.class.getName(), new DeploymentOptions().setInstances(processorsPerVerticle).setConfig(config), handler -> {
                if (handler.succeeded()) {

                }
            });
        }
    }
}
