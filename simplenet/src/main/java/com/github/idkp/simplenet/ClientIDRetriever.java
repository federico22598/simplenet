package com.github.idkp.simplenet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public final class ClientIDRetriever {
    private final ByteBuffer privateIPLenBuf;

    public ClientIDRetriever() {
        privateIPLenBuf = ByteBuffer.allocateDirect(4);
    }

    public ClientID retrieve(SocketChannel pipeChannel) throws IOException {
        boolean wasChBlocking = pipeChannel.isBlocking();
        pipeChannel.configureBlocking(true);

        pipeChannel.read(privateIPLenBuf);
        privateIPLenBuf.flip();
        int pIPLen = privateIPLenBuf.getInt();
        privateIPLenBuf.clear();

        ByteBuffer pIPBuf = ByteBuffer.allocate(pIPLen);
        pipeChannel.read(pIPBuf);
        pIPBuf.flip();

        byte[] pIPBytes = new byte[pIPLen];
        pIPBuf.get(pIPBytes);

        pipeChannel.configureBlocking(wasChBlocking);

        String privIP = new String(pIPBytes, StandardCharsets.UTF_8);
        String pubIP = ((InetSocketAddress) pipeChannel.getRemoteAddress()).getAddress().getHostAddress();

        return new ClientID(privIP, pubIP);
    }
}
