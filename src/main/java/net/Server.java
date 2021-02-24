package net;

import game.GameConstants;
import game.Room;
import net.handler.ActionHandler;
import net.handler.ChatActionHandler;
import net.handler.PingActionHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class Server implements Runnable {

    // Core
    private ServerSocket serverSocket;
    private final int port;

    private volatile boolean running = false;
    private volatile boolean listening = false;
    private volatile boolean shuttingDown = false;

    private final ScheduledExecutorService coreLogicExecutor = Executors.newScheduledThreadPool(1);

    // Connections
    private final HashMap<Integer, Client> clients = new HashMap<>();

    // World
    private final HashMap<String, Room> rooms = new HashMap<>();

    // Actions
    private final HashMap<Short, ActionHandler> actionHandlers = new HashMap<>();
    private final LinkedList<IncomingAction> incomingActions = new LinkedList<>();
    private final LinkedList<OutgoingAction> outgoingActions = new LinkedList<>();

    public Server(int port) {
        if(port > 65535 || port < 1) {
            throw new IllegalArgumentException("Port must be between 1 and 65535!");
        }
        this.port = port;
    }

    @Override
    public void run() {
        try {
            running = true;
            coreLogicExecutor.scheduleAtFixedRate(this::update, 0, GameConstants.TICK_LENGTH, TimeUnit.MILLISECONDS);
            System.out.println("Starting core logic loop");
            load();
            listening = true;
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(NetConstants.SERVER_ACCEPT_TIMEOUT);
            System.out.println("Listening on port " + port);
            while(listening) {
                try {
                    // Little bit of trickery here; if listen() throws an IOException, it's handled here.
                    // If listen() tries to throw a SocketTimeoutException, it's handled silently in that function.
                    Client client = listen();
                    if(client != null) {
                        new Thread(client).start();
                        addClient(client);
                    }
                } catch (IOException ignored) {
                    // Could not establish a connection with the client. Might be a problem with the streams.
                    // We will not print this error, as it is not catastrophic, and the client can reattempt connection.
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Network Server has encountered an error that is unrecoverable, like a port binding error.
            System.exit(NetConstants.NETWORK_SERVER_CATASTROPHE);
        }
    }

    private void load() {
        // Rooms
        loadRooms();

        // Action Handlers
        loadHandlers();
    }

    private void loadRooms() {
        rooms.put("Town", new Room());
    }

    private void loadHandlers() {
        actionHandlers.put(NetConstants.OpRecv.PING, new PingActionHandler());
        actionHandlers.put(NetConstants.OpRecv.CHAT, new ChatActionHandler());
    }

    private Client listen() throws IOException {
        try {
            Socket socket = serverSocket.accept();
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(NetConstants.CLIENT_TIMEOUT);
            return new Client(this,
                    socket,
                    new GMLInputStream(socket.getInputStream()),
                    new GMLOutputStream(socket.getOutputStream()));
        } catch (SocketTimeoutException ignored) {
            // No clients connected in the set timeout period; silently ignore and return null.
            return null;
        }
    }

    private void update() {
        if(running) {
            tick();
        } else if(!shuttingDown){
            shutdown();
        }
    }

    private void tick() {
        // Process incoming actions
        for(IncomingAction a : incomingActions) {
            if(a.getSource().canPerform(a.getOperation())) {
                actionHandlers.get(a.getOperation()).handle(a);
            } else {
                // Invalid action performed by client; terminate the connection.
                a.getSource().terminateClient();
            }
        }
        incomingActions.clear();

        // Process all of the tickable systems here.
        rooms.values().forEach(Room::tick);

        // Process outgoing actions
        for(OutgoingAction a : outgoingActions) a.send();
        outgoingActions.clear();
    }

    public void shutdown() {
        // TODO: May not need another variable; try with only the isTerminated() function
        if(shuttingDown) return;
        shuttingDown = true;
        System.out.println("Shutting down...");
        if(!coreLogicExecutor.isTerminated()){
            // If the server needs to shutdown and hasn't, attempt to begin the process.
            running = false;
            listening = false;
            coreLogicExecutor.shutdown();
            try {
                if (!coreLogicExecutor.awaitTermination(GameConstants.SERVER_SHUTDOWN_MAX_WAIT, TimeUnit.SECONDS)) {
                    coreLogicExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                // Server took too long to shutdown gracefully; force quit.
                coreLogicExecutor.shutdownNow();
            }
        }
    }

    public void addIncomingAction(IncomingAction action) {
        synchronized(this) {
            incomingActions.add(action);
        }
    }

    public void addOutgoingAction(OutgoingAction action) {
        synchronized(this) {
            outgoingActions.add(action);
        }
    }

    private void addClient(Client client) {
        synchronized(this) {
            clients.put(client.getId(), client);
            System.out.println("#" + client.getId() + " " + client.getIp() + "> Connected");
        }
    }

    public void removeClient(int id) {
        synchronized(this) {
            Client client;
            if((client = clients.remove(id)) != null) {
                // Get rid of any remaining actions from the client.
                clearActionsOf(client);
                System.out.println("#" + id + " " + client.getIp() + "> Disconnected");
            }
        }
    }

    private void clearActionsOf(Client client) {
        // Remove if from the provided client
        Predicate<? super IncomingAction> incomingFilter = a -> a.getSource().getId() == client.getId();
        incomingActions.removeIf(incomingFilter);

        Predicate<? super OutgoingAction> outgoingFilter = a -> a.getDestination().getId() == client.getId();
        outgoingActions.removeIf(outgoingFilter);
    }
}