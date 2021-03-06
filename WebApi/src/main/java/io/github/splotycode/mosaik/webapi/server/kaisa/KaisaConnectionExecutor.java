package io.github.splotycode.mosaik.webapi.server.kaisa;

import io.github.splotycode.mosaik.webapi.response.Response;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.github.splotycode.mosaik.webapi.server.kaisa.KaisaConfig.*;

public class KaisaConnectionExecutor {

    private ExecutorService executor;
    private ExecutorService downloader;
    private KaisaServer server;

    public KaisaConnectionExecutor(KaisaServer server) {
        this.server = server;

        //Setup Connection Executor
        if (server.getConfig().containsData(CONNECTION_EXECUTOR)) {
            if (server.getConfig().containsData(CONNECTION_EXECUTOR_PARALLEL))
                throw new InvalidConfigurationException(CONNECTION_EXECUTOR.getName() + " and "  + CONNECTION_EXECUTOR_PARALLEL + " can not be used togehter");
            executor = server.getConfig().getData(CONNECTION_EXECUTOR).get();
        } else if (server.getConfig().containsData(CONNECTION_EXECUTOR_PARALLEL)){
            executor = Executors.newFixedThreadPool(server.getConfig().getData(CONNECTION_EXECUTOR_PARALLEL));
        } else {
            executor = Executors.newFixedThreadPool(4);
        }

        if (server.getConfig().containsData(DOWNLOAD_EXECUTOR)) {
            if (server.getConfig().containsData(DOWNLOAD_EXECUTOR_PARALLEL))
                throw new InvalidConfigurationException(DOWNLOAD_EXECUTOR.getName() + " and "  + DOWNLOAD_EXECUTOR_PARALLEL + " can not be used togehter");
            executor = server.getConfig().getData(DOWNLOAD_EXECUTOR).get();
        } else if (server.getConfig().containsData(DOWNLOAD_EXECUTOR_PARALLEL)){
            executor = Executors.newFixedThreadPool(server.getConfig().getData(DOWNLOAD_EXECUTOR_PARALLEL));
        } else {
            executor = Executors.newFixedThreadPool(2);
        }
    }

    public void execute(SocketChannel connection) {
        executor.execute(() -> {
            try {
                connection.configureBlocking(false);
                KaisaRequest request = new KaisaRequest(server, connection);
                Response response = server.handleRequest(request);
                response.finish(request, server);
                //TODO write headers and response
            } catch (Throwable cause) {
                Response response = server.getErrorHandler().handleError(cause);
                response.finish(null, server);
                //TODO write headers and response
            }
        });
    }

    public void shutdown(boolean force) {
        if (force) {
            executor.shutdownNow();
        } else {
            executor.shutdown();
        }
    }

}
