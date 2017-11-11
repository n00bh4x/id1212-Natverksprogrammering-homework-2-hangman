package se.kth.ict.id1212.minor.hangman.server.net;

import se.kth.ict.id1212.minor.hangman.server.db.Word_DB;
import se.kth.ict.id1212.minor.hangman.server.controller.Controller;
import se.kth.ict.id1212.minor.hangman.common.Constants;
import se.kth.ict.id1212.minor.hangman.common.MsgType;
import se.kth.ict.id1212.minor.hangman.common.Communication;

import java.io.IOException;
import java.net.Socket;


public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private Communication communication;
    private final Controller controller;
    private boolean connected;
    private String fromClient;
    private String toClient;

    public ClientHandler(Socket clientSocket, Word_DB words) {
        this.clientSocket = clientSocket;
        connected = true;
        communication = new Communication(this.clientSocket);
        controller = new Controller(words);
    }

    @Override
    public void run() {
        communication.sendMessage("Welcome to Hangman!");
        communication.sendMessage("'play' to play or 'quit' to quit game.");
        while(connected) {
            fromClient = communication.receiveMessage();
            Message message = new Message(fromClient);
            switch (message.msgType) {
                case PLAY:
                    controller.newGame();
                    toClient = controller.getMessage();
                    communication.sendMessage(toClient);
                    break;
                case QUIT:
                    communication.sendMessage("Thanks for playing Hangman!");
                    disconnect();
                    break;
                case GUESS:
                    controller.handleGuess(message.guess);
                    toClient = controller.getMessage();
                    communication.sendMessage(toClient);
                    break;
                default:
                    communication.sendMessage("Input not allowed. Try again");
            }
        }
    }

    private void disconnect() {
        try {
            clientSocket.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        connected = false;
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
