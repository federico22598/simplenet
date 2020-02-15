package com.github.idkp.simplenet;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class StandardServerClientPipeline extends StandardServerClientPipelineBase {
    private final Map<String, ServerClientPipe> pipes = new HashMap<>();
    private final StandardServerBase server;
    private final StandardServerClient client;

    public StandardServerClientPipeline(StandardServerBase server, StandardServerClient client) {
        this.server = server;
        this.client = client;
    }

    @Override
    public void openPipe(String name, ServerClientPipe pipe, SocketChannel clientChannel) throws IOException {
        pipes.put(name, pipe);
        pipe.open(clientChannel, client);
    }

    @Override
    public void closePipe(String name) throws IOException {
        closePipe(pipes.get(name));
    }

    @Override
    public ServerClientPipe getPipe(String name) {
        return pipes.get(name);
    }

    @Override
    public int getPipeCount() {
        return pipes.size();
    }

    @Override
    public StandardServerBase getServer() {
        return server;
    }

    @Override
    public StandardServerClient getClient() {
        return client;
    }

    @Override
    protected void closePipe0(ServerClientPipe pipe) throws IOException {
        pipes.values().remove(pipe);
        pipe.close();
    }

    @Override
    protected void close0() throws IOException {
        IOException exception = null;

        for (ServerClientPipe pipe : pipes.values()) {
            try {
                pipe.close();
            } catch (IOException e) {
                if (exception == null) {
                    exception = e;
                } else {
                    exception.addSuppressed(e);
                }
            }
        }

        if (exception != null) {
            throw exception;
        }
    }
}
