package com.interswitchgroup.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Route {
    private boolean isOnline;
    private String path;
    private String verb;
    private String headers;
    private String responseStatus;
    private String contentType;
    private String contentEncoding;
    private String responseBody;
    private String requestId;
    private String delay;
    private String createdBy;
    private long dateCreated;
    private String environment;
    private String routeType;
    private String timeout;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    private String group;

    public String getResponseMessage() {
        try{
            return HttpStatus.valueOf(Integer.parseInt(responseStatus)).name();
        }catch(Exception e){
            return "";
        }
    }

    public String getDateCreatedMessage() {
        return new DateTime(dateCreated).toLocalDate().toString();
    }

    public String getDateTimeCreatedMessage() {
        return new DateTime(dateCreated).toLocalDateTime().toString("dd/MM/yyyy @ hh:mm:ss a");
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
