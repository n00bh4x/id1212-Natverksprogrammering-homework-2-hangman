package se.kth.ict.id1212.minor.hangman.client.controller;

import se.kth.ict.id1212.minor.hangman.client.net.ServerHandler;
import se.kth.ict.id1212.minor.hangman.client.view.ServerObserver;

public class Controller {
    private ServerHandler serverHandler;

    public Controller(String address, int portNumber, ServerObserver outputHandler) {
        serverHandler = new ServerHandler(address, portNumber, outputHandler);
    }

    public void sendMessage(String fromUser) {
        serverHandler.sendMessage(fromUser);
    }

    public void disconnect() {
        serverHandler.disconnect();
    }
}
