package net;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class GMLInputStream extends DataInputStream {
    public GMLInputStream(InputStream in) {
        super(in);
    }

    public final short readGMShort() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (short) ((ch2 << 8) + ch1);
    }

    public final int readGMInt() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + ch1);
    }

    public final String readGMString() throws IOException {
        ByteBuffer b = ByteBuffer.allocate(1024);
        int size = 0;
        int c;
        while((c = in.read()) > 0) {
            b.put((byte) c);
            size++;
        }
        byte[] str = new byte[size];
        System.arraycopy(b.array(), 0, str, 0, size);
        return new String(str);
    }
}