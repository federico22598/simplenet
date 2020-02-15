package com.github.idkp.simplenet.file;

import java.nio.file.FileSystemException;

public class NotRegularFileException extends FileSystemException {
    public NotRegularFileException(String filePath) {
        super(filePath);
    }
}
