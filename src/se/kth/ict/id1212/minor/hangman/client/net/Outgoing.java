package se.kth.ict.id1212.minor.hangman.client.net;

import se.kth.ict.id1212.minor.hangman.common.Communication;


import java.util.concurrent.CompletableFuture;

public class Outgoing {
    private Communication communication;


    public Outgoing(Communication communication) {
        this.communication = communication;
    }

    public void sendMessage(String toServer) {
        CompletableFuture.runAsync(() -> communication.sendMessage(toServer));
    }
}
