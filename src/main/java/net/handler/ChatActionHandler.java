package net.handler;

import game.Player;
import game.Room;
import net.Client;
import net.IncomingAction;
import net.NetConstants;
import net.Serializer;

import java.io.IOException;

public class ChatActionHandler extends ActionHandler {
    @Override
    public void handle(IncomingAction action) {
        Client source = action.getSource();
        // Ensure client can perform this action
        if(!source.canPerform(action.getOperation())) return;
        // Broadcast chat message to everyone in room
        try {
            String message = action.getPayloadStream().readGMString();
            broadcast(source, message);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void broadcast(Client source, String message) {
        Player player = source.getPlayer();
        Room room = player.getRoom();

        Serializer s = new Serializer(source.getServer());
        // Start writing here...
        s.writeGMShort(NetConstants.OpRecv.CHAT);
        s.writeGMInt(source.getId()); // TODO: Use these session IDs as player IDs on client
        s.writeGMString(message);
        // Stop writing here.
        byte[] bytes = s.getBytes();

        // Send bytes to each player in source player's room
        for(Player p : room.getPlayers()) {
            p.getClient().send(bytes);
        }
    }
}