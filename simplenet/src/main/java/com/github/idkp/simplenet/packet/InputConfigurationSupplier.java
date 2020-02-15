package com.github.idkp.simplenet.packet;

import com.github.idkp.simplenet.ServerClient;

import java.util.function.Supplier;

public interface InputConfigurationSupplier {
    PacketPipeInputConfiguration get(ServerClient client);
}
