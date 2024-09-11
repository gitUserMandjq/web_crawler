package com.crawler.base.common.model;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.*;

public class ProxySocketFactory extends SocketFactory {

    private final Proxy proxy;

    public ProxySocketFactory(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public Socket createSocket() {
        return new Socket(proxy);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        Socket socket = new Socket(proxy);
        socket.connect(new InetSocketAddress(host, port));
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress address, int port) throws IOException {
        Socket socket = new Socket(proxy);
        socket.connect(new InetSocketAddress(address, port));
        return new Socket(address, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress clientAddress, int clientPort) throws IOException, UnknownHostException {
        Socket socket = new Socket(proxy);
        socket.bind(new InetSocketAddress(clientAddress, clientPort));
        socket.connect(new InetSocketAddress(host, port));
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort) throws IOException {
        Socket socket = new Socket(proxy);
        socket.bind(new InetSocketAddress(clientAddress, clientPort));
        socket.connect(new InetSocketAddress(address, port));
        return socket;
    }
}
