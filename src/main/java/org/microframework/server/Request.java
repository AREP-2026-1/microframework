package org.microframework.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an HTTP request with access to query parameters and request metadata.
 */
public class Request {

    private final String method;
    private final String path;
    private final Map<String, String> queryParams;
    private final Map<String, String> headers;

    public Request(String method, String path, Map<String, String> queryParams, Map<String, String> headers) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams != null ? queryParams : new HashMap<>();
        this.headers = headers != null ? headers : new HashMap<>();
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    /**
     * Returns the value of a query parameter by name.
     * Example: for URL /hello?name=Pedro, req.getValues("name") returns "Pedro"
     */
    public String getValues(String name) {
        return queryParams.getOrDefault(name, "");
    }

    public Map<String, String> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }

    public String getHeader(String name) {
        return headers.getOrDefault(name, "");
    }

    /**
     * Parses a query string like "name=Pedro&age=25" into a map.
     */
    public static Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();
        if (queryString == null || queryString.isEmpty()) {
            return params;
        }
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String value = idx < pair.length() - 1 ? pair.substring(idx + 1) : "";
                params.put(key, java.net.URLDecoder.decode(value, java.nio.charset.StandardCharsets.UTF_8));
            }
        }
        return params;
    }
}
