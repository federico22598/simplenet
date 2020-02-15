package com.github.idkp.simplenet;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

public class StandardClientPipeline implements ClientPipeline {
    private final Set<ClientPipe> pipes = new HashSet<>();
    private final PipeChannelConnector pipeChannelConnector = new PipeChannelConnector();

    @Override
    public void openPipe(ClientPipe pipe, SocketChannel pipeChannel, SocketAddress serverAddress) throws IOException {
        pipeChannelConnector.connect(pipeChannel, serverAddress);

        if (pipes.add(pipe)) {
            pipe.open(pipeChannel);
        }
    }

    @Override
    public void closePipe(ClientPipe pipe) throws IOException {
        if (pipes.remove(pipe)) {
            pipe.close();
        }
    }

    @Override
    public Set<ClientPipe> getPipes() {
        return pipes;
    }

    @Override
    public void close() throws IOException {
        IOException exception = null;

        for (ClientPipe pipe : pipes) {
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
