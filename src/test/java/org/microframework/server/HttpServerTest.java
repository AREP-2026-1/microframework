package org.microframework.server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * Integration tests for the HttpServer.
 * Tests REST service routing, query parameter handling, and static file serving.
 */
public class HttpServerTest {

    private HttpServer server;
    private Thread serverThread;
    private static final int TEST_PORT = 8085;

    @Before
    public void setUp() throws Exception {
        server = new HttpServer(TEST_PORT);

        // Register test routes
        server.addGetRoute("/hello", (req, res) -> "Hello " + req.getValues("name"));
        server.addGetRoute("/pi", (req, res) -> String.valueOf(Math.PI));
        server.addGetRoute("/echo", (req, res) -> "method=" + req.getMethod() + ",path=" + req.getPath());

        server.setStaticFilesPath("/webroot");

        serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                // Server stopped
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        // Give the server a moment to start
        Thread.sleep(500);
    }

    @After
    public void tearDown() {
        server.stop();
    }

    @Test
    public void testHelloEndpoint() throws Exception {
        String response = httpGet("http://localhost:" + TEST_PORT + "/hello?name=Pedro");
        assertEquals("Hello Pedro", response);
    }

    @Test
    public void testHelloEndpointWithoutName() throws Exception {
        String response = httpGet("http://localhost:" + TEST_PORT + "/hello");
        assertEquals("Hello ", response);
    }

    @Test
    public void testPiEndpoint() throws Exception {
        String response = httpGet("http://localhost:" + TEST_PORT + "/pi");
        assertEquals(String.valueOf(Math.PI), response);
    }

    @Test
    public void testEchoEndpoint() throws Exception {
        String response = httpGet("http://localhost:" + TEST_PORT + "/echo");
        assertEquals("method=GET,path=/echo", response);
    }

    @Test
    public void testStaticFileServing() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:" + TEST_PORT + "/index.html").openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        assertEquals(200, responseCode);
        String contentType = conn.getContentType();
        assertTrue(contentType.contains("text/html"));
        conn.disconnect();
    }

    @Test
    public void testStaticCssFile() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:" + TEST_PORT + "/styles.css").openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        assertEquals(200, responseCode);
        assertTrue(conn.getContentType().contains("text/css"));
        conn.disconnect();
    }

    @Test
    public void testStaticJsFile() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:" + TEST_PORT + "/app.js").openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        assertEquals(200, responseCode);
        assertTrue(conn.getContentType().contains("javascript"));
        conn.disconnect();
    }

    @Test
    public void test404ForMissingFile() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:" + TEST_PORT + "/nonexistent.html").openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        assertEquals(404, responseCode);
        conn.disconnect();
    }

    @Test
    public void testContentTypeDetection() {
        assertEquals("text/html", HttpServer.getContentType("/index.html"));
        assertEquals("text/css", HttpServer.getContentType("/styles.css"));
        assertEquals("application/javascript", HttpServer.getContentType("/app.js"));
        assertEquals("application/json", HttpServer.getContentType("/data.json"));
        assertEquals("image/png", HttpServer.getContentType("/logo.png"));
        assertEquals("image/jpeg", HttpServer.getContentType("/photo.jpg"));
        assertEquals("image/gif", HttpServer.getContentType("/anim.gif"));
        assertEquals("image/svg+xml", HttpServer.getContentType("/icon.svg"));
    }

    @Test
    public void testMultipleQueryParams() throws Exception {
        server.addGetRoute("/greet", (req, res) ->
                req.getValues("greeting") + " " + req.getValues("name") + "!");
        String response = httpGet("http://localhost:" + TEST_PORT + "/greet?greeting=Hola&name=Mundo");
        assertEquals("Hola Mundo!", response);
    }

    private String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        conn.disconnect();
        return response.toString();
    }
}
