package se.kth.ict.id1212.minor.hangman.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.PrintWriter;

public class Communication {
    private BufferedReader fromClient;
    private PrintWriter toClient;

    public Communication(Socket clientSocket) {
        this.fromClient = setupReader(clientSocket);
        this.toClient = setupWriter(clientSocket);
    }

    private BufferedReader setupReader(Socket clientSocket) {
        BufferedReader fromClient = null;
        try {
            fromClient =
                    new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fromClient;
    }

    private PrintWriter setupWriter(Socket clientSocket) {
        PrintWriter toClient = null;
        try {
            toClient = new PrintWriter(clientSocket.getOutputStream());
        } catch (IOException e) {
        }
        return toClient;
    }

    public String receiveMessage() {
        String msg = null;
        try {
            msg = this.fromClient.readLine().toLowerCase();
        } catch (Exception e) {
        }
        return msg;
    }

    public void sendMessage(String message) {
        toClient.println(message);
        toClient.flush();
    }
}
