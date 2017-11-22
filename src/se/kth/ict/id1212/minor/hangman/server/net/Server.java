package se.kth.ict.id1212.minor.hangman.server.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import se.kth.ict.id1212.minor.hangman.server.db.Word_DB;

public class Server {
    private final Word_DB wordList;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;


    public Server(int port, final Word_DB wordList) {
        this.wordList = wordList;
        serve(port);
    }

    private void serve(int port){
        try {
            selector = Selector.open();
            initServerSocketChannel(port);
            while(true){
                    selector.select();
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while(iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if(!key.isValid()) {
                            continue;
                        }
                        if(key.isAcceptable()) {
                            startHandler(key);
                        } else if(key.isReadable()) {
                            receiveMessageFromClient(key);
                        } else if(key.isWritable()) {
                            sendMessageToClient(key);
                        }
                    }
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void startHandler(SelectionKey key) {
        try{
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            SocketChannel clientChannel = serverSocketChannel.accept();
            clientChannel.configureBlocking(false);
            ClientHandler clientHandler = new ClientHandler(this, clientChannel, wordList);
            clientChannel.register(this.selector, SelectionKey.OP_WRITE, clientHandler);
            clientChannel.setOption(StandardSocketOptions.SO_LINGER, 5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToClient(SelectionKey key) {
        try {
            ClientHandler client = (ClientHandler) key.attachment();
            client.sendMessage();
            key.interestOps(SelectionKey.OP_READ);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void receiveMessageFromClient(SelectionKey key) {
        ClientHandler client = (ClientHandler) key.attachment();
        try {
            client.receiveMessage(key);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void removeClient(SelectionKey clientKey) throws IOException {
        ClientHandler client = (ClientHandler) clientKey.attachment();
        client.disconnectClient();
        clientKey.cancel();
    }


    private void initServerSocketChannel(int port) {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void wakeUp() {
        selector.wakeup();
    }
}
