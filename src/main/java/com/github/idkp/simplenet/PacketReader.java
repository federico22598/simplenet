package com.github.idkp.simplenet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public final class PacketReader {
    private final SocketChannel channel;
    private final ByteBuffer bufCapBuf;
    private final ByteBuffer headerBuf;
    private final ByteBuffer repeatBuf;

    private short bufCount = -1;
    private PayloadDecoder<?> decoder;
    private short lastBufIdx;
    private ByteBuffer buf;
    private short packetId;
    private Object payload;
    private boolean bufReadCompleted;
    private boolean repeatBufReadCompleted;

    public PacketReader(SocketChannel channel) {
        this.channel = channel;
        this.bufCapBuf = ByteBuffer.allocateDirect(2);
        this.headerBuf = ByteBuffer.allocateDirect(4);
        this.repeatBuf = ByteBuffer.allocateDirect(1);
    }

    public ReadResult readHeader(ShortFunction<PayloadDecoder> payloadDecoderSupplier) throws IOException {
        if (channel.read(headerBuf) == -1) {
            return ReadResult.EOF;
        }

        if (headerBuf.position() != headerBuf.capacity()) {
            return ReadResult.INCOMPLETE;
        }

        headerBuf.flip();

        packetId = headerBuf.getShort();
        bufCount = headerBuf.getShort();

        headerBuf.clear();

        decoder = payloadDecoderSupplier.apply(packetId);
        lastBufIdx = 0;
        payload = null;

        return ReadResult.COMPLETE;
    }

    public ReadResult readPayload() throws IOException {
        if (bufCount != 0) {
            while (lastBufIdx < bufCount) {
                if (buf == null) {
                    int bytesRead = channel.read(bufCapBuf);

                    if (bytesRead == -1) {
                        return ReadResult.EOF;
                    }

                    if (bufCapBuf.position() != bufCapBuf.capacity()) {
                        return ReadResult.INCOMPLETE;
                    }

                    bufCapBuf.flip();

                    short cap = bufCapBuf.getShort();

                    bufCapBuf.clear();

                    buf = ByteBuffer.allocateDirect(cap);
                }

                if (!repeatBufReadCompleted) {
                    int bytesRead = channel.read(repeatBuf);

                    if (bytesRead == -1) {
                        return ReadResult.EOF;
                    }

                    if (repeatBuf.position() == repeatBuf.capacity()) {
                        repeatBufReadCompleted = true;
                        repeatBuf.flip();
                    } else {
                        return ReadResult.INCOMPLETE;
                    }
                }

                if (!bufReadCompleted) {
                    int bytesRead = channel.read(buf);

                    if (bytesRead == -1) {
                        return ReadResult.EOF;
                    }

                    if (buf.position() == buf.capacity()) {
                        bufReadCompleted = true;
                        buf.flip();
                        repeatBuf.clear();
                    } else {
                        return ReadResult.INCOMPLETE;
                    }
                }

                decoder.decode(buf);

                if (repeatBuf.get() == (byte) 1) {
                    buf.clear();
                } else {
                    buf = null;
                    lastBufIdx++;
                }

                bufReadCompleted = false;
                repeatBufReadCompleted = false;
                repeatBuf.clear();
            }

            payload = decoder.fetchResult();
        }

        bufCount = -1;

        return ReadResult.COMPLETE;
    }

    public boolean ready() {
        return bufCount == -1;
    }

    public short getPacketId() {
        return packetId;
    }

    public Object getPayload() {
        return payload;
    }
}