package com.github.idkp.simplenet.file;

import com.github.idkp.simplenet.ServerClientPipe;
import com.github.idkp.simplenet.StandardServerClient;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;

public class FileServerClientPipe implements ServerClientPipe {
    private final FileServer fileServer;
    private final Path destinationDir;
    private final long bufferSize;

    private SocketChannel socketChannel;

    public FileServerClientPipe(FileServer fileServer, Path destinationDir, long bufferSize) {
        this.fileServer = fileServer;
        this.destinationDir = destinationDir;
        this.bufferSize = bufferSize;
    }

    @Override
    public void open(SocketChannel socketChannel, StandardServerClient client) throws IOException {
        this.socketChannel = socketChannel;
        fileServer.registerEntry(socketChannel, new FileServerEntry(socketChannel, destinationDir, bufferSize));
    }

    @Override
    public void close() throws IOException {
        socketChannel.close();
    }
}
