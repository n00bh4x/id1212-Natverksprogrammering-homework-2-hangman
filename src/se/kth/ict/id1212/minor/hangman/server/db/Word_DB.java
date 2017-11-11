package se.kth.ict.id1212.minor.hangman.server.db;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Word_DB {
    private final ArrayList<String> WORDS;
    private final int NUMBER_OF_WORDS;
    public Word_DB (String filename) {
        WORDS = readFile(filename);
        NUMBER_OF_WORDS = this.WORDS.size();
    }

    private ArrayList<String> readFile(String filename) {
        ArrayList<String> wordList = new ArrayList<>();
        try(BufferedReader fromFile =
                new BufferedReader(new FileReader(filename))) {
            fromFile.lines().forEachOrdered(line -> wordList.add(line));
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
        return wordList;
    }

    public synchronized String getRandomWord() {
        int randomNum = ThreadLocalRandom.current().nextInt(0, NUMBER_OF_WORDS + 1);
        return WORDS.get(randomNum);
    }
}
