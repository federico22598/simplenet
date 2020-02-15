package com.github.idkp.simplenet;

import java.io.IOException;

public abstract class StandardServerClientBase implements ServerClient {
    protected abstract StandardServerBase getServer();

    protected abstract void close0() throws IOException;

    @Override
    public final void close() throws IOException {
        getServer().clientClosing(this);
        close0();
    }
}
