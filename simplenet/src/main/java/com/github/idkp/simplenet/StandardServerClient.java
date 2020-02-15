package com.github.idkp.simplenet;

import java.io.IOException;

public class StandardServerClient extends StandardServerClientBase {
    private final StandardServerClientPipeline pipeline;
    private final StandardServerBase server;
    private final ClientID id;

    public StandardServerClient(StandardServerBase server, ClientID id) {
        this.pipeline = new StandardServerClientPipeline(server, this);
        this.server = server;
        this.id = id;
    }

    @Override
    public ClientID getID() {
        return id;
    }

    @Override
    public ServerClientPipeline getPipeline() {
        return pipeline;
    }

    @Override
    protected StandardServerBase getServer() {
        return server;
    }

    @Override
    protected void close0() throws IOException {
        pipeline.close();
    }
}
