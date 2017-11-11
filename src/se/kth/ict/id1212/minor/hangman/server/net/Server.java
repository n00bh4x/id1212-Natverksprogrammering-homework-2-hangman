package se.kth.ict.id1212.minor.hangman.server.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import se.kth.ict.id1212.minor.hangman.server.db.Word_DB;

public class Server {
    private ServerSocket serverSocket;
    private final Word_DB WORDS;
    public Server(int port, final Word_DB WORDS) {
        this.WORDS = WORDS;
        try {
            this.serverSocket = new ServerSocket(port);
            serve();
        } catch(IOException e) {
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
}
