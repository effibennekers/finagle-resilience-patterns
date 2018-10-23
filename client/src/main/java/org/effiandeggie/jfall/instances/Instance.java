package org.effiandeggie.jfall.instances;

public class Instance {

    private final String name;
    private final String host;
    private final int port;

    public Instance(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
    }

    public String getKey() {
        return host + ":" + port;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
