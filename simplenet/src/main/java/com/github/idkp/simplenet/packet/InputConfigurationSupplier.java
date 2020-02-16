package com.github.idkp.simplenet.packet;

import com.github.idkp.simplenet.ServerClient;

public interface InputConfigurationSupplier {
    PacketPipeInputConfiguration get(ServerClient client);
}
