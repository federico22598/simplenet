package com.github.idkp.simplenet.file;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class NonBlockingFileClientPipe extends FileClientPipe {
    private final BlockingQueue<FileEntry> entryQueue  = new LinkedBlockingQueue<>();
    private final Consumer<IOException> writeExceptionHandler;
    private Thread flushThread;

    public NonBlockingFileClientPipe(Consumer<IOException> writeExceptionHandler) {
        super();
        this.writeExceptionHandler = writeExceptionHandler;
    }

    @Override
    public void open(SocketChannel socketChannel) throws IOException {
        super.open(socketChannel);

        flushThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                FileEntry fileEntry;

                try {
                    fileEntry = entryQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }

                try {
                    super.write(fileEntry.file, fileEntry.fileName, fileEntry.bufferSize);
                } catch (IOException e) {
                    writeExceptionHandler.accept(e);
                }
            }
        });

        flushThread.start();
    }

    @Override
    public void write(Path file, String fileName, long bufferSize) {
        entryQueue.add(new FileEntry(fileName, file, bufferSize));
    }

    @Override
    public void close() throws IOException {
        flushThread.interrupt();
        super.close();
    }

    private static class FileEntry {
        final String fileName;
        final Path file;
        final long bufferSize;

        private FileEntry(String fileName, Path file, long bufferSize) {
            this.fileName = fileName;
            this.file = file;
            this.bufferSize = bufferSize;
        }
    }
}
