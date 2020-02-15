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
    protected final FileServerErrorHandler errorHandler;
    protected final boolean openInNewThread;

    protected Selector selector;
    protected Thread selectorThread;

    public FileServer(boolean openInNewThread) {
        this(new ClosingFileServerErrorHandler(), openInNewThread);
    }

    public FileServer(FileServerErrorHandler errorHandler, boolean openInNewThread) {
        this.errorHandler = errorHandler;
        this.openInNewThread = openInNewThread;
    }

    public void open() throws IOException {
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

    protected FileServerEntry createEntry(SocketChannel pipeChannel, Path destinationDir, long bufferSize) {
        return new FileServerEntry(pipeChannel, destinationDir, bufferSize);
    }

    public void registerPipe(SocketChannel pipeChannel, Path destinationDir, long bufferSize) throws IOException {
        if (Files.exists(destinationDir) && !Files.isDirectory(destinationDir)) {
            throw new NotDirectoryException(destinationDir.toString());
        }

        pipeChannel.configureBlocking(false);

        synchronized (this) {
            selector.wakeup();
            pipeChannel.register(selector, SelectionKey.OP_READ, createEntry(pipeChannel, destinationDir, bufferSize));
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
