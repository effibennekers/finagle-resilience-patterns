package org.effiandeggie.jfall;

public class Instance {

    private final String name;
    private final int port;

    public Instance(final String name, final int port) {
        this.name = name;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }
}
