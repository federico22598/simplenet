package com.github.idkp.simplenet;

public final class ServerSelectorKeyData {
    public final PacketWriter packetWriter;
    public final PacketReader packetReader;
    public final ActiveConnection conn;

    public ServerSelectorKeyData(PacketWriter packetWriter, PacketReader packetReader, ActiveConnection conn) {
        this.packetWriter = packetWriter;
        this.packetReader = packetReader;
        this.conn = conn;
    }
}
