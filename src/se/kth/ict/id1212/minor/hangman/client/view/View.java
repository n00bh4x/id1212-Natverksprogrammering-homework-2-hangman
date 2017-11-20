package se.kth.ict.id1212.minor.hangman.client.view;

import se.kth.ict.id1212.minor.hangman.client.controller.Controller;
import se.kth.ict.id1212.minor.hangman.common.MsgType;

import java.util.Scanner;

public class View {
    private Scanner userInput;
    private Controller controller;
    private boolean play;


    public View(Controller controller) {
        play = true;
        userInput = new Scanner(System.in);
        this.controller = controller;
    }

    public void start() {
        String fromUser;
        String toServer;
        while(play){
            fromUser = userInput.nextLine();
            toServer = parseMsg(fromUser);
            if (fromUser.equalsIgnoreCase("QUIT")) {
                play = false;
            }
            controller.sendMessage(toServer);
        }
        //controller.disconnect();
    }

    private String parseMsg(String fromUser) {
        try {
            MsgType msgType = MsgType.valueOf(fromUser.toUpperCase());
            if(msgType == MsgType.PLAY) {
                return "PLAY";
            } else if (msgType == MsgType.QUIT) {
                return "QUIT";
            }
        } catch (Exception e) {
        }
        return "GUESS:" + fromUser;
    }
}
