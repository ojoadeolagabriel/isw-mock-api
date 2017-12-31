package com.interswitchgroup.proxy;

import com.interswitchgroup.config.ComponentConfig;
import com.interswitchgroup.data.dao.LoggerRouteDao;
import com.interswitchgroup.data.dao.ProxyRouteDao;
import com.interswitchgroup.data.dto.Log;
import com.interswitchgroup.data.dto.Route;
import com.interswitchgroup.util.SpringContext;
import com.interswitchgroup.util.consts.AppConstants;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import org.joda.time.DateTime;

import java.util.List;

public class MockProxyGatewayInitializer {

    public static void init() {
        ComponentConfig componentConfig = SpringContext.getConfig();
        MockContext.init(componentConfig.mockPort, componentConfig.adminServerPort, componentConfig.xodusEntityStoreDirPath);
        List<Route> existingRoutes = ProxyRouteDao.routes();
        startExistingRoutes(existingRoutes, componentConfig.routeRuntimeDefaultEnvironment, componentConfig.processorsPerVerticle);
        startListeners(5000, componentConfig.routeRuntimeDefaultEnvironment);

        Log log = new Log();
        log.setLogType("process-restarted");
        log.setLogMessage("Instance restart was detected! kindly confirm from the system admin that this was the intended action [ no reason given]");
        log.setLogStackTrace("TRIGGERED_BY_SYS_ADMIN");
        log.setLogDate(DateTime.now().toDate().getTime());

        LoggerRouteDao.log(log);
    }

    private static void startListeners(int defaultListenerCounter, String routeRuntimeDefaultEnvironment) {
        JsonObject config = new JsonObject();
        config.put(AppConstants.Environment, routeRuntimeDefaultEnvironment);
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
