package com.github.idkp.simplenet.file;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class NonBlockingFileClientPipe extends FileClientPipe {
    private final BlockingQueue<FileData> fileQueue = new LinkedBlockingQueue<>();
    private final Consumer<IOException> writeExceptionHandler;
    private Thread flushThread;

    public NonBlockingFileClientPipe(long bufferSize, Consumer<IOException> writeExceptionHandler) {
        super(bufferSize);
        this.writeExceptionHandler = writeExceptionHandler;
    }

    @Override
    public void open(SocketChannel socketChannel) throws IOException {
        super.open(socketChannel);

        flushThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                FileData fileData;

                try {
                    fileData = fileQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }

                try {
                    super.write(fileData.file, fileData.fileName);
                } catch (IOException e) {
                    writeExceptionHandler.accept(e);
                }
            }
        });

        flushThread.start();
    }

    @Override
    public void write(Path file, String fileName) {
        fileQueue.add(new FileData(fileName, file));
    }

    @Override
    public void close() throws IOException {
        flushThread.interrupt();
        super.close();
    }

    private static class FileData {
        final String fileName;
        final Path file;

        private FileData(String fileName, Path file) {
            this.fileName = fileName;
            this.file = file;
        }
    }
}
