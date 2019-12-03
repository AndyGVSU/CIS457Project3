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
        
    }
    //network event
    public void removeBoardTile(int row, int col) {
        
    }

    //if valid, returns total score, otherwise returns 0
    public int validatePlacedTiles() {
        ScrabbleTile nextTile;
        boolean breakSeach = false;
        for (int r = 0; (r < ScrabbleBoard.BOARD_SIZE && !breakSearch); r++) {
            for (int c = 0; (c < ScrabbleBoard.BOARD_SIZE && !breakSearch); c++) {
                nextTile = getTile(r,c);
                if (nextTile != null) {
                    if (!nextTile.getValidated()) {
                        breakSearch = true;
                    }
                }
            }
        }
        ScoredWord horizontalWord = validateWord(r,c,false,true);
        ScoredWord verticalWord = validateWord(r,c,true,true);

        Vector<ScoredWord> extraWords = new Vector<ScoredWord>();

        int pos;
        if (horizontalWord.getScore() != 0) {
            pos = horizontalWord.getStartPosition();
            for (int i = 0; i < horizontalWord.getWord().length; i++) {
                if (i != pos)
                    extraWords.add(validateWord(r, c + i - pos, true, false));
            }
        }
        if (verticalWord.getScore() != 0) {
            pos = verticalWord.getStartPosition();
            for (int i = 0; i < verticalWord.getWord().length; i++) {
                if (i != pos)
                    extraWords.add(validateWord(r + i - pos, c, true, false));
            }
        }

    extraWords.add(horizontalWord);
    extraWords.add(verticalWord);
    int score;
    for (ScoredWord w : extraWords) {
        score += w.getScore();
    }

    return score;
    }

    //from a (row,col) position, extend out in a direction (horizontal/vertical) until empty spaces are found
    //return score for word (0 indicates that the given search returned no word)
    public int validateWord(int row, int col, boolean direction, boolean scoreBonuses) {
        int[] directionCheck = {1,1};
        String word = getTile(row,col).getLetter();
        String compareWord = word;
        char firstChar = word;
        int score, nextScore;
        BonusValue nextBonus;
        int wordMultiplier = 1;

        ScrabbleTile tile1, tile2;
        char tileLetter1, tileLetter2;
        boolean doneSearching = false;

        int row1, col1, row2, col2;

        while (!doneSearching) {
            row1 = row - directionCheck[1];
            col1 = col - directionCheck[0];
            row2 = row + directionCheck[1];
            col2 = col + directionCheck[0];

            tile1 = getTile(row1, col1);
            tile2 = getTile(row2, col2);
            if (tile1 != null) {
                word = tile1.getLetter() + word;
                nextScore = tile1.getScoreValue();
                nextBonus = getBonus(row1, col1);
                if (nextBonus == BonusValue.DL || nextBonus == BonusValue.ST)
                    nextScore *= 2;
                else if (nextBonus == BonusValue.TL)
                    nextScore *= 3;
                else if (nextBonus == BonusValue.DW)
                    wordMultiplier = 2;
                else if (nextBonus == BonusValue.TW)
                    wordMultiplier = 3;

                score += nextScore;
            }
            if (tile2 != null) {
                word = word + tile2.getLetter();
                nextScore = tile2.getScoreValue();
                nextBonus = getBonus(row2, col2);
                if (nextBonus == BonusValue.DL || nextBonus == BonusValue.ST)
                    nextScore *= 2;
                else if (nextBonus == BonusValue.TL)
                    nextScore *= 3;
                else if (nextBonus == BonusValue.DW)
                    wordMultiplier = 2;
                else if (nextBonus == BonusValue.TW)
                    wordMultiplier = 3;

                score += nextScore;
            }
            if (word.equals(compareWord))
                break;
            compareWord = word;

            if (direction)
                directionCheck[0]++;
            else
                directionCheck[1]++;
        }            

        if (word.size() > 1) {
            //check word with dictionary!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            if (true && validatedWords.indexOf(word) == -1) {            
                score *= wordMultiplier;
                validatedWords.add(word);
                return new ScoredWord(score, word, firstChar);
                }
            }
        return new ScoredWord();
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

        public int getWordScore() {
            return score;
        }
        public int getWord() {
            return word;
        }
        public int getStartPosition() {
            return startPos;
        }
    }
}