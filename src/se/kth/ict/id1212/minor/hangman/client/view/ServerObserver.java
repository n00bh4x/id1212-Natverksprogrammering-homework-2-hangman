package se.kth.ict.id1212.minor.hangman.client.view;

import se.kth.ict.id1212.minor.hangman.client.net.OutputHandler;

public class ServerObserver implements OutputHandler {
    @Override
    public void handleMessage(String msg) {
        System.out.println(msg);
    }
}
