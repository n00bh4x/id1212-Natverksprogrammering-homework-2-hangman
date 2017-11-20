package se.kth.ict.id1212.minor.hangman.client.net;

import se.kth.ict.id1212.minor.hangman.common.Communication;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ServerHandler {
    Incoming incoming;
    Outgoing outgoing;
    Socket socket;
    Thread thread;
    
    public ServerHandler(String address, int portNumber, OutputHandler outputHandler) {
        socket = createSocket(address, portNumber);
        Communication communication = new Communication(socket);
        incoming = new Incoming(outputHandler, communication);
        outgoing = new Outgoing(communication);
        thread =  new Thread(incoming);
        thread.start();
    }


    private Socket createSocket(String address, int portNumber) {
        Socket socket = null;
        try {
            socket = new Socket(address, portNumber);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return socket;
    }

    public void sendMessage(String fromUser) {
        outgoing.sendMessage(fromUser);
    }

    public void disconnect() {
        try {
            thread.join();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
