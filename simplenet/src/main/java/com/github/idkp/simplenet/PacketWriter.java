package com.github.idkp.simplenet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;

public final class PacketWriter {
    private final SocketChannel channel;
    private final Selector selector;
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

    public PacketWriter(SocketChannel channel, Selector selector) {
        this.channel = channel;
        this.selector = selector;
        this.wrDataQueue = new ArrayDeque<>();
        this.wrDataHeaderBuf = ByteBuffer.allocateDirect(4);
        this.bufWrDataPosRepsBuf = ByteBuffer.allocateDirect(3);
        this.bufWrDataCapPosRepsBuf = ByteBuffer.allocateDirect(5);
        this.bufWrDataHeaderBufs = new ByteBuffer[2];
    }

    public void writeToChannel() throws IOException {
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
                        if (bufWrData.repeatingBufWriter != null) {
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
                        } else {
                            bufWrDataHeaderBufs[0] = bufWrDataPosRepsBuf;

                            bufWrDataPosRepsBuf.clear();
                            bufWrDataPosRepsBuf.put((byte) 0);
                            bufWrDataPosRepsBuf.putShort((short) buf.position());
                            bufWrDataPosRepsBuf.flip();
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

    public void setReadyForChannelWrite() {
        if (!writing) {
            writing = true;
            changeSelectorInterestOps(SelectionKey.OP_WRITE);
            selector.wakeup();
        }
    }

    private void changeSelectorInterestOps(int ops) {
        channel.keyFor(selector).interestOps(ops);
    }

    public void queueWrData(PacketWriteData packetWriteData) {
        wrDataQueue.add(packetWriteData);
    }
}
