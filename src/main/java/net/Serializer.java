package net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Serializer {
    private ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
    private GMLOutputStream buffer = new GMLOutputStream(baos);
    private Server server;

    public Serializer(Server server) {
        this.server = server;
    }

    public byte[] getBytes() {
        // Move to new byte[] for length prefixing
        int tempSize = buffer.size();
        byte[] bytes = new byte[2 + tempSize];
        System.arraycopy(baos.toByteArray(), 0, bytes, 2, tempSize);

        // Add length prefix using a GMShort
        bytes[0] = (byte) (tempSize & 0xFF);
        bytes[1] = (byte) ((tempSize >>> 8) & 0xFF);

        return bytes;
    }

    public void writeGMShort(int v) {
        try {
            buffer.writeGMShort(v);
        } catch (IOException e) {
            internalWriteFail(e);
        }
    }

    public void writeGMInt(int v) {
        try {
            buffer.writeGMInt(v);
        } catch (IOException e) {
            internalWriteFail(e);
        }
    }

    public void writeGMString(String s) {
        try {
            buffer.writeGMString(s);
        } catch (IOException e) {
            internalWriteFail(e);
        }
    }

    public void writeByte(int v) {
        try {
            buffer.writeByte(v);
        } catch (IOException e) {
            internalWriteFail(e);
        }
    }

    private void internalWriteFail(IOException e) {
        // This should really never happen! If it does, all hell has broken loose.
        e.printStackTrace();
        // Attempt to shut the server down gracefully.
        server.shutdown();
    }
}