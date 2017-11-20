package se.kth.ict.id1212.minor.hangman.server.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

import se.kth.ict.id1212.minor.hangman.server.db.Word_DB;

public class Server {
    private ServerSocket serverSocket;
    private final Word_DB wordList;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;


    public Server(int port, final Word_DB wordList) {
        this.wordList = wordList;
        try {
            this.serverSocket = new ServerSocket(port);
            selector = Selector.open();
            initServerSocketChannel(port);
            serve();
        } catch(Exception e) {
        }
    }

    private void serve(){
        while(true){
            try {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while(iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if(!key.isValid()) {
                        continue;
                    }
                    if(key.isAcceptable()) {
                        startHandler(key);
                    } else if(key.isReadable()) {
                        receiveMessageFromClient(key);
                        configureInterest(key);
                    } else if(key.isWritable()) {
                        sendMessageToClient(key);
                        configureInterest(key);
                    }
                }
            } catch (IOException e) {
            }
        }
    }

    private void configureInterest(SelectionKey key) {
        if(key.interestOps() == (1 << 0)) {
            key.interestOps(SelectionKey.OP_WRITE);
        } else {
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void startHandler(SelectionKey key) {
        try{
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            SocketChannel clientChannel = serverSocketChannel.accept();
            clientChannel.configureBlocking(false);
            ClientHandler clientHandler = new ClientHandler(this, clientChannel, wordList);
            Client client = new Client(clientHandler);
            clientChannel.register(this.selector, SelectionKey.OP_WRITE, client);
            clientChannel.setOption(StandardSocketOptions.SO_LINGER, 5000);
        } catch (IOException e) {
            // do something
        }
    }

    private void sendMessageToClient(SelectionKey key) {
        Client client = (Client) key.attachment();
        client.sendMessage();
    }

    private void receiveMessageFromClient(SelectionKey key) {
        try {
            Client client = (Client) key.attachment();
            client.clientHandler.receiveMessage();
        } catch (Exception e){
            // Do something
        }
    }

    private void initServerSocketChannel(int port) {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
        }
    }

    public void wakeUp() {
        selector.wakeup();
    }


    private class Client {
        private final ClientHandler clientHandler;

        private Client(ClientHandler handler) {
            clientHandler = handler;
        }

        private void sendMessage() {
            clientHandler.sendMessage();
        }
    }
}
