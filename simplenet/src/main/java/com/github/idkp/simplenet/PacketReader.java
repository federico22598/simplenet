package com.github.idkp.simplenet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public final class PacketReader {
    private final SocketChannel channel;
    private final ByteBuffer headerBuf;
    private final ByteBuffer payloadHeaderBuf;
    private final ByteBuffer repeatBufCapBuf;

    private short bufCount = -1;
    private PayloadDecoder<?> decoder;
    private short lastBufIdx;
    private ByteBuffer payloadBuf;
    private short packetId;
    private Object payload;
    private boolean payloadBufReadCompleted;
    private volatile boolean active;
    private int payloadBufSize = -1;
    private boolean repeatPayloadRead;

    public PacketReader(SocketChannel channel) {
        this.channel = channel;
        this.headerBuf = ByteBuffer.allocateDirect(4);
        this.payloadHeaderBuf = ByteBuffer.allocateDirect(3);
        this.repeatBufCapBuf = ByteBuffer.allocateDirect(2);
    }

    public ReadResult readHeader(ShortFunction<PayloadDecoder> payloadDecoderSupplier) throws IOException {
        active = true;

        if (channel.read(headerBuf) == -1) {
            active = false;

            return ReadResult.EOF;
        }

        if (headerBuf.position() != headerBuf.capacity()) {
            active = false;

            return ReadResult.INCOMPLETE;
        }

        headerBuf.flip();

        packetId = headerBuf.getShort();
        bufCount = headerBuf.getShort();

        if (bufCount == 0) {
            active = false;
        } else {
            decoder = payloadDecoderSupplier.apply(packetId);
        }

        headerBuf.clear();

        lastBufIdx = 0;
        payload = null;

        return ReadResult.COMPLETE;
    }

    public ReadResult readPayload() throws IOException {
        while (lastBufIdx < bufCount) {
            if (payloadBufSize == -1) {
                int bytesRead = channel.read(payloadHeaderBuf);

                if (bytesRead == -1) {
                    active = false;

                    return ReadResult.EOF;
                }

                if (payloadHeaderBuf.position() != payloadHeaderBuf.capacity()) {
                    active = false;

                    return ReadResult.INCOMPLETE;
                }

                payloadHeaderBuf.flip();
                payloadBufSize = payloadHeaderBuf.getShort();
                repeatPayloadRead = payloadHeaderBuf.get() == (byte) 1;
                payloadHeaderBuf.clear();
            }

            if (payloadBuf == null) {
                if (repeatPayloadRead) {
                    int bytesRead = channel.read(repeatBufCapBuf);

                    if (bytesRead == -1) {
                        active = false;

                        return ReadResult.EOF;
                    }

                    if (repeatBufCapBuf.position() != repeatBufCapBuf.capacity()) {
                        active = false;

                        return ReadResult.INCOMPLETE;
                    }

                    repeatBufCapBuf.flip();
                    payloadBuf = ByteBuffer.allocate(repeatBufCapBuf.getShort());
                    repeatBufCapBuf.clear();
                } else {
                    payloadBuf = ByteBuffer.allocate(payloadBufSize);
                }
            }

            if (!payloadBufReadCompleted) {
                int bytesRead = channel.read(payloadBuf);

                if (bytesRead == -1) {
                    active = false;

                    return ReadResult.EOF;
                }

                if (payloadBuf.position() != payloadBufSize) {
                    active = false;

                    return ReadResult.INCOMPLETE;
                }

                payloadBuf.flip();
                payloadBufReadCompleted = true;
            }

            decoder.decode(payloadBuf);

            if (repeatPayloadRead) {
                payloadBuf.clear();
            } else {
                payloadBuf = null;
                lastBufIdx++;
            }

            payloadBufSize = -1;
            payloadBufReadCompleted = false;
        }

        payload = decoder.fetchResult();
        active = false;

        return ReadResult.COMPLETE;
    }

    public boolean ready() {
        return bufCount == -1;
    }

    public boolean active() {
        return active;
    }

    public boolean isPacketPayloadless() {
        return bufCount == 0;
    }

    public short getPacketId() {
        return packetId;
    }

    public Object getPayload() {
        return payload;
    }
}