package com.interswitchgroup.proxy;

import com.interswitchgroup.data.dao.ProxyRouteDao;
import com.interswitchgroup.util.SpringContext;
import com.interswitchgroup.util.consts.AppConstants;
import com.interswitchgroup.util.generator.NameGen;
import com.interswitchgroup.util.response.ResponseCodes;
import com.interswitchgroup.util.io.StringUtility;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.NumberUtils;

import java.util.Map;

public class RestMockServiceVerticle extends MockServiceBaseVerticle {
    String path = "/isw-mock/api/v1/routes";
    String deploymentId;
    Router subRoute;
    Logger logger = LoggerFactory.getLogger(RestMockServiceVerticle.class);
    String environment = "uat";
    Route activeProcessingRoute;
    boolean isRouteEnabled;
    String maskedFields;

    @Override
    public void start() throws Exception {
        deploymentId = deploymentID();
        subRoute = Router.router(vertx);
        JsonObject jsonRoute = (JsonObject) config().getValue("Route");
        environment = StringUtility.isEmpty(config().getString("Environment"), "uat");

        if (jsonRoute == null) {
            subRoute.post(path).blockingHandler(handler -> {
                String path = StringUtility.isEmpty(handler.request().getHeader(AppConstants.Path), null);
                String verb = StringUtility.isEmpty(handler.request().getHeader(AppConstants.Verb), "get");
                String headers = StringUtility.isEmpty(handler.request().getHeader(AppConstants.Headers), "");
                String status = StringUtility.isEmpty(handler.request().getHeader(AppConstants.ResponseStatus), String.valueOf(HttpStatus.OK.value()));
                String contentType = StringUtility.isEmpty(handler.request().getHeader(AppConstants.HeaderContentType), "application/json");
                String contentEncoding = StringUtility.isEmpty(handler.request().getHeader(AppConstants.ContentEncoding), "UTF-8");
                String routeType = StringUtility.isEmpty(handler.request().getHeader(AppConstants.RouteType), "Basic");
                String env = StringUtility.isEmpty(handler.request().getHeader(AppConstants.Environment), environment);
                String createdBy = StringUtility.isEmpty(handler.request().getHeader(AppConstants.CreatedBy), "Sysadmin");
                String group = StringUtility.isEmpty(handler.request().getHeader(AppConstants.Group), "default");
                maskedFields = StringUtility.isEmpty(handler.request().getHeader(AppConstants.MaskedFields), "");

                String responseBody;
                String requestId = handler.request().getHeader(AppConstants.RequestId);
                responseBody = handler.getBodyAsString();
                int delay = 1;
                int timeOut = 10000;

                boolean isValid = verifyThenTriggerRoute(handler, path, verb, headers, status, contentType, contentEncoding, responseBody, requestId, delay, timeOut, routeType, env);
                if (isValid) {
                    //prep path
                    if (!path.startsWith("/"))
                        path = "/".concat(path);
                    if (path.startsWith("//"))
                        path = path.replace("//", "/");

                    persistProxyRoute(path, verb, headers, status, contentType, contentEncoding,
                            responseBody, requestId, delay, timeOut, routeType, env, createdBy, group);
                    int mockPort = SpringContext.getConfig().mockPort;
                    String response = buildResponse("9000",
                            String.format("Route at url - [ http://localhost:%d/%s%s ] is now available", mockPort, environment, path));

                    handler.response().setStatusCode(HttpStatus.CREATED.value());
                    handler.response().putHeader("Content-type", "application/json");
                    handler.response().end(response);
                }
            });
            MockContext.router.mountSubRouter("/" + environment, subRoute);
        } else {
            initiateProxyRoute(jsonRoute.getString("path"),
                    jsonRoute.getString("verb"),
                    jsonRoute.getString("headers"),
                    jsonRoute.getString("responseStatus"),
                    jsonRoute.getString("contentType"),
                    jsonRoute.getString("contentEncoding"),
                    jsonRoute.getString("responseBody"),
                    jsonRoute.getString("requestId"),
                    1,
                    10000,
                    jsonRoute.getString("routeType"), jsonRoute.getString("environment"));
            logger.debug(String.format("initialized default route: " + jsonRoute.getString("requestId")));
        }
    }

    /**
     * service bus consumer to pause route.
     *
     * @param pauseHandler
     */
    private void processUnload(Message<Object> pauseHandler) {
        if (activeProcessingRoute != null) {
            if (pauseHandler.body() != null) {
                String action = String.valueOf(pauseHandler.body());
                switch (action) {
                    case "enable":
                        activeProcessingRoute.enable();
                        isRouteEnabled = true;
                        break;
                    default:
                    case "disable":
                        activeProcessingRoute.disable();
                        isRouteEnabled = false;
                        break;
                }
            }
        } else {
            activeProcessingRoute.disable();
            isRouteEnabled = false;
        }
    }

    /**
     * verify if route creation request is valid
     *
     * @param handler
     * @param path
     * @param verb
     * @param headers
     * @param status
     * @param contentType
     * @param contentEncoding
     * @param responseBody
     * @param requestId
     * @param delay
     * @param timeOut
     * @param routeType
     * @param env
     * @return
     */
    private boolean verifyThenTriggerRoute(RoutingContext handler, String path, String verb, String headers, String status,
                                           String contentType, String contentEncoding, String responseBody, String requestId,
                                           int delay, int timeOut, String routeType, String env) {
        handler.response().putHeader("Content-type", "application/json");
        if (StringUtils.isEmpty(requestId)) {
            handler.response().end(new JsonObject() {{
                put("responseCode", ResponseCodes.NOT_PERMITTED.code);
                put("responseMessage", "Failed - request not permitted (request id cannot be empty)");
            }}.toString());
        } else if (StringUtils.isEmpty(path)) {
            handler.response().end(new JsonObject() {{
                put("responseCode", ResponseCodes.NOT_PERMITTED.code);
                put("responseMessage", "Failed - request not permitted (path cannot be empty)");
            }}.toString());
        } else if (ProxyRouteDao.getRouteById(requestId).size() > 0) {
            handler.response().end(new JsonObject() {{
                put("responseCode", ResponseCodes.NOT_PERMITTED.code);
                put("responseMessage", String.format("Failed - request not permitted (route with id:%s already exists)", requestId));
            }}.toString());
        } else if (!validateEnvironment(env)) {
            handler.response().end(new JsonObject() {{
                put("responseCode", ResponseCodes.NOT_PERMITTED.code);
                put("responseMessage", String.format("Failed - request not permitted (specified environment %s is not permitted)", env));
            }}.toString());
        } else if (!validRouteType(routeType)) {
            handler.response().end(new JsonObject() {{
                put("responseCode", ResponseCodes.NOT_PROCESSABLE.code);
                put("responseMessage", String.format("Failed - request not processable (route type provided is not valid)", requestId));
            }}.toString());
        } else if (!validateContentType(contentType)) {
            handler.response().end(new JsonObject() {{
                put("responseCode", ResponseCodes.NOT_PERMITTED.code);
                put("responseMessage", String.format("Failed - request not processable (content type provided is not valid)", requestId));
            }}.toString());
        } else {
            initiateProxyRoute(path, verb, headers, status, contentType, contentEncoding, responseBody, requestId, delay, timeOut, routeType, env);
            return true;
        }
        return false;
    }

    private boolean validateContentType(String contentType) {
        switch (contentType) {
            case "application/xml":
            case "application/json":
            case "text/xml":
            case "text/html":
            case "text/csv":
            case "text/plain":
                return true;
            default:
                return false;

        }
    }

    /**
     * validate environment identifier
     *
     * @param env
     * @return
     */
    public boolean validateEnvironment(String env) {
        switch (env) {
            case "uat":
            case "staging":
            case "techop-uat":
            case "techop-staging":
                return true;
            default:
                return false;
        }
    }

    /**
     * persist route
     *  @param path
     * @param verb
     * @param headers
     * @param status
     * @param contentType
     * @param contentEncoding
     * @param responseBody
     * @param requestId
     * @param delay
     * @param timeOut
     * @param routeType
     * @param env
     * @param createdBy
     * @param group
     */
    private void persistProxyRoute(String path, String verb, String headers, String status, String contentType, String contentEncoding, String responseBody, String requestId, int delay, int timeOut, String routeType, String env, String createdBy, String group) {
        ProxyRouteDao.persistRoute(path, verb, headers, status, contentType, contentEncoding,
                responseBody, requestId, delay, timeOut, routeType, createdBy, group);
    }

    private void initiateProxyRoute(String path, String verb, String headers, String status, String contentType,
                                    String contentEncoding, String responseBody, String requestId, int delay, int timeOut, String requestType, String environment) {
        switch (verb) {
            default:
            case "get":
                initGetProcessor(path, headers, status, contentType, contentEncoding, responseBody, requestId, delay, timeOut, requestType, environment);
                break;
            case "post":
                initPostProcessor(path, headers, status, contentType, contentEncoding, responseBody, requestId, delay, timeOut, requestType, environment);
                break;
            case "put":
                initPutProcessor(path, headers, status, contentType, contentEncoding, responseBody, requestId, delay, timeOut, requestType, environment);
                break;
            case "delete":
                initDeleteProcessor(path, headers, status, contentType, contentEncoding, responseBody, requestId, delay, timeOut, requestType, environment);
                break;
        }

        isRouteEnabled = true;
    }

    private void initDeleteProcessor(String path, String headers, String status, String contentType, String contentEncoding, String responseBody, String requestId, int delay, int timeOut, String requestType, String environment) {
        vertx.setTimer(delay, delayHandler -> {
            activeProcessingRoute = subRoute.delete(path).blockingHandler(handler -> {
                handler.response().putHeader("Content-type", contentType);
                handler.response().putHeader("Content-encoding", contentEncoding);
                handler.response().setStatusCode(NumberUtils.parseNumber(status, Integer.class));
                processHeaders(handler, headers);
                if (StringUtils.isEmpty(responseBody)) {
                    handler.response().end(successResponse("no body set"));
                } else {
                    handler.response().end(responseBody);
                }
            });
            MockContext.router.mountSubRouter("/" + environment, subRoute);
        });

        vertx.eventBus().consumer(NameGen.generatePauseRouteHandlerDescriptor(requestId), pauseHandler -> processUnload(pauseHandler));
        vertx.eventBus().consumer(NameGen.generateRouteStatusDescriptor(requestId), pauseHandler -> processStatus(pauseHandler));
    }

    private void initPutProcessor(String path, String headers, String status, String contentType, String contentEncoding, String responseBody, String requestId, int delay, int timeOut, String requestType, String environment) {
        vertx.setTimer(delay, delayHandler -> {
            activeProcessingRoute = subRoute.put(path).blockingHandler(handler -> {
                handler.response().putHeader("Content-type", contentType);
                handler.response().putHeader("Content-encoding", contentEncoding);
                handler.response().setStatusCode(NumberUtils.parseNumber(status, Integer.class));

                Map<String, String> additionalHeaderMap = processHeaders(handler, headers);
                MultiMap originalHeaders = handler.request().headers();

                if (StringUtils.isEmpty(responseBody)) {
                    handler.response().end(successResponse("no body set"));
                } else {
                    String finalBody = processBody(additionalHeaderMap, originalHeaders, responseBody, requestType, "");
                    handler.response().end(finalBody);
                }
            });
            MockContext.router.mountSubRouter("/" + environment, subRoute);
        });

        vertx.eventBus().consumer(NameGen.generatePauseRouteHandlerDescriptor(requestId), pauseHandler -> processUnload(pauseHandler));
        vertx.eventBus().consumer(NameGen.generateRouteStatusDescriptor(requestId), pauseHandler -> processStatus(pauseHandler));
    }

    private void initGetProcessor(String path, String headers, String status, String contentType, String contentEncoding,
                                  String responseBody, String requestId, int delay, int timeOut, String requestType, String environment) {
        vertx.setTimer(delay, timerHandler -> {
            activeProcessingRoute = subRoute.get(path).blockingHandler(handler -> {
                handler.response().putHeader("Content-type", contentType);
                handler.response().putHeader("Content-encoding", contentEncoding);
                handler.response().setStatusCode(NumberUtils.parseNumber(status, Integer.class));

                Map<String, String> additionalHeaderMap = processHeaders(handler, headers);
                MultiMap originalHeaders = handler.request().headers();
                processBody(additionalHeaderMap, originalHeaders, responseBody, requestType, "");
                handler.response().end();
            });
            MockContext.router.mountSubRouter("/" + environment, subRoute);
        });

        //init load consumer.
        vertx.eventBus().consumer(NameGen.generatePauseRouteHandlerDescriptor(requestId), pauseHandler -> processUnload(pauseHandler));
        vertx.eventBus().consumer(NameGen.generateRouteStatusDescriptor(requestId), pauseHandler -> processStatus(pauseHandler));
    }

    private void initPostProcessor(String path, String headers, String status, String contentType, String contentEncoding,
                                   String responseBody, String requestId, int delay, int timeOut, String requestType, String environment) {
        vertx.setTimer(delay, delayHandler -> {
            activeProcessingRoute = subRoute.post(path).blockingHandler(handler -> {
                handler.response().putHeader("Content-type", contentType);
                handler.response().putHeader("Content-encoding", contentEncoding);
                handler.response().setStatusCode(NumberUtils.parseNumber(status, Integer.class));
                String body = handler.getBodyAsString();

                Map<String, String> additionalHeaderMap = processHeaders(handler, headers);
                MultiMap originalHeaders = handler.request().headers();

                if (StringUtils.isEmpty(responseBody)) {
                    handler.response().end(successResponse("no body set"));
                } else {
                    String finalBody = processBody(additionalHeaderMap, originalHeaders, responseBody, requestType, body);
                    handler.response().end(finalBody);
                }
            });

            //init sub router
            MockContext.router.mountSubRouter("/" + environment, subRoute);
            //init load consumer.
            vertx.eventBus().consumer(NameGen.generatePauseRouteHandlerDescriptor(requestId), pauseHandler -> processUnload(pauseHandler));
            vertx.eventBus().consumer(NameGen.generateRouteStatusDescriptor(requestId), pauseHandler -> processStatus(pauseHandler));
        });
    }

    private void processStatus(Message<Object> pauseHandler) {
        if (isRouteEnabled) {
            pauseHandler.reply(new JsonObject().put(AppConstants.ResponseCode, ResponseCodes.SUCCESS.code).put(AppConstants.Status, "active"));
        } else {
            pauseHandler.reply(new JsonObject().put(AppConstants.ResponseCode, ResponseCodes.SUCCESS.code).put(AppConstants.Status, "inactive"));
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
