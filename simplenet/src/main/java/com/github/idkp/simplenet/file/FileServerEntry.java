package com.github.idkp.simplenet.file;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileServerEntry implements Closeable {
    protected final SocketChannel channel;
    protected final Path destinationDir;
    protected final long bufferSize;

    protected FileChannel fileChannel;
    protected ByteBuffer fileInfoDataBuf = ByteBuffer.allocateDirect(8);
    protected ByteBuffer fileInfoBuf = null;
    protected int fileNameLen = 0;
    protected int fileSize = 0;
    protected int fileBytesRead = 0;

    public FileServerEntry(SocketChannel channel, Path destinationDir, long bufferSize) {
        this.channel = channel;
        this.destinationDir = destinationDir;
        this.bufferSize = bufferSize;
    }

    public void transfer() throws IOException {
        if (fileInfoBuf == null) {
            if (channel.read(fileInfoDataBuf) == -1) {
                close();
                return;
            }

            if (fileInfoDataBuf.hasRemaining()) {
                return;
            }

            fileInfoDataBuf.flip();
            fileNameLen = fileInfoDataBuf.getInt();
            fileSize = fileInfoDataBuf.getInt();
            fileInfoBuf = ByteBuffer.allocate(fileNameLen);
            fileInfoDataBuf.clear();
        } else if (fileChannel == null) {
            if (channel.read(fileInfoBuf) == -1) {
                close();
                return;
            }

            if (fileInfoBuf.hasRemaining()) {
                return;
            }

            fileInfoBuf.flip();

            byte[] fileNameBytes = new byte[fileNameLen];
            fileInfoBuf.get(fileNameBytes);
            String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
            Path file = destinationDir.resolve(
                    FileNameSanitizer.sanitize(fileName));
            Path fileParent = file.getParent();

            if (Files.notExists(fileParent)) {
                Files.createDirectories(fileParent);
            }

            fileChannel = FileChannel.open(file,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            fileInfoBuf.clear();
        } else {
            long bytesReadNow;
            if ((bytesReadNow = fileChannel.transferFrom(channel, fileBytesRead, Math.min(bufferSize, fileSize - fileBytesRead))) != -1) {
                fileBytesRead += bytesReadNow;

                if (fileBytesRead != fileSize) {
                    return;
                }
            }

            fileChannel.force(true);
            fileChannel.close();
            fileChannel = null;
            fileInfoBuf = null;
            fileBytesRead = 0;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            channel.close();
        } finally {
            if (fileChannel != null) {
                fileChannel.force(true);
                fileChannel.close();
            }
        }
    }
}
