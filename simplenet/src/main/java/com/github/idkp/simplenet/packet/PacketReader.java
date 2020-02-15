package com.github.idkp.simplenet.packet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collection;

public final class PacketReader {
    private final SocketChannel channel;
    private final PacketPipeInputConfiguration config;
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

    public PacketReader(SocketChannel channel, PacketPipeInputConfiguration config) {
        this.channel = channel;
        this.config = config;
        this.headerBuf = ByteBuffer.allocateDirect(4);
        this.payloadHeaderBuf = ByteBuffer.allocateDirect(3);
        this.repeatBufCapBuf = ByteBuffer.allocateDirect(2);
    }

    public ReadResult read() throws IOException {
        if (!active) {
            ReadResult headerReadResult = readHeader(config);

            if (headerReadResult != ReadResult.COMPLETE) {
                return headerReadResult;
            }
        }

        if (bufCount != 0) {
            ReadResult readResult = readPayload();

            if (readResult != ReadResult.COMPLETE) {
                return readResult;
            }
        } else {
            bufCount = -1;
        }

        Collection<PacketListener<?>> listeners = config.getPacketListeners(packetId);

        if (listeners != null) {
            for (PacketListener<?> listener : listeners) {
                //noinspection unchecked
                ((PacketListener<Object>) listener).received(payload);
            }
        }

        return ReadResult.COMPLETE;
    }

    private ReadResult readHeader(PacketPipeInputConfiguration connConfig) throws IOException {
        active = true;

        try {
            if (channel.read(headerBuf) == -1) {
                active = false;
                bufCount = -1;

                return ReadResult.EOF;
            }
        } catch (IOException e) {
            active = false;
            bufCount = -1;

            throw e;
        }

        if (headerBuf.position() != headerBuf.capacity()) {
            return ReadResult.INCOMPLETE;
        }

        headerBuf.flip();

        packetId = headerBuf.getShort();
        bufCount = headerBuf.getShort();

        if (bufCount == 0) {
            active = false;
            bufCount = 0;
        } else {
            decoder = connConfig.getPacketPayloadDecoder(packetId);
        }

        headerBuf.clear();

        lastBufIdx = 0;
        payload = null;

        return ReadResult.COMPLETE;
    }

    private ReadResult readPayload() throws IOException {
        while (lastBufIdx < bufCount) {
            if (payloadBufSize == -1) {
                int bytesRead;

                try {
                    bytesRead = channel.read(payloadHeaderBuf);
                } catch (IOException e) {
                    active = false;
                    bufCount = -1;
                    throw e;
                }

                if (bytesRead == -1) {
                    active = false;
                    bufCount = -1;

                    return ReadResult.EOF;
                }

                if (payloadHeaderBuf.position() != payloadHeaderBuf.capacity()) {
                    return ReadResult.INCOMPLETE;
                }

                payloadHeaderBuf.flip();
                repeatPayloadRead = payloadHeaderBuf.get() == (byte) 1;
                payloadBufSize = payloadHeaderBuf.getShort();
                payloadHeaderBuf.clear();
            }

            if (payloadBuf == null) {
                if (repeatPayloadRead) {
                    int bytesRead;

                    try {
                        bytesRead = channel.read(repeatBufCapBuf);
                    } catch (IOException e) {
                        active = false;
                        bufCount = -1;

                        throw e;
                    }

                    if (bytesRead == -1) {
                        active = false;
                        bufCount = -1;

                        return ReadResult.EOF;
                    }

                    if (repeatBufCapBuf.position() != repeatBufCapBuf.capacity()) {
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
                int bytesRead;

                try {
                    bytesRead = channel.read(payloadBuf);
                } catch (IOException e) {
                    active = false;
                    bufCount = -1;

                    throw e;
                }

                if (bytesRead == -1) {
                    active = false;
                    bufCount = -1;

                    return ReadResult.EOF;
                }

                if (payloadBuf.position() != payloadBufSize) {
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

        payload = decoder.getResult();
        active = false;
        bufCount = -1;

        return ReadResult.COMPLETE;
    }

    public boolean isActive() {
        return active;
    }

    public PacketPipeInputConfiguration getConfig() {
        return config;
    }
}