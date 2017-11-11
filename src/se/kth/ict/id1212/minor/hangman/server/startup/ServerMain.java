package se.kth.ict.id1212.minor.hangman.server.startup;

import se.kth.ict.id1212.minor.hangman.server.net.Server;
import se.kth.ict.id1212.minor.hangman.server.db.Word_DB;

public class ServerMain {

    public static void main(String[] args){
        Word_DB words = new Word_DB("words.txt");
        try {
            new Server(Integer.parseInt(args[0]), words);

        } catch (NumberFormatException e){
            System.out.println("Portnummer måste vara en integer");
            System.exit(0);
        }
    }
}
