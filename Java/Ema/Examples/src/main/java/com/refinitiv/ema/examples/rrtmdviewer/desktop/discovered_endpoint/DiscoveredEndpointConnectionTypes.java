package com.refinitiv.ema.examples.rrtmdviewer.desktop.discovered_endpoint;

import com.refinitiv.ema.access.ServiceEndpointDiscoveryOption;

public enum DiscoveredEndpointConnectionTypes {

    ENCRYPTED_SOCKET("Encrypted-Socket",
            ServiceEndpointDiscoveryOption.TransportProtocol.TCP,
            ServiceEndpointDiscoveryOption.DataformatProtocol.RWF
    ),

    ENCRYPTED_WEBSOCKET("Encrypted-WebSocket",
            ServiceEndpointDiscoveryOption.TransportProtocol.WEB_SOCKET,
            ServiceEndpointDiscoveryOption.DataformatProtocol.JSON2
    );

    private final String textLabel;

    private final int transportProtocol;

    private final int dataFormatProtocol;

    @Override
    public String toString() {
        return textLabel;
    }

    public int getTransportProtocol() {
        return transportProtocol;
    }

    DiscoveredEndpointConnectionTypes(String textLabel, int transportProtocol, int dataFormatProtocol) {
        this.textLabel = textLabel;
        this.transportProtocol = transportProtocol;
        this.dataFormatProtocol = dataFormatProtocol;
    }
}
