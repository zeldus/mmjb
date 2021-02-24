package net;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class GMLOutputStream extends DataOutputStream {
    public GMLOutputStream(OutputStream out) {
        super(out);
    }

    public final void writeGMShort(int v) throws IOException {
        out.write(v & 0xFF);
        out.write((v >>> 8) & 0xFF);
        incCount(2);
    }

    public final void writeGMInt(int v) throws IOException {
        out.write(v & 0xFF);
        out.write((v >>>  8) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 24) & 0xFF);
        incCount(4);
    }

    public final void writeGMString(String s) throws IOException {
        int size = s.getBytes(StandardCharsets.UTF_8).length + 1;
        ByteBuffer b = ByteBuffer.allocate(size);
        b.put(s.getBytes(StandardCharsets.UTF_8));
        b.put((byte) 0);
        out.write(b.array());
        incCount(size);
    }

    private void incCount(int value) {
        int temp = written + value;
        if (temp < 0) {
            temp = Integer.MAX_VALUE;
        }
        written = temp;
    }
}