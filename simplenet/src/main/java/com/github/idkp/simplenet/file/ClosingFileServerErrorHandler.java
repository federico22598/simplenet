package com.github.idkp.simplenet.file;

import java.io.IOException;

public class ClosingFileServerErrorHandler implements FileServerErrorHandler {

    @Override
    public void handle(FileServer server, String name, Exception exception) {
        exception.printStackTrace();

        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle(FileServer server, String errorName, FileServerEntry entry) {
        try {
            entry.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle(FileServer server, String errorName, Exception exception, FileServerEntry entry) {
        exception.printStackTrace();

        try {
            entry.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
