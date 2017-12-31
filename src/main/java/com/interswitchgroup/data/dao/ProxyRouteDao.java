package com.interswitchgroup.data.dao;

import com.interswitchgroup.proxy.MockContext;
import com.interswitchgroup.data.dto.Route;
import com.interswitchgroup.util.consts.AppConstants;
import com.interswitchgroup.util.io.StringUtility;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.StoreTransaction;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import static com.interswitchgroup.util.consts.AppConstants.*;

public class ProxyRouteDao {
    public static final String RouteSchema = "Route";

    public static List<Route> getRouteById(String id) {
        StoreTransaction txn = MockContext.store.beginTransaction();
        List<Route> routes = new ArrayList<>();
        try {
            EntityIterable entities = txn.findStartingWith(RouteSchema, AppConstants.RequestId, id);
            for (Entity entity : entities) {
                Route routeEntity = getRoute(entity);
                routes.add(routeEntity);
            }
        } catch (Exception e) {

        } finally {
            if (txn != null & !txn.isFinished()) {
                txn.abort();
            }
        }
        return routes;
    }

    public static List<Route> routes() {
        List<Route> routes = new ArrayList<>();
        StoreTransaction txn = MockContext.store.beginTransaction();
        try {
            EntityIterable route = txn.getAll(RouteSchema);
            for (Entity entity : route) {
                Route routeEntity = getRoute(entity);
                routes.add(routeEntity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (txn != null & !txn.isFinished()) {
                txn.abort();
            }
        }
        return routes;
    }

    @NotNull
    private static Route getRoute(Entity entity) {
        Route routeEntity = new Route();
        routeEntity.setPath(StringUtility.parseString(entity.getProperty(AppConstants.Path)));
        routeEntity.setVerb(StringUtility.parseString(entity.getProperty(AppConstants.Verb)));
        routeEntity.setHeaders(StringUtility.parseString(entity.getProperty(AppConstants.Headers)));
        routeEntity.setResponseStatus(StringUtility.parseString(entity.getProperty(AppConstants.ResponseStatus)));
        routeEntity.setContentType(StringUtility.parseString(entity.getProperty(AppConstants.ContentType)));
        routeEntity.setContentEncoding(StringUtility.parseString(entity.getProperty(AppConstants.ContentEncoding)));
        routeEntity.setResponseBody(StringUtility.parseString(entity.getProperty(AppConstants.ResponseBody)));
        routeEntity.setRequestId(StringUtility.parseString(entity.getProperty(AppConstants.RequestId)));
        routeEntity.setDelay(StringUtility.parseString(entity.getProperty(AppConstants.Delay)));
        routeEntity.setTimeout(StringUtility.parseString(entity.getProperty(AppConstants.TimeOut)));
        routeEntity.setRouteType(StringUtility.parseString(entity.getProperty(AppConstants.RouteType)));
        routeEntity.setCreatedBy(StringUtility.parseString(entity.getProperty(AppConstants.CreatedBy), "Sysadmin"));
        routeEntity.setEnvironment(StringUtility.parseString(entity.getProperty(Environment), "uat"));
        routeEntity.setGroup(StringUtility.parseString(entity.getProperty(Group), "default"));

        String dateTimeString = StringUtility.parseString(entity.getProperty(DateCreated), String.valueOf(DateTime.now().toDate().getTime()));
        routeEntity.setDateCreated(Long.valueOf(dateTimeString));
        return routeEntity;
    }

    public static boolean updateRoute(Route route) {
        StoreTransaction txn = MockContext.store.beginTransaction();
        try {
            EntityIterable entities = txn.find(RouteSchema, AppConstants.RequestId, route.getRequestId());
            if (entities != null && entities.size() == 1) {
                Entity first = entities.getFirst();
                //first.setProperty(AppConstants.Path, route.getPath());
                first.setProperty(AppConstants.Verb, route.getVerb());
                first.setProperty(AppConstants.ResponseStatus, route.getResponseStatus().toLowerCase());
                first.setProperty(AppConstants.RouteType, route.getRouteType().toLowerCase());
                //first.setProperty(AppConstants.Environment, route.getEnvironment().toLowerCase());
                //first.setProperty(AppConstants.ResponseBody, route.getResponseBody());
                txn.commit();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (txn != null & !txn.isFinished()) {
                txn.abort();
            }
        }
    }

    public static void persistRoute(String path, String verb, String headers, String status, String contentType,
                                    String contentEncoding, String responseBody, String requestId, int delay, int timeOut,
                                    String routeType, String createdBy, String group) {
        StoreTransaction txn = MockContext.store.beginTransaction();
        try {
            Entity route = txn.newEntity(RouteSchema);
            route.setProperty(AppConstants.Verb, verb);
            route.setProperty(AppConstants.Path, path);
            route.setProperty(AppConstants.Headers, headers);
            route.setProperty(AppConstants.ResponseStatus, status);
            route.setProperty(AppConstants.ContentType, contentType);
            route.setProperty(AppConstants.ContentEncoding, contentEncoding);
            route.setProperty(AppConstants.ResponseBody, responseBody);
            route.setProperty(AppConstants.RequestId, requestId);
            route.setProperty(AppConstants.Delay, delay);
            route.setProperty(AppConstants.TimeOut, timeOut);
            route.setProperty(AppConstants.RouteType, routeType);
            route.setProperty(DateCreated, DateTime.now().toDate().getTime());
            route.setProperty(CreatedBy, createdBy);
            route.setProperty(AppConstants.Group, group);
            txn.commit();
        } catch (Exception e) {

        } finally {
            if (txn != null & !txn.isFinished()) {
                txn.abort();
            }
        }
    }
}
