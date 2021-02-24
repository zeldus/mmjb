package net;

import game.Player;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
public class Client implements Runnable {
    private static int idCounter = 0;

    @Getter private final int id = idCounter++;
    @Getter private final Server server;
    private final Socket socket;
    @Getter private final GMLInputStream in;
    @Getter private final GMLOutputStream out;

    @Getter @Setter private Player player;
    private volatile boolean listening = false;

    @Getter private ClientState state = ClientState.WAITING_FOR_HANDSHAKE;

    public Client(Server server, Socket socket, GMLInputStream in, GMLOutputStream out) {
        this.server = server;
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        listen();
    }

    private void listen() {
        listening = true;
        try {
            int version = in.readGMInt();
            if(version != NetConstants.VERSION) {
                // Wrong version provided; terminate the connection.
                terminateClient();
            }
            String handshake = in.readGMString();
            if(handshake.equals(NetConstants.HANDSHAKE)) {
                // Handshake accepted; valid connection established.
                System.out.println("Valid handshake!");
                state = ClientState.HANDSHAKE_APPROVED;
                keepListening();
            } else {
                // Wrong handshake provided; terminate the connection.
                terminateClient();
            }
        } catch (SocketTimeoutException ignored) {
            // Socket timed out; Terminate the connection.
            terminateClient();
        } catch (IOException e) {
            // Incorrect handshake procedure or another error occurred; Terminate the connection.
            terminateClient(e);
        }
    }

    private void keepListening() {
        while(listening) {
            try {
                short size = in.readGMShort();
                short operation = in.readGMShort();
                byte[] payload = in.readNBytes(size - 2);
                IncomingAction action = new IncomingAction(this, size, operation, payload);
                // Add the new action the the server's queue.
                server.addIncomingAction(action);
            } catch (SocketTimeoutException ignored) {
                // Socket timed out; Terminate the connection.
                terminateClient();
            } catch (IOException e) {
                // Some network error occurred. Could've been a bad message, or a dropped connection.
                terminateClient(e);
            }
        }
        terminateClient();
    }

    public void send(byte[] bytes) {
        try {
            out.write(bytes);
            out.flush(); // Not sure if needed
        } catch (IOException e) {
            // Error writing to the client; could have lost connection or improperly closed the game.
            terminateClient(e);
        }
    }

    public String getIp() {
        return socket.getInetAddress().getHostAddress();
    }

    public void terminateClient() {
        // TODO: Handle different client states during termination (e.g., logging out of player session)
        listening = false;
        if(state == ClientState.IN_GAME) {
            logout();
        }
        state = ClientState.DISCONNECTED;
        server.removeClient(id);
    }

    private void logout() {
        // TODO: Implement
    }

    public void terminateClient(Exception e) {
        terminateClient();
        e.printStackTrace();
    }

    public boolean canPerform(short op) {
        // TODO: Flesh out Op codes and move the approvedOps definitions to a static method
        switch (state) {
            case WAITING_FOR_HANDSHAKE -> {
                // Should not be getting any Op codes here.
                return false;
            }
            case HANDSHAKE_APPROVED -> {
                ArrayList<Short> approvedOps = new ArrayList<>();
                approvedOps.add(NetConstants.OpRecv.PING);
                approvedOps.add(NetConstants.OpRecv.AUTHENTICATE);
                return approvedOps.contains(op);
            }
            case AUTHENTICATING -> {
                return op == NetConstants.OpRecv.PING;
            }
            case CHOOSING_CHARACTER -> {
                ArrayList<Short> approvedOps = new ArrayList<>();
                approvedOps.add(NetConstants.OpRecv.PING);
                approvedOps.add(NetConstants.OpRecv.CHOOSE_CHARACTER);
                return approvedOps.contains(op);
            }
            case IN_GAME -> {
                ArrayList<Short> approvedOps = new ArrayList<>();
                approvedOps.add(NetConstants.OpRecv.PING);
                approvedOps.add(NetConstants.OpRecv.CHAT);
                return approvedOps.contains(op);
            }
            case DISCONNECTED -> {
                // Stale action executed; TODO: should add a cleanser for stale actions to terminateClient();
                System.out.println("Stale action attempted!");
                return false;
            }
        }
        return false;
    }
}
