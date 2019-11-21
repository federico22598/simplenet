package com.github.idkp.simplenet;

import com.sun.javafx.collections.ObservableSetWrapper;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.function.Predicate;

public class StandardConnectionReviewer implements ConnectionReviewer {
    private final ObservableSet<Predicate<SocketChannel>> filters = new ObservableSetWrapper<>(new HashSet<>());

    @Override
    public ConnectionAttemptResult accept(Server server) {
        SocketChannel channel;
        SocketAddress channelAddr;

        try {
            channel = server.getChannel().accept();
            channelAddr = channel.getRemoteAddress();
        } catch (IOException e) {
            return ConnectionAttemptResult.failed(e);
        }

        for (Predicate<SocketChannel> filter : filters) {
            if (filter.test(channel)) {
                return ConnectionAttemptResult.filtered();
            }
        }

        PacketHandler packetHandler = new StandardPacketHandler();
        Selector selector = server.getSelector();
        PacketWriter packetWriter = new PacketWriter(channel, selector);
        ActiveConnection connection = new StandardActiveConnection(packetHandler, channel, selector, packetWriter);
        PacketReader packetReader = new PacketReader(channel);

        try {
            ActiveConnection activeConn = server.getConnection(channelAddr);

            if (activeConn != null) {
                server.closeConnection(activeConn);
            }

            channel.configureBlocking(false);
            channel.register(server.getSelector(), SelectionKey.OP_READ,
                    new ServerSelectorKeyData(packetWriter, packetReader, packetHandler, connection));
        } catch (IOException e) {
            return ConnectionAttemptResult.failed(e);
        }

        return ConnectionAttemptResult.succeeded(connection);
    }

    @Override
    public void addFilter(Predicate<SocketChannel> filter) {
        filters.add(filter);
    }

    @Override
    public void removeFilter(Predicate<SocketChannel> filter) {
        filters.remove(filter);
    }

    @Override
    public boolean hasFilter(Predicate<SocketChannel> filter) {
        return filters.contains(filter);
    }

    @Override
    public ObservableSet<Predicate<SocketChannel>> getFilters() {
        return filters;
    }
}