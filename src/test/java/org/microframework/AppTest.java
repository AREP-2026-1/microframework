package org.microframework;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.microframework.server.MicroFramework.*;

import org.microframework.server.MicroFramework;

/**
 * Tests for the App example configuration.
 */
public class AppTest {

    @Test
    public void testRoutesAreRegistered() {
        get("/test-route", (req, res) -> "test");
        assertNotNull(MicroFramework.getServer().getGetRoutes().get("/test-route"));
    }

    @Test
    public void testStaticFilesPathIsSet() {
        staticfiles("/custom-path");
        assertEquals("/custom-path", MicroFramework.getServer().getStaticFilesPath());
    }
}
