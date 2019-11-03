import java.util.*;
import java.net.*;

public class ScrabblePlayer {

    final static int HAND_SIZE = 7;
    private String name;
    private int score;
    private Vector<ScrabbleTile> hand;
    private Socket clientSocket;

    public ScrabblePlayer(String name, Socket sock) {
        this.name = name;
        this.clientSocket = sock;
        this.score = 0;
        this.hand = new Vector<ScrabbleTile>();
    }

    public void addScore(int s) {
        score += s;
    } 

    public void addTile(ScrabbleTile t) {
        hand.add(t);
    }

    public ScrabbleTile removeTile(int index) {
        return hand.remove(index);
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public Socket getSocket() {
        return clientSocket;
    }
}
