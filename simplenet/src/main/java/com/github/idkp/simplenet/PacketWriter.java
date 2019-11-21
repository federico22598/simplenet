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
    private final ByteBuffer bufWrDataRepsBuf;
    private final ByteBuffer bufWrDataCapRepsBuf;
    private final ByteBuffer[] bufWrDataHeaderBufs;
    private final ByteBuffer[] bufWrDataPayloadRepsBufs;
    private boolean writing;
    private boolean completedLastHeaderWrite;
    private boolean startedLastHeaderWrite;
    private boolean completedWrDataHeaderWrite;
    private boolean startedWrDataHeaderWrite;
    private boolean bufWrDataRepeat;
    private boolean updatedPayloadBufArr;
    private boolean wroteRepsBuf;

    public PacketWriter(SocketChannel channel, Selector selector) {
        this.channel = channel;
        this.selector = selector;
        this.wrDataQueue = new ArrayDeque<>();
        this.wrDataHeaderBuf = ByteBuffer.allocateDirect(4);
        this.bufWrDataRepsBuf = ByteBuffer.allocateDirect(1);
        this.bufWrDataCapRepsBuf = ByteBuffer.allocateDirect(3);
        this.bufWrDataHeaderBufs = new ByteBuffer[2];
        this.bufWrDataPayloadRepsBufs = new ByteBuffer[2];

        this.bufWrDataHeaderBufs[0] = bufWrDataCapRepsBuf;
        this.bufWrDataPayloadRepsBufs[0] = bufWrDataRepsBuf;
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

                if (completedWrDataHeaderWrite) {
                    if (wroteRepsBuf) {
                        channel.write(buf);
                    } else {
                        channel.write(bufWrDataPayloadRepsBufs);
                    }
                } else {
                    if (!startedWrDataHeaderWrite) {
                        if (bufWrData.repeatingBufWriter != null) {
                            bufWrDataRepeat = bufWrData.repeatingBufWriter.repeat();
                        }

                        bufWrDataCapRepsBuf.clear();
                        bufWrDataCapRepsBuf.putShort((short) buf.position());
                        bufWrDataCapRepsBuf.put(bufWrDataRepeat ? (byte) 1 : (byte) 0);
                        bufWrDataCapRepsBuf.flip();
                        buf.flip();

                        bufWrDataHeaderBufs[1] = buf;
                        startedWrDataHeaderWrite = true;
                    }

                    channel.write(bufWrDataHeaderBufs);

                    if (bufWrDataCapRepsBuf.hasRemaining()) {
                        return;
                    }

                    bufWrDataCapRepsBuf.clear();

                    completedWrDataHeaderWrite = true;
                    wroteRepsBuf = true;
                }

                if (buf.hasRemaining()) {
                    if (!updatedPayloadBufArr) {
                        bufWrDataPayloadRepsBufs[1] = buf;
                        updatedPayloadBufArr = true;
                    }

                    return;
                }

                if (bufWrDataRepeat) {
                    buf.clear();
                    //noinspection ConstantConditions
                    bufWrDataRepeat = bufWrData.repeatingBufWriter.repeat();

                    buf.flip();

                    if (!updatedPayloadBufArr) {
                        bufWrDataPayloadRepsBufs[1] = buf;
                        updatedPayloadBufArr = true;
                    }

                    bufWrDataRepsBuf.clear();
                    bufWrDataRepsBuf.put(bufWrDataRepeat ? (byte) 1 : (byte) 0);
                    bufWrDataRepsBuf.flip();

                    wroteRepsBuf = false;

                    continue;
                }

                completedWrDataHeaderWrite = false;
                startedWrDataHeaderWrite = false;
                bufWrDataRepeat = false;
                updatedPayloadBufArr = false;
                wroteRepsBuf = false;

                bufWrDataQueue.remove();
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
