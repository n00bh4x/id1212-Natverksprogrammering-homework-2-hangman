package se.kth.ict.id1212.minor.hangman.client.startup;

import se.kth.ict.id1212.minor.hangman.client.controller.Controller;
import se.kth.ict.id1212.minor.hangman.client.view.ServerObserver;
import se.kth.ict.id1212.minor.hangman.client.view.View;

public class ClientMain {

    public static void main(String[] args) {
        try {
            ServerObserver serverObserver = new ServerObserver();
            Controller controller = new Controller(args[0], Integer.parseInt(args[1]), serverObserver);
            View view = new View(controller);
            view.start();

        } catch (NumberFormatException e){
            System.out.println("Portnummer måste vara ett nummer");
            System.exit(0);
        }
    }
}
