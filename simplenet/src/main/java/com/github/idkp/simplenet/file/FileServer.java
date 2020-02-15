package com.github.idkp.simplenet.file;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Iterator;

public class FileServer implements Closeable {
    private final Path destinationDir;
    private final long bufferSize;
    private final FileServerErrorHandler errorHandler;
    private final boolean openInNewThread;

    private Selector selector;
    private Thread selectorThread;

    public FileServer(Path destinationDir, long bufferSize, boolean openInNewThread) {
        this(destinationDir, bufferSize, new ClosingFileServerErrorHandler(), openInNewThread);
    }

    public FileServer(Path destinationDir, long bufferSize, FileServerErrorHandler errorHandler, boolean openInNewThread) {
        this.destinationDir = destinationDir;
        this.bufferSize = bufferSize;
        this.errorHandler = errorHandler;
        this.openInNewThread = openInNewThread;
    }

    public void open() throws IOException {
        if (Files.exists(destinationDir) && !Files.isDirectory(destinationDir)) {
            throw new NotDirectoryException(destinationDir.toString());
        }

        selector = Selector.open();

        if (openInNewThread) {
            selectorThread = new Thread(this::startSelectorLoop);
            selectorThread.start();
        } else {
            startSelectorLoop();
        }
    }

    private void startSelectorLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (this) {
            }

            try {
                selector.select();
            } catch (IOException e) {
                errorHandler.handle(this, "sel", e);
                return;
            }

            Iterator<SelectionKey> selectedKeysIter = selector.selectedKeys().iterator();

            while (selectedKeysIter.hasNext()) {
                SelectionKey key = selectedKeysIter.next();
                selectedKeysIter.remove();

                if (key.isValid()) {
                    if (key.isReadable()) {
                        FileServerEntry entry = (FileServerEntry) key.attachment();

                        try {
                            entry.transfer();
                        } catch (IOException e) {
                            errorHandler.handle(this, "retrieve", e, entry);
                        }
                    }
                }
            }
        }
    }

    public void registerPipe(SocketChannel pipeChannel) throws IOException {
        pipeChannel.configureBlocking(false);

        synchronized (this) {
            selector.wakeup();
            pipeChannel.register(selector, SelectionKey.OP_READ,
                    new FileServerEntry(pipeChannel, destinationDir, bufferSize));
        }
    }

    @Override
    public void close() throws IOException {
        if (selectorThread != null) {
            selectorThread.interrupt();
        }

        selector.close();
    }
}
