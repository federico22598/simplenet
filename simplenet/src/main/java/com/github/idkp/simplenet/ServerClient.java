package com.github.idkp.simplenet;

import java.io.Closeable;

public interface ServerClient extends Closeable {
    ClientID getID();

    ServerClientPipeline getPipeline();
}
