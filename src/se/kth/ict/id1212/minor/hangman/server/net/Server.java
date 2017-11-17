package se.kth.ict.id1212.minor.hangman.server.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;

import se.kth.ict.id1212.minor.hangman.server.db.Word_DB;

public class Server {
    private ServerSocket serverSocket;
    private final Word_DB WORDS;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;


    public Server(int port, final Word_DB WORDS) {
        this.WORDS = WORDS;
        initServerSocketChannel(port);
        try {
            this.serverSocket = new ServerSocket(port);
            selector = Selector.open();

            serve();
        } catch(Exception e) {
        }
    }

    private void serve(){
        while(true){
            try {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setSoLinger(true, 5000);
                new Thread(new ClientHandler(clientSocket, WORDS)).start();
            } catch (IOException e) {
            }
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

    private void startHandler(SelectionKey key) {
    }

    private class Client {
        private final ClientHandler clientHandler;
        private final Queue<ByteBuffer> messageToClient;

        private Client(ClientHandler handler) {
            clientHandler = handler;
            messageToClient = new ArrayDeque<>();
        }

        private synchronized void putMessageInQueue(ByteBuffer message) {

        }
    }
}
