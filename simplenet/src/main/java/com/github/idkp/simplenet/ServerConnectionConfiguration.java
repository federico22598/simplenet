package com.github.idkp.simplenet;

import java.net.SocketAddress;

public interface ServerConnectionConfiguration extends ConnectionConfiguration {
    SocketAddress getAddress();
}
