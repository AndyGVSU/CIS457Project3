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
    private HandTile[] handTiles = new HandTile[ScrabblePlayer.HAND_SIZE];
    private PlayerPanel[] playerLabels = null;
    private ScrabbleClient game = null;
    
    private ScrabbleTCPServer server;
    
    private Vector<String> serverNameList;
    private Vector<InetAddress> serverIPList;
    private Vector<String> playerNameList;
    
    private ServerListUpdater serverListUpdate;
    private LobbyListUpdater playerListUpdate;
    private GameLoop commandListener;
    
    private JScrollPane listPane;
    private JLabel totalLettersLabel;
    private JTable letterCounts;
    
    private String serverName;
    private String username = "!DEBUG NAME!"; //defualt name
    private boolean gameRunning = true;
    
    private boolean enabled = true;
    private boolean host;

    public ScrabbleGUI(ScrabbleClient gm) {
        game = gm;
        serverName = null;
        host = false;

        updateServerList();
        initComponents();
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

        commandListener = new GameLoop();
        commandListener.start();
    } 

    public void goToGame() {
        playerListUpdate.close();
        if (host)
            server.closeLobby();
        remove(getContentPane());
        GamePanel gp = new GamePanel();
        setContentPane(gp);
        finishComponents();
        updateGUI(false);
    }

    public void updateServerList() {
        serverNameList = game.getServerNames();
        serverIPList = game.getServerAddresses();
    }

    public void updatePlayerList() {
        playerNameList = game.getPlayerNames();
    }

    public void updateGUI(boolean turnOn) {
       
        //enables/disables all components, but only if flagged beforehand (to avoid repeats)
        if (enabled != turnOn) {
            System.out.println("GUI Turned On: "+turnOn);
            enabled = turnOn;

            for (Component p : getContentPane().getComponents()) {
                for (Component subPanelc : ((JPanel) p).getComponents())
                    if (subPanelc.getClass() == JPanel.class) {
                        for (Component d : ((JPanel) subPanelc).getComponents()) 
                            d.setEnabled(turnOn);
                    }   
                    else
                        subPanelc.setEnabled(turnOn);
            }
        }
        
        ScrabblePlayer play = game.getPlayers().get(game.getPlayerIndex());
        if (play != null) {
	        //update hand
	        for (int i = 0; i < handTiles.length; i++) {
	        	try {
	            handTiles[i].setText(String.valueOf(play.getTile(i).getLetter()));
	        	}
	        	//no more tiles in hand
	        	catch (ArrayIndexOutOfBoundsException e) {
	        		handTiles[i].setText("");
	        	}
	        }
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

        Vector<ScrabblePlayer> playerList = game.getPlayers();
        //update player values
        for (int i = 0; i < game.getPlayerCount(); i++) {
            PlayerPanel p = playerLabels[i];
            play = playerList.get(i);
            
            p.setNameLabel(play.getName());
            p.setScoreLabel("Score: "+String.valueOf(play.getScore()));
            p.setTileLabel("Tiles: "+String.valueOf(play.getHandSize()));
        }
        //update tile count
        int[] tileCounts = game.getCurrentTileCounts();
        int totalTiles = 0;
        for (int i = 0; i < tileCounts.length; i++) {
        	totalTiles += tileCounts[i];
        	letterCounts.setValueAt(String.valueOf(tileCounts[i]),i+1,1);
        }
        totalLettersLabel.setText("Total Tiles: "+String.valueOf(totalTiles));
        
    }

    public ScrabbleClient getGame() {
        return game;
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
            JLabel authorLabel = new JLabel("By Andy Hudson, Zack Poorman, and Gray Schafer");
            JButton serverButton = new JButton("Host Game");
            
            JLabel nameLabel = new JLabel("User name:");
            JTextField nameField = new JTextField("",10);

            serverButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {

                    serverName = (String) JOptionPane.showInputDialog(
                    "Enter server name: ");
                    if (nameField.getText() != ""){
                        username = nameField.getText();
                    }
                    
                    if (!(serverName == null || serverName.isEmpty())) {
                        host = true;

                        server = new ScrabbleTCPServer();
                        server.start();

                        InetAddress myAddress = null;
                        try{ myAddress = InetAddress.getByName("127.0.0.1");}catch(Exception e){}

                        game.setUpTCP(myAddress, username);

                        goToLobby();
                        }
                    }});

            JLabel serverLabel = new JLabel("Available games:           ");
            JList<String> serverList = new JList<String>(serverNameList);
            serverList.setVisibleRowCount(8);
            listPane = new JScrollPane(serverList);
            listPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            JButton clientButton = new JButton("Join Game");
            clientButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    
                    if (nameField.getText() != ""){
                        username = nameField.getText();
                    }
                    
                    int selectedServerIndex = serverList.getSelectedIndex();

                    if (selectedServerIndex > -1) {
                        InetAddress serverIP = serverIPList.get(selectedServerIndex);

                        //set up TCP connection to valid IP!!
                        game.setUpTCP(serverIP, username);

                        host = false;
                        goToLobby();
                    }
                    }});

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
            leftPanel.add(nameLabel);
            //leftPanel.add(Box.createRigidArea(new Dimension(1,20)));
            leftPanel.add(nameField);
            leftPanel.add(Box.createRigidArea(new Dimension(1,20)));

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
            game.closeUDP();
            if (host) {
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
            
            JList<String> playerNames = new JList<String>(game.getPlayerNames());
            playerNames.setEnabled(false);
            add(playerNames);

            add(Box.createRigidArea(new Dimension(1,20)));

            playerListUpdate = new LobbyListUpdater(playerNames);
            playerListUpdate.start();
            //no button if running client!
            if (host)
                {
                JButton startButton = new JButton("START GAME");
                startButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        game.startGame();
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
            letterCounts = new JTable(28,2);
            letterCounts.setValueAt("Letter", 0, 0);
            letterCounts.setValueAt("Tiles Left", 0, 1);
            for (int i = 1; i < 28; i++) {
            	letterCounts.setValueAt((char) ('A' + i - 1), i, 0);
            }
            letterCounts.setValueAt("blank", 27, 0);

            totalLettersLabel = new JLabel("Total Tiles: 100");
            JButton passButton = new JButton("PASS");
            passButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    //tell game to restock game hand
                        //go to next turn (done *client-side*)
                        game.sendCommand(ScrabbleClient.ScrabbleCommand.PASS_TURN);
                        //System.out.println("pass on gui");
                    }});

            JButton endButton = new JButton("END");
            passButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    //tell game to validate word placed on board
                    //upon a successful validation:
                        //tell game to restock game hand
                        //go to next turn
                        game.sendCommand(ScrabbleClient.ScrabbleCommand.END_TURN); //freezing up?
                        //System.out.println("end on gui");
                    }});
            
            leftPanel.add(titleLabel);
            leftPanel.add(letterCounts);
            leftPanel.add(totalLettersLabel);
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

            int playerCount = game.getPlayerCount();
            playerLabels = new PlayerPanel[playerCount];
            for(int i = 0; i < playerCount; i++) {
                playerLabels[i] = new PlayerPanel();
                }
            handPanel.add(playerLabels[0]);
            
            HandTile h;
            for(int i = 0; i < ScrabblePlayer.HAND_SIZE; i++) {
                h = new HandTile("A");
                handTiles[i] = h;
                //handButton.setHorizontalAlignment(SwingConstants.CENTER);
                handPanel.add(h);
            }
            
            rightPanel.add(gridPanel, BorderLayout.CENTER);
            rightPanel.add(handPanel, BorderLayout.SOUTH);
            if (playerLabels.length > 1)
                rightPanel.add(playerLabels[1], BorderLayout.WEST);
            if (playerLabels.length > 2)
                rightPanel.add(playerLabels[2], BorderLayout.NORTH);
            if (playerLabels.length > 3)
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

    private class LobbyListUpdater extends Thread {
        private boolean open = true;
        private JList<String> updateList;

        public LobbyListUpdater(JList<String> playerList) {
            updateList = playerList;
        }
        
        public void run() {
            try {
                while (open) {
                    TimeUnit.SECONDS.sleep(2);
                    updatePlayerList();
                    updateList.setListData(playerNameList);
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

    private class GameLoop extends Thread {

        public void run() {
            int nextCmd = -1;

            while(gameRunning) {
                nextCmd = game.getLastCommand(); //resets to -1 after access
                System.out.print("");
                if (nextCmd != -1) {
                    System.out.println("GUI heard command: "+nextCmd);
                    if (nextCmd == ScrabbleClient.ScrabbleCommand.START_GAME.ordinal())
                        goToGame();
                    else if (nextCmd != ScrabbleClient.ScrabbleCommand.PLAYER_INFO.ordinal())
                        updateGUI(game.getMyTurn());
                }
            }
            System.out.println("GUI Command listener thread terminated");
        }
    }
}
