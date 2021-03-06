package org.joyrest.grizzly;

import org.glassfish.grizzly.http.server.HttpServer;
import org.joyrest.context.ApplicationContext;
import org.joyrest.grizzly.handler.GrizzlyApplicationHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class GrizzlyServer {

    private static Logger LOG = Logger.getLogger(GrizzlyServer.class.getName());

    public static void start(final ApplicationContext applicationConfig, String path, final int port) {
        try {
            // Logging settings
            InputStream loggingStream = GrizzlyServer.class.getResourceAsStream("/logging.properties");
            LogManager.getLogManager().readConfiguration(loggingStream);

            // Automatically set the SelectorRunner count value equal to
            // Runtime.getRuntime().availableProcessors()
            HttpServer server = HttpServer.createSimpleServer(null, port);
            server.getServerConfiguration().addHttpHandler(
                    new GrizzlyApplicationHandler(applicationConfig), path);

            Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));

            server.start();
            LOG.info("Feed Combine Application started. Stop the application using ^C.");
            Thread.currentThread().join();
        } catch (IOException | InterruptedException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

}
