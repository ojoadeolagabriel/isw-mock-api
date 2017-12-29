package com.interswitchgroup.proxy;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.interswitchgroup.util.consts.AppConstants;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MockServiceBaseVerticle extends AbstractVerticle {

    public String buildResponse(String responseCode, String responseMessage) {
        JsonObject object = new JsonObject().put("responseCode", responseCode).put("responseMessage", responseMessage);
        return object.toString();
    }

    public String successResponse(String altMessage) {
        JsonObject object = new JsonObject().put("responseCode", "90000").put("responseMessage", "Successful - " + altMessage);
        return object.toString();
    }

    public boolean validRouteType(String routeType) {
        switch (routeType.toLowerCase().trim()) {
            case "basic":
            case "mustache":
            case "freemarker":
            case "groovy":
                return true;
            default:
                return false;
        }
    }

    public Map<String, String> processHeaders(RoutingContext handler, String headers) {
        Map<String, String> headerDict = new HashMap<>();
        if (!StringUtils.isEmpty(headers)) {
            Iterable<String> map = Splitter.on(CharMatcher.anyOf(",;")).split(headers);
            for (String mapField : map) {
                try {
                    String[] parts = mapField.split("=");
                    handler.response().putHeader(parts[0], parts[1]);
                    headerDict.put(parts[0], parts[1]);
                } catch (Exception ex) {

                }
            }
        }
        return headerDict;
    }

    private boolean isBasicRouteType(String routeType) {
        switch (routeType.toLowerCase().trim()) {
            case "basic":
                return true;
            default:
                return false;
        }
    }

    public String processBody(Map<String, String> additionalHeaderMap, MultiMap originalHeaders, String templateBody, String requestType, String apiRequestBody) {
        switch (requestType) {
            case "mustache":
                return handleMustache(additionalHeaderMap, originalHeaders, templateBody, apiRequestBody);
            case "groovy":
                return handleGroovy(additionalHeaderMap, originalHeaders, templateBody, apiRequestBody);
            case "basic":
                return templateBody;
        }
        return apiRequestBody;
    }

    public static String toUpper(String str){
        return str.toUpperCase();
    }

    private String handleGroovy(Map<String, String> additionalHeaderMap, MultiMap originalHeaders, String templateBody, String requestBody) {
        Iterator<Map.Entry<String, String>> iterator = originalHeaders.iterator();
        Binding binding = new Binding();
        while (iterator.hasNext()) {
            Map.Entry<String, String> item = iterator.next();
            binding.setProperty(item.getKey(), item.getValue());
        }
        Set<Map.Entry<String, String>> entries = additionalHeaderMap.entrySet();
        for (Map.Entry<String, String> entry : entries){
            binding.setProperty(entry.getKey(), entry.getValue());
        }

        binding.setProperty(AppConstants.ResponseBody, requestBody);
        GroovyShell groovyShell = new GroovyShell(binding);
        String resp = groovyShell.evaluate(templateBody).toString();
        return resp;
    }

    private String handleMustache(Map<String, String> additionalHeaderMap, MultiMap originalHeaders, String templateBody, String apiRequestBody) {
        try {
            Iterator<Map.Entry<String, String>> iterator = originalHeaders.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> item = iterator.next();
                additionalHeaderMap.put(item.getKey(), item.getValue());
            }

            if (!StringUtils.isEmpty(apiRequestBody))
                additionalHeaderMap.put(AppConstants.ResponseBody, apiRequestBody);
            Template template = Mustache.compiler().compile(templateBody);
            String resp = template.execute(additionalHeaderMap);
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return templateBody;
    }

}
