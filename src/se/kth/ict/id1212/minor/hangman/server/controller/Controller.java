package se.kth.ict.id1212.minor.hangman.server.controller;

import se.kth.ict.id1212.minor.hangman.server.model.Hangman;
import se.kth.ict.id1212.minor.hangman.server.db.Word_DB;

public class Controller {
    private final Hangman HANGMAN;
    private final Word_DB WORDS;

    public Controller(Word_DB words) {
        WORDS = words;
        HANGMAN = new Hangman();
    }

    public void newGame() {
        String word = WORDS.getRandomWord();
        HANGMAN.newGame(word);
    }

    public void handleGuess(String guess) {
        HANGMAN.handleGuess(guess);
    }

    public String getMessage() {
        return HANGMAN.getMessage();
    }
}
