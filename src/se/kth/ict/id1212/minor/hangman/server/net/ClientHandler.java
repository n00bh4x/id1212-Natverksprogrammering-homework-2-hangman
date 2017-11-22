package se.kth.ict.id1212.minor.hangman.server.net;

import se.kth.ict.id1212.minor.hangman.server.db.Word_DB;
import se.kth.ict.id1212.minor.hangman.server.controller.Controller;
import se.kth.ict.id1212.minor.hangman.common.Constants;
import se.kth.ict.id1212.minor.hangman.common.MsgType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ForkJoinPool;


public class ClientHandler implements Runnable {

    private final Server server;
    private final SocketChannel clientChannel;
    private final Controller controller;
    private final Queue<ByteBuffer> messagesToSend;
    private final Queue<ByteBuffer> receivedMessages;
    private ByteBuffer messageFromClient = ByteBuffer.allocateDirect(Constants.MAX_MSG_LENGTH);
    private SelectionKey key;

    private String fromClient;
    private String toClient;


    public ClientHandler(Server server, SocketChannel clientChannel, Word_DB words) {
        this.server = server;
        this.clientChannel = clientChannel;
        controller = new Controller(words);
        messagesToSend = new ArrayDeque<>();
        receivedMessages = new ArrayDeque<>();
        welcomeMessage();
    }

    private void welcomeMessage() {
        makeMessageReady("Welcome to Hangman!\n" +
                "'play' to play or 'quit' to quit game.");
    }

    private synchronized void makeMessageReady(String message) {
        messagesToSend.add(ByteBuffer.wrap(message.getBytes()));
    }

    void receiveMessage(SelectionKey key) throws IOException {
        this.key = key;
        messageFromClient.clear();
        int numOfReadBytes;
        numOfReadBytes = clientChannel.read(messageFromClient);
        synchronized (receivedMessages) {
            receivedMessages.add(messageFromClient);
        }
        if (numOfReadBytes == -1) {
            throw new IOException("Client has closed connection.");
        }
        ForkJoinPool.commonPool().execute(this);
    }

    private String extractMessageFromBuffer(ByteBuffer msg) {
        msg.flip();
        byte[] bytes = new byte[msg.remaining()];
        msg.get(bytes);
        return new String(bytes);
    }

    void sendMessage() throws IOException{
        ByteBuffer msg;
        synchronized (messagesToSend) {
            while ((msg = messagesToSend.peek()) != null) {
                clientChannel.write(msg);
                messagesToSend.remove();
            }
        }

    }

    private void readyForWrite(){
        key.interestOps(SelectionKey.OP_WRITE);
        server.wakeUp();
    }

    @Override
    public void run() {
        ByteBuffer msg;
        boolean moreMessages;
        synchronized (receivedMessages) {
            moreMessages = receivedMessages.peek() != null;
        }

        while (moreMessages) {
            synchronized (receivedMessages) {
                msg = receivedMessages.peek();
                receivedMessages.remove();
                moreMessages = receivedMessages.peek() != null;
            }
            fromClient = extractMessageFromBuffer(msg);
            Message message = new Message(fromClient);
            switch (message.msgType) {
                case PLAY:
                    controller.newGame();
                    toClient = controller.getMessage();
                    makeMessageReady(toClient);
                    readyForWrite();
                    break;
                case QUIT:
                    makeMessageReady("Thanks for playing Hangman!");
                    readyForWrite();
                    break;
                case GUESS:
                    controller.handleGuess(message.guess);
                    toClient = controller.getMessage();
                    makeMessageReady(toClient);
                    readyForWrite();
                    break;
                default:
                    makeMessageReady("Input not allowed. Try again");
                    readyForWrite();
            }
        }
    }

    public void disconnectClient() {
        try {
            clientChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Message {
        private MsgType msgType;
        private String guess;

        private Message(String receivedString) {
            parse(receivedString);
        }

        private void parse(String strToParse) {
            try {
                String[] msgTokens = strToParse.split(Constants.MSG_DELIMITER);
                msgType = MsgType.valueOf(msgTokens[Constants.MSG_TYPE_INDEX].toUpperCase());
                if (msgType != MsgType.GUESS && hasBody(msgTokens)) {
                    msgType = MsgType.INVALID;
                } else if (msgType == MsgType.GUESS && !hasBody(msgTokens)) {
                    msgType = MsgType.INVALID;
                } else if (msgType == MsgType.GUESS && hasBody(msgTokens)){
                    guess = msgTokens[Constants.MSG_BODY_INDEX];
                }
            } catch (Exception e) {
                msgType = MsgType.INVALID;
            }
        }

        private boolean hasBody(String[] msgTokens) {
            return msgTokens.length > 1;
        }
    }
}
