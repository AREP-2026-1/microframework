package org.microframework.server;

import java.io.IOException;

/**
 * Main entry point for the MicroFramework web framework.
 * Provides static methods to configure routes and start the server.
 *
 * Usage example:
 * <pre>
 *     import static org.microframework.server.MicroFramework.*;
 *
 *     public class App {
 *         public static void main(String[] args) {
 *             staticfiles("/webroot");
 *             get("/hello", (req, res) -> "Hello " + req.getValues("name"));
 *             get("/pi", (req, res) -> String.valueOf(Math.PI));
 *             start();
 *         }
 *     }
 * </pre>
 */
public class MicroFramework {

    private static final HttpServer server = new HttpServer();
    private static int port = 8080;

    private MicroFramework() {
        // Utility class, no instantiation
    }

    /**
     * Registers a GET REST service route with a lambda handler.
     *
     * @param path    the URL path to match (e.g., "/hello")
     * @param handler the lambda function to handle the request
     */
    public static void get(String path, RequestHandler handler) {
        server.addGetRoute(path, handler);
    }

    /**
     * Sets the folder where static files are located.
     * The framework will look for static files in the classpath under this path.
     *
     * @param path the static files directory path (e.g., "/webroot")
     */
    public static void staticfiles(String path) {
        server.setStaticFilesPath(path);
    }

    /**
     * Sets the server port. Must be called before start().
     */
    public static void port(int p) {
        port = p;
    }

    /**
     * Starts the HTTP server with the configured routes and static files path.
     */
    public static void start() {
        try {
            HttpServer s = new HttpServer(port);
            // Copy routes and config
            server.getGetRoutes().forEach(s::addGetRoute);
            s.setStaticFilesPath(server.getStaticFilesPath());
            s.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }

    /**
     * Returns the internal server instance (used for testing).
     */
    public static HttpServer getServer() {
        return server;
    }
}
