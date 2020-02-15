package com.github.idkp.simplenet.example.files;

import com.github.idkp.simplenet.ClientPipeline;
import com.github.idkp.simplenet.Server;
import com.github.idkp.simplenet.StandardClientPipeline;
import com.github.idkp.simplenet.StandardServer;
import com.github.idkp.simplenet.file.FileClientPipe;
import com.github.idkp.simplenet.file.FileServer;
import com.github.idkp.simplenet.file.FileServerClientPipe;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public final class Launcher {
    private static final int SERVER_PORT = 1338;
    private static final Path DESTINATION_DIR = Paths.get("simplenet-files-example/downloaded-files");

    public static void main(String[] args) throws IOException {
        startServer();
        startClient();
    }

    private static void startClient() throws IOException {
        SocketChannel serverSocketChannel = SocketChannel.open();
        ClientPipeline pipeline = new StandardClientPipeline();
        FileClientPipe filePipe = new FileClientPipe(8192L);

        pipeline.openPipe(filePipe, serverSocketChannel, new InetSocketAddress(InetAddress.getLocalHost(), SERVER_PORT));

        Path filesToDownloadDir = Paths.get("simplenet-files-example/todownload");

        filePipe.write(filesToDownloadDir.resolve("loremipsum.txt"));

        int rootDirPathLen = filesToDownloadDir.toString().length() + FileSystems.getDefault().getSeparator().length();

        for (Iterator<Path> dirIter = Files.walk(filesToDownloadDir.resolve("loremipsum1")).filter(Files::isRegularFile).iterator(); dirIter.hasNext(); ) {
            Path file = dirIter.next();
            String filePath = file.toString();
            String relativeFilePath = filePath.substring(rootDirPathLen);

            filePipe.write(file, relativeFilePath);
        }

        filePipe.close();
    }

    private static void startServer() throws IOException {
        FileServer fileServer = new FileServer(true);
        Server server = StandardServer.create((client, pipeChannel) -> {
            FileServerClientPipe filePipe = new FileServerClientPipe(fileServer, DESTINATION_DIR, 8192L);

            try {
                client.getPipeline().openPipe("file", filePipe, pipeChannel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        fileServer.open();
        server.bind(SERVER_PORT);
    }
}
