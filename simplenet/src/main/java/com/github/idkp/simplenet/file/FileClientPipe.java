package com.github.idkp.simplenet.file;

import com.github.idkp.simplenet.ClientPipe;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileClientPipe implements ClientPipe {
    private final long bufferSize;
    private final ByteBuffer fileInfoDataBuf;
    private SocketChannel socketChannel;

    public FileClientPipe(long bufferSize) {
        this.bufferSize = bufferSize;
        this.fileInfoDataBuf = ByteBuffer.allocateDirect(8);
    }

    @Override
    public void open(SocketChannel socketChannel) throws IOException {
        socketChannel.configureBlocking(true);
        this.socketChannel = socketChannel;
    }

    public void write(Path file) throws IOException {
        write(file, file.getFileName().toString());
    }

    public void write(Path file, String fileName) throws IOException {
        if (Files.isDirectory(file)) {
            throw new NotRegularFileException(file.toString());
        }

        try (FileChannel fileChannel = FileChannel.open(file, StandardOpenOption.READ)) {
            int fileNameLen = fileName.length();
            long fileChannelSize = fileChannel.size();

            fileInfoDataBuf.clear();
            fileInfoDataBuf.putInt(fileNameLen);
            fileInfoDataBuf.putInt((int) fileChannelSize);
            fileInfoDataBuf.flip();
            socketChannel.write(fileInfoDataBuf);

            ByteBuffer fileNameBuf = ByteBuffer.allocate(fileNameLen);

            fileNameBuf.put(fileName.getBytes(StandardCharsets.UTF_8));
            fileNameBuf.flip();
            socketChannel.write(fileNameBuf);

            long pos = 0;

            while (pos < fileChannelSize) {
                pos += fileChannel.transferTo(pos, bufferSize, socketChannel);
            }
        }
    }

    @Override
    public void close() throws IOException {
        socketChannel.close();
    }
}
