package com.github.idkp.simplenet;

import javafx.collections.ObservableSet;

import java.nio.channels.SocketChannel;
import java.util.function.Predicate;

public interface ConnectionReviewer {
    ConnectionAttemptResult accept(Server server);

    void addFilter(Predicate<SocketChannel> filter);

    void removeFilter(Predicate<SocketChannel> filter);

    boolean hasFilter(Predicate<SocketChannel> filter);

    ObservableSet<Predicate<SocketChannel>> getFilters();
}