package org.microframework.server;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an HTTP response that can be customized by REST service handlers.
 */
public class Response {

    private int statusCode = 200;
    private String contentType = "text/plain";
    private final Map<String, String> headers = new HashMap<>();

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
