package com.github.idkp.simplenet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public final class PipeChannelConnector {
    private final ByteBuffer pIPLenBuf;

    public PipeChannelConnector() {
        pIPLenBuf = ByteBuffer.allocateDirect(4);
    }

    public void connect(SocketChannel channel, SocketAddress serverAddress) throws IOException {
        if (channel.isConnected()) {
            throw new AlreadyConnectedException();
        }

        boolean wasChBlocking = channel.isBlocking();
        channel.configureBlocking(true);
        channel.connect(serverAddress);
        writePrivateIPAddress(channel);
        channel.configureBlocking(wasChBlocking);
    }

    private void writePrivateIPAddress(SocketChannel channel) throws IOException {
        String pIP = ((InetSocketAddress) channel.getLocalAddress()).getAddress().getHostAddress();
        byte[] pIPBytes = pIP.getBytes(StandardCharsets.UTF_8);
        int pIPByteCount = pIPBytes.length;

        pIPLenBuf.putInt(pIPByteCount);
        pIPLenBuf.flip();
        channel.write(pIPLenBuf);
        pIPLenBuf.clear();

        ByteBuffer pIPBuf = ByteBuffer.allocate(pIPByteCount);
        pIPBuf.put(pIPBytes);
        pIPBuf.flip();
        channel.write(pIPBuf);
    }
}
