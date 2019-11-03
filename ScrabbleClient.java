import java.util.*;

//game class
public class ScrabbleClient {
    private ScrabblePlayer[] players;
    private ScrabbleBoard board;
    private boolean hosting;
    final private int[] tileCounts = {9, 2, 2, 4, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1, 2};
    private char[] tileCharacters = new char[tileCounts.length];
    private Vector<ScrabbleTile> tileBag;
    private int turn;

    public ScrabbleClient(boolean host) {
        hosting = host;

        //blank is left bracket
        char startChar = 'A';
        for (int i = 0; i < tileCounts.length; i++) {
            tileCharacters[i] = startChar;
            startChar++;
        }
        
        if (host) {
            tileBag = new Vector<ScrabbleTile>();
            int count;
            char nextChar;
            for (int i = 0; i < tileCounts.length; i++) {
                count = tileCounts[i];
                nextChar = tileCharacters[i];
                for (int t = 0; t < count; t++) {
                    tileBag.add(new ScrabbleTile(nextChar));
                }
            }
        }

        board = new ScrabbleBoard();
    }

    public int getPlayerCount() {
        return 4;
        //return players.length;
    }

    public ScrabbleBoard getBoard() {
        return board;
    }
    
    public void endTurn() {};
    
    public void passTurn() {};

}