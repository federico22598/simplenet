package com.github.idkp.simplenet.packet;

import com.github.idkp.simplenet.ServerClient;

public interface OutputConfigurationSupplier {
    PacketPipeOutputConfiguration get(ServerClient client);
}
