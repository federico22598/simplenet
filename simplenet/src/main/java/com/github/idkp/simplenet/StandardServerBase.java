package com.github.idkp.simplenet;

public abstract class StandardServerBase implements Server {
    protected abstract void unregisterClient(ServerClient client);

    final void clientPipelineClosed(ServerClient client) {
        unregisterClient(client);
    }

    final void clientPipeClosed(ServerClient client) {
        ServerClientPipeline pipeline = client.getPipeline();

        if (pipeline.getPipeCount() == 1) {
            unregisterClient(client);
        }
    }

    final void clientClosed(ServerClient client) {
        unregisterClient(client);
    }
}
