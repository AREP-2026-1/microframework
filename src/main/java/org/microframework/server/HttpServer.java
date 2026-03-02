package org.microframework.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lightweight HTTP server that handles REST service routing and static file serving.
 * Uses a thread pool to handle concurrent connections.
 */
public class HttpServer {

    private static final int DEFAULT_PORT = 8080;
    private final int port;
    private final Map<String, RequestHandler> getRoutes = new ConcurrentHashMap<>();
    private String staticFilesPath = "/webroot";
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public HttpServer() {
        this(DEFAULT_PORT);
    }

    public HttpServer(int port) {
        this.port = port;
    }

    public void addGetRoute(String path, RequestHandler handler) {
        getRoutes.put(path, handler);
    }

    public void setStaticFilesPath(String path) {
        this.staticFilesPath = path;
    }

    public String getStaticFilesPath() {
        return staticFilesPath;
    }

    public Map<String, RequestHandler> getGetRoutes() {
        return getRoutes;
    }

    public void start() throws IOException {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        serverSocket = new ServerSocket(port);
        running = true;
        System.out.println("MicroFramework server started on port " + port);
        System.out.println("Static files served from: " + staticFilesPath);
        System.out.println("Registered routes: " + getRoutes.keySet());

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleClient(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try (clientSocket;
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                return;
            }

            // Parse request headers
            Map<String, String> headers = new HashMap<>();
            String headerLine;
            while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
                int colonIdx = headerLine.indexOf(':');
                if (colonIdx > 0) {
                    headers.put(
                        headerLine.substring(0, colonIdx).trim().toLowerCase(),
                        headerLine.substring(colonIdx + 1).trim()
                    );
                }
            }

            // Parse the request line: GET /path?query HTTP/1.1
            String[] parts = requestLine.split(" ");
            if (parts.length < 3) {
                return;
            }

            String method = parts[0];
            String fullPath = parts[1];

            // Split path and query string
            String path;
            String queryString = null;
            int queryIdx = fullPath.indexOf('?');
            if (queryIdx >= 0) {
                path = fullPath.substring(0, queryIdx);
                queryString = fullPath.substring(queryIdx + 1);
            } else {
                path = fullPath;
            }

            Map<String, String> queryParams = Request.parseQueryString(queryString);
            Request request = new Request(method, path, queryParams, headers);
            Response response = new Response();

            // Try to match a REST route first
            if ("GET".equalsIgnoreCase(method) && getRoutes.containsKey(path)) {
                handleRestRequest(request, response, out);
            } else {
                // Try to serve a static file
                handleStaticFile(path, out);
            }

        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

    private void handleRestRequest(Request request, Response response, OutputStream out) throws IOException {
        RequestHandler handler = getRoutes.get(request.getPath());
        String body = handler.handle(request, response);

        String httpResponse = "HTTP/1.1 " + response.getStatusCode() + " OK\r\n"
                + "Content-Type: " + response.getContentType() + "\r\n"
                + "Content-Length: " + body.getBytes().length + "\r\n"
                + "\r\n"
                + body;
        out.write(httpResponse.getBytes());
        out.flush();
    }

    private void handleStaticFile(String path, OutputStream out) throws IOException {
        // Look for the file in the classpath under the configured static files path
        String resourcePath = staticFilesPath + path;
        InputStream fileStream = getClass().getResourceAsStream(resourcePath);

        if (fileStream != null) {
            byte[] fileBytes = fileStream.readAllBytes();
            fileStream.close();
            String contentType = getContentType(path);
            String header = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: " + contentType + "\r\n"
                    + "Content-Length: " + fileBytes.length + "\r\n"
                    + "\r\n";
            out.write(header.getBytes());
            out.write(fileBytes);
            out.flush();
        } else {
            // Try from file system as fallback (target/classes/...)
            Path filePath = Paths.get("target/classes" + resourcePath);
            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                byte[] fileBytes = Files.readAllBytes(filePath);
                String contentType = getContentType(path);
                String header = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: " + contentType + "\r\n"
                        + "Content-Length: " + fileBytes.length + "\r\n"
                        + "\r\n";
                out.write(header.getBytes());
                out.write(fileBytes);
                out.flush();
            } else {
                String body = "404 Not Found: " + path;
                String header = "HTTP/1.1 404 Not Found\r\n"
                        + "Content-Type: text/plain\r\n"
                        + "Content-Length: " + body.getBytes().length + "\r\n"
                        + "\r\n";
                out.write(header.getBytes());
                out.write(body.getBytes());
                out.flush();
            }
        }
    }

    static String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif")) return "image/gif";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }
}
