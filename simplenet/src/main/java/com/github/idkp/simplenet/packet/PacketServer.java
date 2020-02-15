package com.github.idkp.simplenet.packet;

import com.github.idkp.simplenet.ServerClientPipe;
import com.github.idkp.simplenet.StandardServerClient;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface PacketServer extends Closeable {
    void open() throws IOException;

    PipeRegistrationKey registerPipe(SocketChannel pipeChannel, StandardServerClient client, ServerClientPipe pipe) throws IOException;

    @Override
    void close() throws IOException;
}
