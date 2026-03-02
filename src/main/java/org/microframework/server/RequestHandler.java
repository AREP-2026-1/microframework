package org.microframework.server;

/**
 * Functional interface for handling REST service requests using lambda expressions.
 * Example: (req, res) -> "hello world!"
 */
@FunctionalInterface
public interface RequestHandler {
    String handle(Request req, Response res);
}
