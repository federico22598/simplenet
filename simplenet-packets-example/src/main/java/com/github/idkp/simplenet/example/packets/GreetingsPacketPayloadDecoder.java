package com.github.idkp.simplenet.example.packets;

import com.github.idkp.simplenet.packet.PayloadDecoder;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class GreetingsPacketPayloadDecoder implements PayloadDecoder<String> {
    private String payload;

    @Override
    public void decode(ByteBuffer buf) {
        byte[] msgBytes = new byte[buf.getInt()];
        buf.get(msgBytes);

        payload = new String(msgBytes, StandardCharsets.UTF_8);
    }

    @Override
    public String getResult() {
        return payload;
    }
}
