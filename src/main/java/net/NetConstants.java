package net;

public class NetConstants {
    public static final int PORT = 2828;
    public static final int VERSION = 12282020;
    public static final String HANDSHAKE = "73rry cr3w5";
    public static final int CLIENT_TIMEOUT = 1000;
    public static final int NETWORK_SERVER_CATASTROPHE = -400;
    public static final int SERVER_ACCEPT_TIMEOUT = 1000;

    public static class OpRecv {
        public static final short PING = 1;

        public static final short AUTHENTICATE = 2;
        public static final short CHOOSE_CHARACTER = 3;

        public static final short CHAT = 3;
    }

    public static class OpSend {
        public static final short PING = 1;
    }
}