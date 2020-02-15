package com.github.idkp.simplenet.file;

public interface FileServerErrorHandler {
    void handle(FileServer server, String name, Exception exception);

    void handle(FileServer server, String errorName, FileServerEntry entry);

    void handle(FileServer server, String errorName, Exception exception, FileServerEntry entry);
}
