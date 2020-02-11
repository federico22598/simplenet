package com.github.idkp.simplenet;

import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.function.Predicate;

public interface ConnectionReviewer {
    ConnectionAttemptResult accept(Server server);

    void addFilter(Predicate<SocketChannel> filter);

    void removeFilter(Predicate<SocketChannel> filter);

    boolean hasFilter(Predicate<SocketChannel> filter);

    Set<Predicate<SocketChannel>> getFilters();
}