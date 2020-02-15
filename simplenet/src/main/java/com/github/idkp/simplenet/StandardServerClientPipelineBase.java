package com.github.idkp.simplenet;

import java.io.IOException;

public abstract class StandardServerClientPipelineBase implements ServerClientPipeline {
    protected abstract StandardServerBase getServer();

    protected abstract void closePipe0(ServerClientPipe pipe) throws IOException;

    protected abstract void close0() throws IOException;

    @Override
    public final void closePipe(ServerClientPipe pipe) throws IOException {
        getServer().clientPipeClosing(getClient());
        closePipe0(pipe);
    }

    @Override
    public final void close() throws IOException {
        getServer().clientPipelineClosing(getClient());
        close0();
    }
}
