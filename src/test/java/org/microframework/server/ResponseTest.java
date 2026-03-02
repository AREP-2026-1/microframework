package org.microframework.server;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the Response class.
 */
public class ResponseTest {

    @Test
    public void testDefaultStatusCode() {
        Response res = new Response();
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void testSetStatusCode() {
        Response res = new Response();
        res.setStatusCode(404);
        assertEquals(404, res.getStatusCode());
    }

    @Test
    public void testDefaultContentType() {
        Response res = new Response();
        assertEquals("text/plain", res.getContentType());
    }

    @Test
    public void testSetContentType() {
        Response res = new Response();
        res.setContentType("application/json");
        assertEquals("application/json", res.getContentType());
    }

    @Test
    public void testSetHeader() {
        Response res = new Response();
        res.setHeader("X-Custom", "value");
        assertEquals("value", res.getHeaders().get("X-Custom"));
    }
}
