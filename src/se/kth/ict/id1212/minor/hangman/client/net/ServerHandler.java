package se.kth.ict.id1212.minor.hangman.client.net;


import se.kth.ict.id1212.minor.hangman.common.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class ServerHandler implements Runnable {
    private Selector selector;
    private SocketChannel socketChannel;
    private InetSocketAddress serverAddress;
    private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
    private final ByteBuffer messageFromServer = ByteBuffer.allocateDirect(Constants.MAX_MSG_LENGTH);
    private OutputHandler outputHandler;
    private String address;
    private int portNumber;
    private Thread thread;

    private boolean connected;
    private boolean timeToSend;
    private boolean goodByeMessageReceived;

    public ServerHandler(String address, int portNumber, OutputHandler outputHandler) {
        this.address = address;
        this.portNumber = portNumber;
        this.outputHandler = outputHandler;
        timeToSend = false;
        connected = true;
        goodByeMessageReceived = false;
        thread = new Thread(this);
        thread.start();
    }

    private void initSelector() {
        try {
            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initConnection() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(serverAddress);
            connected = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    private void connect(String host, int port) {
        serverAddress = new InetSocketAddress(host, port);
    }

    private void completeConnection(SelectionKey key) throws IOException {
        socketChannel.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
    }


    @Override
    public void run() {
        try {
            connect(address, portNumber);
            initConnection();
            initSelector();
            while (connected || !messagesToSend.isEmpty() || !goodByeMessageReceived) {
                if (timeToSend) {
                    socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    timeToSend = false;
                }
                selector.select();
                for (SelectionKey key : selector.selectedKeys()) {
                    selector.selectedKeys().remove(key);
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isConnectable()) {
                        completeConnection(key);
                    } else if (key.isReadable()) {
                        receiveFromServer();
                    } else if (key.isWritable()) {
                        sendToServer(key);
                    }
                }
            }

            doDisconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendMessage(String message) {
        synchronized (messagesToSend) {
            messagesToSend.add(ByteBuffer.wrap(message.getBytes()));
        }
        timeToSend = true;
        selector.wakeup();
    }

    private void sendToServer(SelectionKey key) throws IOException {
        ByteBuffer msg;
        synchronized (messagesToSend) {
            while ((msg = messagesToSend.peek()) != null) {
                socketChannel.write(msg);
                if (msg.hasRemaining()) {
                    return;
                }
                messagesToSend.remove();
            }

        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private void receiveFromServer() {
        messageFromServer.clear();
        int numOfReadBytes = 0;
        try {
            numOfReadBytes = socketChannel.read(messageFromServer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (numOfReadBytes == -1) {
            //System.out.println("FAIL");
            return;
        }
        String receivedMessage = extractMessageFromBuffer();
        if(receivedMessage.equalsIgnoreCase("Thanks for playing Hangman!")) {
            goodByeMessageReceived = true;
        }
        notifyUser(receivedMessage);
    }

    private void notifyUser(String message) {
        CompletableFuture.runAsync(() -> outputHandler.handleMessage(message));
    }

    private String extractMessageFromBuffer() {
        messageFromServer.flip();
        byte[] bytes = new byte[messageFromServer.remaining()];
        messageFromServer.get(bytes);
        return new String(bytes);
    }

    public void disconnect() {
        connected = false;
    }
    public void doDisconnect() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        socketChannel.keyFor(selector).cancel();
    }
}
