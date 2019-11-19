package pl.edu.mimuw.cloudatlas.agent;

import java.io.OutputStream;

class NoopOutputStream extends OutputStream {
    public void write(int b) {}
}
