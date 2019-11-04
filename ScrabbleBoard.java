public class ScrabbleBoard {

    enum BonusValue {NA, DL, TL, DW, TW, ST; }

    final static int BOARD_SIZE = 15;
    private ScrabbleTile[][] boardTiles;
    private BonusValue[][] boardBonuses;

    public ScrabbleBoard() {
        boardTiles = new ScrabbleTile[BOARD_SIZE][BOARD_SIZE];

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

    public void validatePlacedTiles() {

    }

    public int scorePlacedTiles() {
        return 0;
    }
}