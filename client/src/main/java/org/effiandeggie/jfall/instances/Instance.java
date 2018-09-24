package org.effiandeggie.jfall.instances;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Instance {

    private final String name;
    private final String host;
    private final int port;

    public String getKey() {
        return host + ":" + port;
    }

}
