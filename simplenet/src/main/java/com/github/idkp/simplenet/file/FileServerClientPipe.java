package com.github.idkp.simplenet.file;

import com.github.idkp.simplenet.ServerClientPipe;
import com.github.idkp.simplenet.StandardServerClient;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class FileServerClientPipe implements ServerClientPipe {
    private final FileServer fileServer;
    private SocketChannel socketChannel;

    public FileServerClientPipe(FileServer fileServer) {
        this.fileServer = fileServer;
    }

    @Override
    public void open(SocketChannel socketChannel, StandardServerClient client) throws IOException {
        this.socketChannel = socketChannel;
        fileServer.registerPipe(socketChannel);
    }

    @Override
    public void close() throws IOException {
        socketChannel.close();
    }
}
