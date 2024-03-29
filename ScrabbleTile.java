/****************************************************************
 * @author Anderson Hudson, Zack Poorman, Gray Schafer
 * 
 * ScrabbleTile
 * 
 * Holds game tile data (letter, base score, scored flag).
 * 
 ***************************************************************/

public class ScrabbleTile {
    final static int[] tileScore = {1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10, 0};
    private char letter;
    private int score;
    private boolean scored;

    //letter should always be capital or '['
    public ScrabbleTile(char letter) {
        this.letter = letter;
        this.score = tileScore[letter - 'A'];
    }

    public int getScoreValue() {
        return score;
    }
    public char getLetter() {
        return letter;
    }
    public boolean getScored() {
        return scored;
    }
    public void setScored(boolean s) {
        scored = s;
    }
}