package com.github.idkp.simplenet;

public abstract class StandardServerBase implements Server {
    protected abstract void unregisterClient(ServerClient client);

    final void clientPipelineClosing(ServerClient client) {
        unregisterClient(client);
    }

    final void clientPipeClosing(ServerClient client) {
        ServerClientPipeline pipeline = client.getPipeline();

        if (pipeline.getPipeCount() == 1) {
            unregisterClient(client);
        }
    }

    final void clientClosing(ServerClient client) {
        unregisterClient(client);
    }
}
