package se.kth.ict.id1212.minor.hangman.server.net;

import se.kth.ict.id1212.minor.hangman.server.db.Word_DB;
import se.kth.ict.id1212.minor.hangman.server.controller.Controller;
import se.kth.ict.id1212.minor.hangman.common.Constants;
import se.kth.ict.id1212.minor.hangman.common.MsgType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ForkJoinPool;


public class ClientHandler implements Runnable {

    private final Server server;
    private final SocketChannel clientChannel;
    private final Controller controller;
    private ByteBuffer messageFromClient = ByteBuffer.allocateDirect(Constants.MAX_MSG_LENGTH);
    private ByteBuffer messageToClient = ByteBuffer.allocateDirect(Constants.MAX_MSG_LENGTH);
    private SelectionKey key;

    private String fromClient;
    private String toClient;


    public ClientHandler(Server server, SocketChannel clientChannel, Word_DB words) {
        this.server = server;
        this.clientChannel = clientChannel;
        controller = new Controller(words);
        createWelcomeMessage();
    }

    private void createWelcomeMessage() {
        makeMessageReady("Welcome to Hangman!\n" +
                "'play' to play or 'quit' to quit game.");
    }

    private void makeMessageReady(String message) {
        messageToClient = ByteBuffer.wrap(message.getBytes());
    }

    void receiveMessage(SelectionKey key) throws IOException {
        this.key = key;
        messageFromClient.clear();
        int numOfReadBytes;
        numOfReadBytes = clientChannel.read(messageFromClient);
        if (numOfReadBytes == -1) {
            throw new IOException("Client has closed connection.");
        }
        ForkJoinPool.commonPool().execute(this);
    }

    private String extractMessageFromBuffer() {
        messageFromClient.flip();
        byte[] bytes = new byte[messageFromClient.remaining()];
        messageFromClient.get(bytes);
        return new String(bytes);
    }

    void sendMessage() {
        try {
            clientChannel.write(messageToClient);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readyForWrite(){
        key.interestOps(SelectionKey.OP_WRITE);
        server.wakeUp();
    }

    @Override
    public void run() {
        fromClient = extractMessageFromBuffer();
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

    public void disconnectClient() {
        try {
            clientChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
    private void disconnect() {
        try {
            clientSocket.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        connected = false;
    }
    */

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
