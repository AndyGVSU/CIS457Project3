import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.net.*;

public class ScrabbleGUI extends JFrame {

    final static Font tileBoardFont = new Font("Times New Roman",0,12);
    final static Font tileHandFont = new Font("Times New Roman",0,32);
    final private Color[] boardBonusColor = {Color.WHITE, Color.CYAN, Color.BLUE, Color.PINK, Color.RED, Color.YELLOW};
    private BoardTile[][] boardTiles;
    private HandTile[] handTiles = null;
    private PlayerPanel[] playerLabels = null;
    private ScrabbleClient game = null;
    private boolean host;
    private Vector<String> serverNameList;
    private Vector<InetAddress> serverIPList;
    private ServerListUpdater serverListUpdate;
    private String serverName;
    private JScrollPane listPane;

    public ScrabbleGUI(ScrabbleClient gm) {
        game = gm;
        serverName = null;
        host = false;

        updateServerList();
        initComponents();    
    }

    private void initComponents() {
        setContentPane(new TitlePanel());
        setResizable(false);
        finishComponents();
    }

    private void finishComponents() {
        pack();
        setVisible(true);
    }

    public void goToLobby() {
        serverListUpdate.close();
        remove(getContentPane());
        setContentPane(new LobbyPanel());
        finishComponents();
    } 

    public void goToGame() {
        remove(getContentPane());
        setContentPane(new GamePanel());
        finishComponents();
        updateGUI();
    }

    public void updateServerList() {
        serverNameList = game.getServerNames();
        serverIPList = game.getServerAddresses();
    }

    public void updateGUI() {
       
       ScrabbleClient client = getGame();
        //update hand
        for (int i = 0; i < handTiles.length; i++) {
            //setText();
        }

        //update board
        ScrabbleBoard board = game.getBoard();
        ScrabbleTile gameTile;
        BoardTile guiTile;
        for (int r = 0; r < boardTiles.length; r++) {
            for (int c = 0; c < boardTiles[r].length; c++) {
                //setText();
                guiTile = boardTiles[r][c];
                gameTile = board.getTile(r,c);
                if (gameTile != null){
                    guiTile.setEnabled(false);
                }
                else {
                    guiTile.setBackground(boardBonusColor[board.getBonus(r, c).ordinal()]);
                }  
            }
        }

        //update player values
        /*
        for (int i = 0; i < game.getPlayerCount(); i++) {
            PlayerPanel p = playerLabels[i];
            p.setNameLabel();
            p.setScoreLabel();
            p.setTileLabel();
        }
*/
        //update tile count

        
    }

    public ScrabbleClient getGame() {
        return game;
    } 

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ScrabbleClient game = new ScrabbleClient(true);
                ScrabbleGUI gui = new ScrabbleGUI(game);
                gui.setVisible(true);
            }
        });
    }

    private class TitlePanel extends JPanel {
        public TitlePanel() {
            initComponents();
        }      

        private void initComponents() {
            BoxLayout mainLayout = new BoxLayout(this,BoxLayout.X_AXIS);
            setLayout(mainLayout);
            JPanel rightPanel = new JPanel();
            JPanel leftPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel,BoxLayout.Y_AXIS));
            leftPanel.setLayout(new BoxLayout(leftPanel,BoxLayout.Y_AXIS));

            JLabel titleLabel = new JLabel("     SCRABBLE: CIS 457     ");
            titleLabel.setFont(new Font("Times New Roman",0,48));
            JLabel authorLabel = new JLabel("By Andy Hudson, Zack Poorman, and Grey Schafer");
            JButton serverButton = new JButton("Host Game");

            serverButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {

                    serverName = (String) JOptionPane.showInputDialog(
                    "Enter server name: ");
                    
                    if (!(serverName == null || serverName.isEmpty())) {
                        host = true;
                        goToLobby();
                        }
                    }});

            JButton clientButton = new JButton("Join Game");
            clientButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    InetAddress serverIP = serverIPList.get(listPane.getSelectedIndex());

                    //set up TCP connection to valid IP!!


                    host = false;
                    goToLobby();
                    }});

            JLabel serverLabel = new JLabel("Available games:           ");
            JList<String> serverList = new JList<String>(serverNameList);
            serverList.setVisibleRowCount(8);
            listPane = new JScrollPane(serverList);
            listPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            serverListUpdate = new ServerListUpdater(serverList);
            serverListUpdate.start();

            //contain
            leftPanel.add(Box.createRigidArea(new Dimension(1,40)));
            leftPanel.add(titleLabel);
            leftPanel.add(Box.createRigidArea(new Dimension(1,20)));
            leftPanel.add(authorLabel);
            leftPanel.add(Box.createRigidArea(new Dimension(1,40)));
            leftPanel.add(serverButton);
            leftPanel.add(Box.createRigidArea(new Dimension(1,20)));
            leftPanel.add(clientButton);
            leftPanel.add(Box.createRigidArea(new Dimension(1,60)));

            for (Component c : leftPanel.getComponents()){
                ((JComponent) c).setAlignmentX(JComponent.CENTER_ALIGNMENT);
            }

            rightPanel.add(Box.createRigidArea(new Dimension(1,30)));
            rightPanel.add(serverLabel);
            rightPanel.add(Box.createRigidArea(new Dimension(1,10)));
            rightPanel.add(listPane);
            //serverLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

            this.add(leftPanel);
            this.add(rightPanel);
        }
    }

    private class LobbyPanel extends JPanel {

        public LobbyPanel() {
            initComponents();
            if (host) {
                game.closeUDP();
                ScrabbleUDPServer nameSend = new ScrabbleUDPServer(serverName);
                nameSend.start();
            }
        }

        private void initComponents() {
            setPreferredSize(new Dimension(600,300));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
            JLabel lobbyLabel = new JLabel("Lobby: Waiting for Players...");
            lobbyLabel.setFont(new Font("Times New Roman",0,36));
            add(Box.createRigidArea(new Dimension(1,40)));
            add(lobbyLabel);
            
            JLabel[] playerNames = new JLabel[4];
            for(int i=0; i < 4; i++) {
                playerNames[i] = new JLabel("waiting for player "+i+"...");
                add(Box.createRigidArea(new Dimension(1,10)));
                add(playerNames[i]);
            }

            add(Box.createRigidArea(new Dimension(1,20)));

            //no button if running client!
            if (host)
                {
                JButton startButton = new JButton("START GAME");
                startButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        goToGame();
                        }});

                add(startButton);
            }
            
        }
    }

    private class GamePanel extends JPanel {

        public GamePanel() {
            initComponents();
        }
        
        private void initComponents() {
            int boardSize = ScrabbleBoard.BOARD_SIZE;

            BoxLayout mainLayout = new BoxLayout(this,BoxLayout.X_AXIS);
            setLayout(mainLayout);
            JPanel rightPanel = new JPanel();
            JPanel leftPanel = new JPanel();
            rightPanel.setLayout(new BorderLayout(50,50));
            leftPanel.setLayout(new BoxLayout(leftPanel,BoxLayout.Y_AXIS));

            JLabel titleLabel = new JLabel("Scrabble CIS457");
            JTable letterCounts = new JTable(27,2);
            for (int i = 0; i < 27; i++) {

            }

            JLabel letterTotal = new JLabel("100");
            JButton passButton = new JButton("PASS");
            passButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    //tell game to restock game hand
                        //go to next turn
                        //update GUI
                    }});

            JButton endButton = new JButton("END");
            passButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    //tell game to validate word placed on board
                    //upon a successful validation:
                        //tell game to restock game hand
                        //go to next turn
                        //update GUI
                    }});
            
            leftPanel.add(titleLabel);
            leftPanel.add(letterCounts);
            leftPanel.add(letterTotal);
            leftPanel.add(passButton);
            leftPanel.add(endButton);

            boardTiles = new BoardTile[ScrabbleBoard.BOARD_SIZE][ScrabbleBoard.BOARD_SIZE];
            JPanel gridPanel = new JPanel();
            gridPanel.setLayout(new GridLayout(boardSize,boardSize));
            for(int i = 0; i < boardSize; i++) {
                for(int j = 0; j < boardSize; j++) {
                    boardTiles[i][j] = new BoardTile("");
                    gridPanel.add(boardTiles[i][j]);
                }
            }
            
            JPanel handPanel = new JPanel();
            handPanel.setLayout(new BoxLayout(handPanel, BoxLayout.X_AXIS));

            playerLabels = new PlayerPanel[game.getPlayerCount()];
            for(int i = 0; i < 4; i++) {
                playerLabels[i] = new PlayerPanel();
                }
            handPanel.add(playerLabels[0]);
            
            handTiles = new HandTile[ScrabblePlayer.HAND_SIZE];
            HandTile h;
            for(int i = 0; i < ScrabblePlayer.HAND_SIZE; i++) {
                h = new HandTile("A");
                handTiles[i] = h;
                //handButton.setHorizontalAlignment(SwingConstants.CENTER);
                handPanel.add(h);
            }
            
            rightPanel.add(gridPanel, BorderLayout.CENTER);
            rightPanel.add(handPanel, BorderLayout.SOUTH);
            rightPanel.add(playerLabels[1], BorderLayout.WEST);
            rightPanel.add(playerLabels[2], BorderLayout.NORTH);
            rightPanel.add(playerLabels[3], BorderLayout.EAST);

            add(leftPanel);
            add(rightPanel);
        }
    }

    private class PlayerPanel extends JPanel {
        private JLabel nameLabel;
        private JLabel scoreLabel;
        private JLabel tileLabel;

        public PlayerPanel() {
            initComponents();
        }
        private void initComponents() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            
            nameLabel = new JLabel("player name");
            scoreLabel = new JLabel("score: 0");
            tileLabel = new JLabel("tiles: 7");

            add(nameLabel);
            add(scoreLabel);
            add(tileLabel);
            
            for (Component c : getComponents()){
                ((JComponent) c).setAlignmentX(JComponent.CENTER_ALIGNMENT);
                ((JComponent) c).setAlignmentY(JComponent.CENTER_ALIGNMENT);
            }
        }

        public void setNameLabel(String n) {
            nameLabel.setText(n);
        }
        public void setScoreLabel(String s) {
            scoreLabel.setText(s);
        }
        public void setTileLabel(String t) {
            tileLabel.setText(t);
        }
    }

    private class HandTile extends JTextField implements Transferable, MouseListener {
        String letter;
        
        public HandTile(String letter) {
            setDragEnabled(true);
            setPreferredSize(new Dimension(20,20));
            addMouseListener(this);
            setFont(tileHandFont);
            setEditable(false);
            this.letter = letter;
            setText(letter);
        }

        public void mouseEntered(MouseEvent e) {
            selectAll();
            requestFocus();
        }

        public void mouseExited(MouseEvent e) {}
        public void mousePressed(MouseEvent e) {}
        public void mouseClicked(MouseEvent e) {
            selectAll();
            requestFocus();
        }
        public void mouseReleased(MouseEvent e) {}

        public Object getTransferData(DataFlavor d) throws UnsupportedFlavorException, IOException {
            return null;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return null;
        }

        public boolean isDataFlavorSupported(DataFlavor d) {
            return true;
        }

    }
    private class BoardTile extends JTextField implements Transferable, MouseListener {
        String letter;
        
        public BoardTile(String letter) {
            setDragEnabled(false);
            
            setPreferredSize(new Dimension(20,20));
            addMouseListener(this);
            setFont(tileBoardFont);
            this.letter = letter;
            setText(letter);
            
            setTransferHandler(new TransferHandler() {
                public boolean canImport(TransferHandler.TransferSupport data) {return true;}
                public boolean importData(TransferHandler.TransferSupport data) {
                    JTextField thisField = (JTextField) data.getComponent();
                    Transferable transfer = data.getTransferable();
                    String newLetter = null;
                    try {
                        newLetter = (String) transfer.getTransferData(DataFlavor.stringFlavor);
                    }
                    catch (Exception e) {}
                    //System.out.println(newLetter);

                    //for appearance only
                    thisField.setText(newLetter);

                    //do game add-to-board event (will overwrite the above display change)
                    
                    return true;
                }

                });
                
        }

        public void mouseEntered(MouseEvent e) {
            selectAll();
            requestFocus();
        }

        public void mouseExited(MouseEvent e) {}
        public void mousePressed(MouseEvent e) {}
        public void mouseClicked(MouseEvent e) {
            //call game to remove tile
        }
        public void mouseReleased(MouseEvent e) {}

        public Object getTransferData(DataFlavor d) throws UnsupportedFlavorException, IOException {
            System.out.println("Received data!");
            return null;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return null;
        }

        public boolean isDataFlavorSupported(DataFlavor d) {
            return true;
        }

    }
    private class ServerListUpdater extends Thread {
        private boolean open = true;
        private JList<String> updateList;

        public ServerListUpdater(JList<String> serverList) {
            updateList = serverList;
        }
        
        public void run() {
            try {
                while (open) {
                    TimeUnit.SECONDS.sleep(2);
                    updateServerList();
                    updateList.setListData(serverNameList);
                }
            }
            catch (Exception e) {
                System.out.println(e);
            }
        }
        public void close() {
            open = false;
        }
    }
}
