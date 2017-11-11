package se.kth.ict.id1212.minor.hangman.client.net;

import se.kth.ict.id1212.minor.hangman.common.Communication;

public class Incoming implements Runnable {
    OutputHandler outputHandler;
    Communication communication;
    boolean run;

    public Incoming(OutputHandler outputHandler, Communication communication) {
        this.outputHandler = outputHandler;
        this.communication = communication;
        run = true;
    }

    @Override
    public void run() {
        String fromServer;
        while (run) {
            fromServer = communication.receiveMessage();
            if(fromServer.equalsIgnoreCase("Thanks for playing Hangman!")) {
                run = false;
            }
            outputHandler.handleMessage(fromServer);
        }
    }
}
