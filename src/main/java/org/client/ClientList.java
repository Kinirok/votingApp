package org.client;
import io.netty.channel.Channel;
import java.util.concurrent.ConcurrentHashMap;

public class ClientList {
    private final ConcurrentHashMap<Channel, ClientData> clients = new ConcurrentHashMap<>();

    public void addClient(Channel channel) {
        clients.put(channel, new ClientData());
    }

    public void removeClient(Channel channel) {
        clients.remove(channel);
    }

    public ClientData getClientData(Channel channel) {
        return clients.get(channel);
    }
}