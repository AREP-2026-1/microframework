package org.microframework.server;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for the Request class and query parameter parsing.
 */
public class RequestTest {

    @Test
    public void testGetValuesReturnsQueryParam() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "Pedro");
        Request req = new Request("GET", "/hello", params, null);

        assertEquals("Pedro", req.getValues("name"));
    }

    @Test
    public void testGetValuesReturnsEmptyForMissingParam() {
        Request req = new Request("GET", "/hello", new HashMap<>(), null);

        assertEquals("", req.getValues("name"));
    }

    @Test
    public void testParseQueryStringSimple() {
        Map<String, String> params = Request.parseQueryString("name=Pedro");
        assertEquals("Pedro", params.get("name"));
    }

    @Test
    public void testParseQueryStringMultipleParams() {
        Map<String, String> params = Request.parseQueryString("name=Pedro&age=25&city=Bogota");
        assertEquals("Pedro", params.get("name"));
        assertEquals("25", params.get("age"));
        assertEquals("Bogota", params.get("city"));
    }

    @Test
    public void testParseQueryStringEmpty() {
        Map<String, String> params = Request.parseQueryString("");
        assertTrue(params.isEmpty());
    }

    @Test
    public void testParseQueryStringNull() {
        Map<String, String> params = Request.parseQueryString(null);
        assertTrue(params.isEmpty());
    }

    @Test
    public void testParseQueryStringEncodedValue() {
        Map<String, String> params = Request.parseQueryString("name=Hello+World");
        // URLDecoder converts + to space
        assertEquals("Hello World", params.get("name"));
    }

    @Test
    public void testGetMethod() {
        Request req = new Request("GET", "/test", null, null);
        assertEquals("GET", req.getMethod());
    }

    @Test
    public void testGetPath() {
        Request req = new Request("GET", "/hello", null, null);
        assertEquals("/hello", req.getPath());
    }

    @Test
    public void testGetHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "text/html");
        Request req = new Request("GET", "/test", null, headers);

        assertEquals("text/html", req.getHeader("content-type"));
    }

    @Test
    public void testGetHeaderReturnsEmptyForMissing() {
        Request req = new Request("GET", "/test", null, null);
        assertEquals("", req.getHeader("x-custom"));
    }

    @Test
    public void testQueryParamsAreUnmodifiable() {
        Map<String, String> params = new HashMap<>();
        params.put("key", "value");
        Request req = new Request("GET", "/test", params, null);

        try {
            req.getQueryParams().put("another", "val");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
}
