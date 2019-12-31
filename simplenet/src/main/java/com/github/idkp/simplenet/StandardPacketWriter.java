package com.github.idkp.simplenet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;

public final class StandardPacketWriter implements PacketWriter {
    private final SocketChannel channel;
    private final Selector selector;
    private final ConnectionConfiguration connConfig;
    private final Queue<PacketWriteData> wrDataQueue;
    private final ByteBuffer wrDataHeaderBuf;
    private final ByteBuffer bufWrDataPosRepsBuf;
    private final ByteBuffer bufWrDataCapPosRepsBuf;
    private final ByteBuffer[] bufWrDataHeaderBufs;
    private boolean writing;
    private boolean completedLastHeaderWrite;
    private boolean startedLastHeaderWrite;
    private boolean completedWrDataHeaderWrite;
    private boolean startedWrDataHeaderWrite;
    private boolean bufWrDataRepeat;

    public StandardPacketWriter(SocketChannel channel,
                                Selector selector,
                                ConnectionConfiguration connConfig) {
        this.channel = channel;
        this.selector = selector;
        this.connConfig = connConfig;
        this.wrDataQueue = new ArrayDeque<>();
        this.wrDataHeaderBuf = ByteBuffer.allocateDirect(4);
        this.bufWrDataPosRepsBuf = ByteBuffer.allocateDirect(3);
        this.bufWrDataCapPosRepsBuf = ByteBuffer.allocateDirect(5);
        this.bufWrDataHeaderBufs = new ByteBuffer[2];
    }

    @Override
    public <T> void write(String packetName, T payload) {
        Short packetId = connConfig.getPacketId(packetName);
        if (packetId == null) {
            throw new UnknownPacketException();
        }

        PacketWriteData writeData = new PacketWriteData(packetId);
        //noinspection unchecked
        PayloadEncoder<T> encoder = (PayloadEncoder<T>) connConfig.getPacketPayloadEncoder(packetName);

        if (encoder != null) {
            encoder.encode(payload, new StandardPacketBufferWriter(writeData));
        }

        wrDataQueue.add(writeData);
        setReadyForChannelWrite();
    }

    @Override
    public void flush() throws IOException {
        while (!wrDataQueue.isEmpty()) {
            PacketWriteData wrData = wrDataQueue.peek();
            Queue<PacketBufWriteData> bufWrDataQueue = wrData.bufQueue;

            if (!completedLastHeaderWrite) {
                if (!startedLastHeaderWrite) {
                    wrDataHeaderBuf.putShort(wrData.packetId);
                    wrDataHeaderBuf.putShort((short) bufWrDataQueue.size());
                    wrDataHeaderBuf.flip();

                    startedLastHeaderWrite = true;
                }

                channel.write(wrDataHeaderBuf);

                if (wrDataHeaderBuf.hasRemaining()) {
                    return;
                }

                wrDataHeaderBuf.clear();

                completedLastHeaderWrite = true;
            }

            while (!bufWrDataQueue.isEmpty()) {
                PacketBufWriteData bufWrData = bufWrDataQueue.peek();
                ByteBuffer buf = bufWrData.buf;

                if (!completedWrDataHeaderWrite) {
                    if (!startedWrDataHeaderWrite) {
                        if (bufWrData.repeatingBufWriter == null) {
                            bufWrDataHeaderBufs[0] = bufWrDataPosRepsBuf;

                            bufWrDataPosRepsBuf.clear();
                            bufWrDataPosRepsBuf.put((byte) 0);
                            bufWrDataPosRepsBuf.putShort((short) buf.position());
                            bufWrDataPosRepsBuf.flip();
                        } else {
                            if (bufWrDataRepeat) {
                                buf.clear();
                                bufWrDataRepeat = bufWrData.repeatingBufWriter.repeat();
                                bufWrDataHeaderBufs[0] = bufWrDataPosRepsBuf;

                                bufWrDataPosRepsBuf.clear();
                                bufWrDataPosRepsBuf.put(bufWrDataRepeat ? (byte) 1 : (byte) 0);
                                bufWrDataPosRepsBuf.putShort((short) buf.position());
                                bufWrDataPosRepsBuf.flip();
                            } else { // first time
                                bufWrDataRepeat = bufWrData.repeatingBufWriter.repeat();
                                bufWrDataHeaderBufs[0] = bufWrDataCapPosRepsBuf;

                                bufWrDataCapPosRepsBuf.clear();
                                bufWrDataCapPosRepsBuf.put(bufWrDataRepeat ? (byte) 1 : (byte) 0);
                                bufWrDataCapPosRepsBuf.putShort((short) buf.position());
                                bufWrDataCapPosRepsBuf.putShort((short) buf.capacity());
                                bufWrDataCapPosRepsBuf.flip();
                            }
                        }

                        buf.flip();

                        bufWrDataHeaderBufs[1] = buf;
                        startedWrDataHeaderWrite = true;
                    }

                    channel.write(bufWrDataHeaderBufs);

                    if (bufWrDataHeaderBufs[0].hasRemaining()) {
                        return;
                    }

                    completedWrDataHeaderWrite = true;
                } else {
                    channel.write(buf);
                }

                if (buf.hasRemaining()) {
                    return;
                }

                completedWrDataHeaderWrite = false;
                startedWrDataHeaderWrite = false;

                if (!bufWrDataRepeat) {
                    bufWrDataQueue.remove();
                }
            }

            completedLastHeaderWrite = false;
            startedLastHeaderWrite = false;

            wrDataQueue.remove();
        }

        writing = false;
        changeSelectorInterestOps(SelectionKey.OP_READ);
    }

    private void setReadyForChannelWrite() {
        if (!writing) {
            writing = true;
            changeSelectorInterestOps(SelectionKey.OP_WRITE);
            selector.wakeup();
        }
    }

    private void changeSelectorInterestOps(int ops) {
        channel.keyFor(selector).interestOps(ops);
    }
}
