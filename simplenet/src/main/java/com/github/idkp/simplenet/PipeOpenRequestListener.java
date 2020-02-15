package com.github.idkp.simplenet;

import java.nio.channels.SocketChannel;

public interface PipeOpenRequestListener {
    void received(ServerClient client, SocketChannel pipeChannel);
}
