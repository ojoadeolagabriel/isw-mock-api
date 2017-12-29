package com.interswitchgroup.proxy;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl;
import jetbrains.exodus.entitystore.PersistentEntityStores;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MockContext {
    public static Router router;
    public static Vertx vertx;
    public static PersistentEntityStoreImpl store;

    public static void init(int serverPort, int adminServerPort, String persistenceStorePath) {
        initPersistenceStore(persistenceStorePath);

        vertx = Vertx.vertx(new VertxOptions() {{ setWorkerPoolSize(5000); }});
        HttpServer httpServer = vertx.createHttpServer();
        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        //start server
        httpServer.requestHandler(router::accept)
                .listen(serverPort, httpServerAsyncResult -> {
                    if (httpServerAsyncResult.succeeded()) {
                        System.out.println("");
                        System.out.printf("Interswitch API MOCK [Processor Endpoint] is online on port [ %d ]...\r\n", serverPort);
                        System.out.printf("Interswitch API MOCK [Administrator Endpoint] is online on port [ %d ]...\r\n", adminServerPort);
                    } else {
                        System.out.print("Could not initialize server: " + httpServerAsyncResult.cause().getMessage());
                    }
                });
    }

    private static void initPersistenceStore(String persistenceStorePath) {
        store = PersistentEntityStores.newInstance(persistenceStorePath);
    }
}
