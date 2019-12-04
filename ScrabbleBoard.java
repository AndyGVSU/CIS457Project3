/****************************************************************
 * @author Anderson Hudson, Zack Poorman, Gray Schafer
 * 
 * ScrabbleBoard
 * 
 * Holds all board data (tile grid, bonus values, scored words).
 * Holds methods to score words that have been placed.
 * 
 ***************************************************************/

import java.util.*;

public class ScrabbleBoard {

    enum BonusValue {NA, DL, TL, DW, TW, ST; }

    final static int BOARD_SIZE = 15;
    private ScrabbleTile[][] boardTiles;
    private BonusValue[][] boardBonuses;
    private Vector<String> validatedWords = new Vector<String>();

    public ScrabbleBoard() {
        boardTiles = new ScrabbleTile[BOARD_SIZE][BOARD_SIZE]; //empty space is null

        initBonuses();
    }

    private void initBonuses() {

        boardBonuses = new BonusValue[][] {
            {BonusValue.TW,BonusValue.NA,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.TW,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.NA,BonusValue.TW},
            {BonusValue.NA,BonusValue.DW,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.TL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.TL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DW,BonusValue.NA},
            {BonusValue.NA,BonusValue.NA,BonusValue.DW,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DW,BonusValue.NA,BonusValue.NA},
            {BonusValue.DL,BonusValue.NA,BonusValue.NA,BonusValue.DW,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DW,BonusValue.NA,BonusValue.NA,BonusValue.DL},
            {BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DW,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DW,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.NA},
            {BonusValue.NA,BonusValue.TL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.TL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.TL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.TL,BonusValue.NA},
            {BonusValue.NA,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.NA},
            {BonusValue.TW,BonusValue.NA,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.ST,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.NA,BonusValue.TW},
            {BonusValue.NA,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.NA},
            {BonusValue.NA,BonusValue.TL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.TL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.TL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.TL,BonusValue.NA},
            {BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DW,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DW,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.NA},
            {BonusValue.DL,BonusValue.NA,BonusValue.NA,BonusValue.DW,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DW,BonusValue.NA,BonusValue.NA,BonusValue.DL},
            {BonusValue.NA,BonusValue.NA,BonusValue.DW,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DW,BonusValue.NA,BonusValue.NA},
            {BonusValue.NA,BonusValue.DW,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.TL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.TL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DW,BonusValue.NA},
            {BonusValue.TW,BonusValue.NA,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.TW,BonusValue.NA,BonusValue.NA,BonusValue.NA,BonusValue.DL,BonusValue.NA,BonusValue.NA,BonusValue.TW}
        };
    }

    public ScrabbleTile getTile(int row, int col) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE)
            return null;
        return boardTiles[row][col];
    }

    public BonusValue getBonus(int row, int col) {
        return boardBonuses[row][col];
    }

    public void removeBonus(int row, int col) {
        boardBonuses[row][col] = BonusValue.NA;
    }

    //network event
    public void addBoardTile(int row, int col, ScrabbleTile t) {
        boardTiles[row][col] = t;
    }
    //network event
    public ScrabbleTile removeBoardTile(int row, int col) {
        ScrabbleTile t = boardTiles[row][col];
        boardTiles[row][col] = null;
        return t;
    }

    //if valid, returns total score, otherwise returns 0
    public int validatePlacedTiles() {
        ScrabbleTile nextTile;
        boolean breakSearch = false;
        int r = 0, c = 0;
        for (r = 0; (r < ScrabbleBoard.BOARD_SIZE && !breakSearch); r++) {
            for (c = 0; (c < ScrabbleBoard.BOARD_SIZE && !breakSearch); c++) {
                nextTile = getTile(r,c);
                if (nextTile != null) {
                    if (!nextTile.getScored()) {
                        breakSearch = true;
                    }
                }
            }
        }
        r--; c--; //correct values from incrementation

        ScoredWord horizontalWord = validateWord(r,c,false,true);
        ScoredWord verticalWord = validateWord(r,c,true,true);
        Vector<ScoredWord> extraWords = new Vector<ScoredWord>();

        int pos;
        ScrabbleTile extraCheck;
        if (horizontalWord.getScore() != 0) {
            pos = horizontalWord.getStartPosition();
            for (int i = 0; i < horizontalWord.getWord().length(); i++) {
                extraCheck = getTile(r, c + i - pos);
                if (!extraCheck.getScored())
                    if (i != pos)
                        extraWords.add(validateWord(r, c + i - pos, true, false));
                extraCheck.setScored(true);
            }
        }
        if (verticalWord.getScore() != 0) {
            pos = verticalWord.getStartPosition();
            for (int i = 0; i < verticalWord.getWord().length(); i++) {
                extraCheck = getTile(r + i - pos, c);
                if (!extraCheck.getScored())
                    if (i != pos)
                        extraWords.add(validateWord(r + i - pos, c, true, false));
                extraCheck.setScored(true);
            }
        }

    extraWords.add(horizontalWord);
    extraWords.add(verticalWord);
    int score = 0;
    for (int i = 0; i < extraWords.size(); i++) {
        score += extraWords.get(i).getScore();
    }

    return score;
    }

    //from a (row,col) position, extend out in a direction (horizontal/vertical) until empty spaces are found
    //returns a ScoredWord instance (empty or has a word, depending on validation)
    public ScoredWord validateWord(int row, int col, boolean direction, boolean scoreBonuses) {
        int[] directionCheck = {1,1};
        ScrabbleTile startTile = getTile(row,col);
        String word = String.valueOf(startTile.getLetter());
        String compareWord = word;
        char[] firstChar = word.toCharArray();
        int score = 0;
        int[] nextScore;
        int wordMultiplier = 1;

        ScrabbleTile tile1, tile2;
        char tileLetter1, tileLetter2;
        boolean doneSearching = false;

        int row1 = row, col1 = col, row2 = row, col2 = col;

        //score first tile
        nextScore = scoreTile(row, col);
        if (nextScore[1] > wordMultiplier)
            wordMultiplier = nextScore[1];
        score += nextScore[0];

        if (direction)
            directionCheck[0] = 0;
        else
            directionCheck[1] = 0;

        //score tiles in specified direction
        boolean tile1space = false;
        boolean tile2space = false;
        while (!doneSearching) {
            if (!tile1space) {
                row1 -= directionCheck[1];
                col1 -= directionCheck[0];
            }
            if (!tile2space) {
                row2 += directionCheck[1];
                col2 += directionCheck[0];
            }

            tile1 = getTile(row1, col1);
            tile2 = getTile(row2, col2);
            if (tile1 != null && !tile1space) {
                word = tile1.getLetter() + word;
                
                nextScore = scoreTile(row1, col1);
                if (nextScore[1] > wordMultiplier)
                    wordMultiplier = nextScore[1];
                score += nextScore[0];
            }
            else {
                tile1space = true;
            }
            if (tile2 != null && !tile2space) {
                word = word + tile2.getLetter();

                nextScore = scoreTile(row2, col2);
                if (nextScore[1] > wordMultiplier)
                    wordMultiplier = nextScore[1];
                score += nextScore[0];
            }
            else {
                tile2space = true;
            }
            if (word.equals(compareWord))
                break;
            compareWord = word;
        }            

        int startPos;
        if (word.length() > 1) {
            if (direction) {
                startPos = (row - row1) - 1;
            }
            else {                
                startPos = (col - col1) - 1;
            }
            //check word with dictionary?
            if (true && validatedWords.indexOf(word) == -1) {          
                score *= wordMultiplier;
                validatedWords.add(word);
                return new ScoredWord(score, word, startPos);
                }
            }
        return new ScoredWord();
    }

    private int[] scoreTile(int row, int col) {
        ScrabbleTile tile = getTile(row, col);
        int nextScore = tile.getScoreValue();
        BonusValue nextBonus = getBonus(row, col);
        int wordMultiplier = 1;
        int[] returnArray = new int[2];

        if (!tile.getScored()) {
            if (nextBonus == BonusValue.DL)
                nextScore *= 2;
            else if (nextBonus == BonusValue.TL)
                nextScore *= 3;
            else if (nextBonus == BonusValue.DW || nextBonus == BonusValue.ST)
                wordMultiplier = 2;
            else if (nextBonus == BonusValue.TW)
                wordMultiplier = 3;
        }
            
        returnArray[0] = nextScore;
        returnArray[1] = wordMultiplier;
        return returnArray;
    }

    private class ScoredWord {
        private int score;
        private String word;
        private int startPos;

        public ScoredWord(int score, String word, int startPos) {
            this.score = score;
            this.word = word;
            this.startPos = startPos;
        }
        public ScoredWord() {
            this.score = 0;
            this.word = "";
        }

        public int getScore() {
            return score;
        }
        public String getWord() {
            return word;
        }
        public int getStartPosition() {
            return startPos;
        }
    }
}