package com.github.idkp.simplenet.file;

import com.github.idkp.simplenet.ServerClientPipe;
import com.github.idkp.simplenet.StandardServerClient;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class FileServerClientPipe implements ServerClientPipe {
    private final FileServer fileServer;
    private final FileServerEntry fileServerEntry;
    private SocketChannel socketChannel;

    public FileServerClientPipe(FileServer fileServer, FileServerEntry fileServerEntry) {
        this.fileServer = fileServer;
        this.fileServerEntry = fileServerEntry;
    }

    @Override
    public void open(SocketChannel socketChannel, StandardServerClient client) throws IOException {
        this.socketChannel = socketChannel;
        fileServer.registerEntry(socketChannel, fileServerEntry);
    }

    @Override
    public void close() throws IOException {
        socketChannel.close();
    }
}
