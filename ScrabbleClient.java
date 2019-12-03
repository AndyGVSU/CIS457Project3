import java.io.*;
import java.net.*;
import java.util.*;

//game class
public class ScrabbleClient {

    private ScrabbleBoard board;
    private Vector<ScrabblePlayer> players = new Vector<ScrabblePlayer>();
    final private int[] tileCounts = {9, 2, 2, 4, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1, 2};
    private int[] currentTileCounts = tileCounts.clone();
    private char[] tileCharacters = new char[tileCounts.length];

    private Vector<ScrabbleTile> tileBag;
    private Vector<String> serverNameList = new Vector<String>();
    private Vector<InetAddress> serverIPList = new Vector<InetAddress>();
    private Vector<String> playerNameList = new Vector<String>();

    private boolean host;
    private MulticastSocket clientUDPSocket;
    private Socket clientTCPSocket;
    private ClientTCP client;

    private int turn;
    private int playerIndex = -1;
    private int playerCount = 0;
    private int lastCommand = -1;

    public enum ScrabbleCommand {ADD_HAND, REM_HAND, ADD_BOARD_TILE, REM_BOARD_TILE, 
        END_TURN, PASS_TURN, START_GAME, END_GAME, PLAYER_INFO}

    public ScrabbleClient(boolean host) {
        this.host = host;

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
        ClientUDP serverSearcher = new ClientUDP();
        serverSearcher.start();
    }

    public int getPlayerCount() {
        return playerCount;
    }
    
    /*
     * Returns this client's player index.
     * 
     */
    public int getPlayerIndex() {
    	return playerIndex;
    }

    public ScrabbleBoard getBoard() {
        return board;
    }

    public int[] getCurrentTileCounts() {
    	return currentTileCounts;
    }

    public void turnStart() {
        int handSize = players.get(turn).getHandSize();
        
        sendAddHand(ScrabblePlayer.HAND_SIZE - handSize);
    }
    
    public void sendStartGame() {
        sendCommand(ScrabbleCommand.START_GAME);
    }

    public void sendAddHand(int count) {
        sendCommand(ScrabbleCommand.ADD_HAND);
        try {
        client.getOutputStream().writeInt(count);
        }
        catch (Exception e) {System.out.println(e);}
    }

    public void sendRemoveHand(char c) {
        sendCommand(ScrabbleCommand.REM_HAND);
        try {
        client.getOutputStream().writeChar(c);
        }
        catch (Exception e) {System.out.println(e);}
    }

    public void sendAddBoard(int r, int c, char tile) {
        sendCommand(ScrabbleCommand.ADD_BOARD_TILE);
        DataOutputStream o = client.getOutputStream();
        try {
        o.writeInt(r);
        o.writeInt(c);
        o.writeChar(tile);
        }
        catch (Exception e) {System.out.println(e);}
    }

    public void sendRemoveBoard(int r, int c) {
        DataOutputStream o = client.getOutputStream();
        sendCommand(ScrabbleCommand.REM_BOARD_TILE);
        try {
        o.writeInt(r);
        o.writeInt(c);
        }
        catch (Exception e) {System.out.println(e);}
    }
 
    public void sendEndTurn() {
        sendCommand(ScrabbleCommand.END_TURN);
    };
    
    public void sendPassTurn() {
        sendCommand(ScrabbleCommand.PASS_TURN);
    };

    public void setIsHost(boolean h) {
        host = h;
    }

    public boolean getIsHost() {
        return host;
    }

    public boolean getMyTurn() {
        return (turn == playerIndex);
    }

    public Vector<ScrabblePlayer> getPlayers() {
    	return players;
    }
    
    public Vector<String> getPlayerNames() {
        return playerNameList;
    }

    public Vector<String> getServerNames() {
        return serverNameList;
    }

    public Vector<InetAddress> getServerAddresses() {
        return serverIPList;
    }

    public void closeUDP() {
        if (clientUDPSocket != null)
            clientUDPSocket.close();
    }

    public void setUpTCP(InetAddress host, String username) {
        client = new ClientTCP(host, username);
        client.start();
    }

    public void setLastCommand(int c) {
        lastCommand = c;
    }

    public int getLastCommand() {
        int last = lastCommand;
        lastCommand = -1;
        return last;
    }

    //only for command values; additional arguments must be provided when necessary
    public void sendCommand(ScrabbleCommand cmd) {
    	try {
        DataOutputStream out = client.getOutputStream();
        out.writeBoolean(false);
        out.writeInt(cmd.ordinal());
    	}
    	catch (Exception e) {
    		System.out.println(e);
    	}
    }

    private class ClientTCP extends Thread {
        private InetAddress host;
        private String username;
        private DataInputStream fromServer;
        private DataOutputStream toServer;
        private boolean isOpen = true;
        
        public ClientTCP(InetAddress host, String username) {
            this.host = host;
            this.username = username;
        }

        public void run() {
            System.out.println("TCP Client Running");
            clientTCPSocket = null;
            try {
                clientTCPSocket = new Socket(host, ScrabbleTCPServer.TCP_PORT);
                System.out.println("Connected to "+host);
                fromServer = new DataInputStream(clientTCPSocket.getInputStream());
                toServer = new DataOutputStream(clientTCPSocket.getOutputStream());

                //send name
                toServer.writeUTF(username);
                //get MY player index
                playerIndex = fromServer.readInt();
            }
            catch (Exception e) {
                System.out.println(e);
                System.out.println("Failed to set up TCP Client");
                isOpen = false;
            }

            boolean sentByServer;
            int command = -1;
            while (isOpen) {
                //read next command
                try {
                sentByServer = fromServer.readBoolean();
                if (!sentByServer)
                    System.out.println("!!!Client got data sent by client!!!");
                else
                    {
                    command = fromServer.readInt();
                    ScrabbleCommand convert = ScrabbleCommand.values()[command];
                    System.out.println("Client received command: "+command);

                    switch(convert) {

                        case ADD_HAND:
                            Vector<String> tiles = new Vector<String>();
                            int tilesToDraw = fromServer.readInt();
                            boolean noMoreTiles = false;

                            int tcount;
                            for (int c = 0; c < tileCharacters.length; c++) {
                                tcount = currentTileCounts[c];
                                for (int repeat = 0; repeat < tcount; repeat++)
                                    tiles.add(String.valueOf(tileCharacters[c]));
                            }
                            
                            char[] fromString;
                            Random grab = new Random();
                            for(int i = 0; i < tilesToDraw; i++){
                                if (tiles.size() > 0) {
                                    String nextChar = tiles.remove(grab.nextInt(tiles.size()));
                                    fromString = nextChar.toCharArray();
                                    players.get(turn).addTile(new ScrabbleTile(fromString[0]));
                                    currentTileCounts[fromString[0] - 'A']--;
                                }
                                else {
                                    noMoreTiles = true;
                                    break;
                                }
                            }
                            break;
                        case REM_HAND:
                            char toRemove = fromServer.readChar();
                            ScrabblePlayer currentPlayer = players.get(turn);
                            for(int i = 0; i < currentPlayer.getHandSize(); i++){
                                if (currentPlayer.getTile(i).getLetter() == toRemove) {
                                    currentPlayer.removeTile(i);
                                }
                            }

                            break;
                        case ADD_BOARD_TILE:
                            int row = fromServer.readInt();
                            int column = fromServer.readInt();
                            ScrabbleTile tileName = new ScrabbleTile(fromServer.readChar());
                            board.addBoardTile(row, column, tileName);
                            break;
                        case REM_BOARD_TILE:
                            int r = fromServer.readInt();
                            int c = fromServer.readInt();
                            ScrabblePlayer p = players.get(turn);
                            p.addTile(board.removeBoardTile(r, c));
                            break;
                        case END_TURN:
                            //score already-verified tiles
                            int score = board.validatePlacedTiles();
                            players.get(turn).addScore(score);
                            //increment turn
                            turn++;
                            if (turn == getPlayerCount())
                                turn = 0;//if everyone has had a turn this round
                            
                            if (getMyTurn()){
                                turnStart();
                            }
                            break;
                        case PASS_TURN:
                            //System.out.println("pass turn");
                            
                            turn++;
                            if (turn == getPlayerCount())
                                turn = 0;//if everyone has had a turn this round
                            
                            if (getMyTurn()) {
                                turnStart();
                            }
                            break;
                        case START_GAME:
                            //set up game on client-end
                            for (int i = 0; i < playerCount; i++) {
                                players.add(new ScrabblePlayer(playerNameList.get(i)));
                            }
                            //each player should have an automatic pass to refill hand

                            //if (getMyTurn())
                            //    sendPassTurn();
                            turnStart();

                            break;
                        case END_GAME:

                            break;
                        case PLAYER_INFO:
                            playerNameList.add(fromServer.readUTF());
                            playerCount++;
                            break;
                    }
                }
            } catch(Exception e) {
                System.out.println("ERROR READING!" + e);
            }

            setLastCommand(command);
            }
        }
        public DataOutputStream getOutputStream() {
            return toServer;
        }
    }

    private class ClientUDP extends Thread {
        public void run() {

            clientUDPSocket = null;
            try {
                clientUDPSocket = new MulticastSocket(ScrabbleUDPServer.UDP_PORT);
                InetAddress group = InetAddress.getByName(ScrabbleUDPServer.UDP_MULTICAST);
                clientUDPSocket.joinGroup(group);
            }
            catch (Exception e) {
                System.out.println(e);
            }
            byte[] receiveData = new byte[1024];
            String newName;

            System.out.println("UDP Client Running");

            while(clientUDPSocket != null && !clientUDPSocket.isClosed()) {
                DatagramPacket receiveName = new DatagramPacket(receiveData, receiveData.length);
                try {
                    clientUDPSocket.receive(receiveName);
                    InetAddress sender = receiveName.getAddress();
                    newName = new String(receiveName.getData());
                    newName.trim();

                    if (serverIPList.indexOf(sender) == -1) {
                        serverNameList.add(newName);
                        serverIPList.add(sender);
                    }
                }
                catch (Exception e) {
                    System.out.println(e);
                }
            }

            System.out.println("UDP Client Closed");
        }
    }
}
