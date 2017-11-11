package se.kth.ict.id1212.minor.hangman.server.model;


public class Hangman {
    private String word;
    private int score;
    private int attempts;
    private String guess;
    private boolean won;
    private boolean lost;
    private boolean on;

    public Hangman() {
        score = 0;
        on = false;
    }


    public String getMessage() {
        String message;
        if(on) {
            if (won) {
                on = false;
                message = "Congratulations! You guessed the correct word: " + word + "." +
                        "\nYour current score is: " + score +
                        ".\n'play' to play again or 'quit' to quit game.";
            } else if (lost) {
                on = false;
                message = "You lost!" +
                        "\nYou have " + attempts + " remaining attempts." +
                        "\nYour current score is " + score + "." +
                        "'play' to play again or 'quit' to quit game.";
            } else {
                message = "Hidden word: " + guess +
                        "\nScore: " + score +
                        "\nRemaining attempts: " + attempts +
                        "\nMake a guess. A letter or a word.";
            }
        } else {
            message = "Game not started." +
                    "\n'play' to play again or 'quit' to quit game.";
        }
        return message;
    }

    public void newGame(String word) {
        this.word = word;
        System.out.println(word + "         Printed from hangman class. take away when done.");
        this.guess = getGuess();
        this.attempts = this.word.length();
        this.won = false;
        this.lost = false;
        on = true;
    }


    public void handleGuess(String guess) {
        if(on) {
            if (guess.equalsIgnoreCase(word)) {
                this.guess = word;
            } else if (guess.length() == 1) {
                checkIfLetterExist(guess);
            } else {
                this.attempts--;
            }

            if (!this.guess.contains("_")) {
                won = true;
                score++;
            }

            if (attempts == 0 && won == false) {
                lost = true;
                score--;
            }
        }
    }


    private void checkIfLetterExist(String guess) {
        boolean correctGuess = false;
        for (int i = 0; i < this.word.length(); i++) {
            if (this.word.charAt(i) == guess.charAt(0)) {
                updateGuess(guess, i);
                correctGuess = true;
            }
        }
        if(!correctGuess) {
            this.attempts--;
        }
    }

    private void updateGuess(String guess, int position) {
        StringBuilder builder = new StringBuilder(this.guess);
        int index;
        if(position == 0) {
            index = 0;
        } else {
            index = position * 2;
        }
        builder.replace(index, index+1, guess);
        this.guess = builder.toString();
    }



    private String getGuess() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.word.length(); i++) {
            builder.append('_');
            if(i != this.word.length() - 1) {
                builder.append(' ');
            }
        }
        return builder.toString();
    }



}