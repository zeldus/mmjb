package net;

import lombok.Getter;

import java.io.ByteArrayInputStream;

public class IncomingAction {;
    @Getter private final Client source;
    private final short size;
    @Getter private final short operation;
    @Getter private final GMLInputStream payloadStream;

    public IncomingAction(Client source, short size, short operation, byte[] payload) {
        this.source = source;
        this.size = size;
        this.operation = operation;
        this.payloadStream = new GMLInputStream(new ByteArrayInputStream(payload));
    }
}
