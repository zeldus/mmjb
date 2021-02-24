package net.handler;

import net.*;

import java.io.IOException;

public class PingActionHandler extends ActionHandler {
    @Override
    public void handle(IncomingAction action) {
        Client source = action.getSource();
        // Ensure client can perform this action
        if(!source.canPerform(action.getOperation())) return;
        // Broadcast chat message to everyone in room
        try {
            int timeProvided = action.getPayloadStream().readGMInt();
            pong(source, timeProvided);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void pong(Client source, int time) {
        Serializer s = new Serializer(source.getServer());
        // Start writing here...
        s.writeGMShort(NetConstants.OpSend.PING);
        s.writeGMInt(time);
        // Stop writing here.
        byte[] bytes = s.getBytes();

        // Send bytes back to player
        source.getServer().addOutgoingAction(new OutgoingAction(source, bytes));
    }
}
