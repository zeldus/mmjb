package net;

import lombok.Getter;

public class OutgoingAction {
    @Getter private final Client destination;
    @Getter private final byte[] data;

    public OutgoingAction(Client destination, byte[] data) {
        this.destination = destination;
        this.data = data;
    }

    public void send() {
        destination.send(data);
    }
}
