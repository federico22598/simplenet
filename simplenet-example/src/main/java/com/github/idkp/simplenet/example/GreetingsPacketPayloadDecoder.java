package com.github.idkp.simplenet.example;

import com.github.idkp.simplenet.PayloadDecoder;

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
    public String fetchResult() {
        return payload;
    }
}
