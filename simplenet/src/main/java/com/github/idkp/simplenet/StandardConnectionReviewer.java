package com.github.idkp.simplenet;

import com.sun.javafx.collections.ObservableSetWrapper;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Predicate;

public class StandardConnectionReviewer implements ConnectionReviewer {
    private final ObservableSet<Predicate<SocketChannel>> filters = new ObservableSetWrapper<>(new HashSet<>());
    private final Function<SocketAddress, ConnectionConfiguration> configProvider;

    public StandardConnectionReviewer(Function<SocketAddress, ConnectionConfiguration> configProvider) {
        this.configProvider = configProvider;
    }

    @Override
    public ConnectionAttemptResult accept(Server server) {
        SocketChannel channel;

        try {
            channel = server.getChannel().accept();
        } catch (IOException e) {
            return new FailedConnectionAttemptResult(e);
        }

        for (Predicate<SocketChannel> filter : filters) {
            if (filter.test(channel)) {
                return new FilteredConnectionAttemptResult();
            }
        }

        SocketAddress address;

        try {
            address = channel.getRemoteAddress();
        } catch (IOException e) {
            return new FailedConnectionAttemptResult(e);
        }

        Selector selector = server.getSelector();
        ConnectionConfiguration config = configProvider.apply(address);
        PacketWriter packetWriter = new StandardPacketWriter(channel, selector, config);
        PacketReader packetReader = new StandardPacketReader(channel, config);
        ActiveConnection connection = new StandardActiveConnection(channel, packetWriter, packetReader);

        try {
            channel.configureBlocking(false);
            channel.register(server.getSelector(), SelectionKey.OP_READ,
                    new ServerSelectorKeyData(packetWriter, packetReader, connection));
        } catch (IOException e) {
            return new FailedConnectionAttemptResult(e);
        }

        return new OKConnectionAttemptResult(connection);
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