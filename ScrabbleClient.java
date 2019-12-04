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

    //private Vector<ScrabbleTile> tileBag;
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

    private Vector<Integer> lastCommandQueue = new Vector<Integer>();

    public enum ScrabbleCommand {ADD_HAND, REM_HAND, ADD_BOARD_TILE, REM_BOARD_TILE, 
        END_TURN, PASS_TURN, START_GAME, END_GAME, PLAYER_INFO, GAME_INIT}

    public ScrabbleClient(boolean host) {
        //System.out.println("GHOST: "+Integer.toString(ADD_HAND));
        this.host = host;

        //blank is left bracket
        char startChar = 'A';
        for (int i = 0; i < tileCounts.length; i++) {
            tileCharacters[i] = startChar;
            startChar++;
        }
        
        /*
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
        */

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

    
    public void turnStart(int recentTurn) {
        //int handSize = players.get(recentTurn).getHandSize();
        
        //sendAddHand(ScrabblePlayer.HAND_SIZE - handSize);
    }
    
    public void sendStartGame() {
        sendCommand(ScrabbleCommand.START_GAME);
    }

    public void sendGameInit(int autoSkip) {
        sendCommand(ScrabbleCommand.GAME_INIT);
        try {
            client.getOutputStream().writeInt(autoSkip);
            }
            catch (Exception e) {System.out.println(e);}
    }

    public void sendAddHand(int toDraw, int init) {
        sendCommand(ScrabbleCommand.ADD_HAND);
        DataOutputStream o = client.getOutputStream();
        try {
        o.writeInt(toDraw);
        o.writeInt(init);
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

    public int getMyPlayer() {
        return playerIndex;
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
        lastCommandQueue.add(Integer.valueOf(c));
    }

    public int getLastCommand() {
        if (lastCommandQueue.size() > 0) {
            return lastCommandQueue.remove(0).intValue();
        }
        return -1;
    }

    //only for command values; additional arguments must be provided when necessary
    public void sendCommand(ScrabbleCommand cmd) {
    	try {
    	System.out.println("CLIENT: sending command: "+Integer.toString(cmd.ordinal())+", "+cmd);
        DataOutputStream out = client.getOutputStream();
        out.writeUTF(ScrabbleTCPServer.FROM_CLIENT);
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
            System.out.println("CLIENT: TCP Client Running");
            clientTCPSocket = null;
            try {
                clientTCPSocket = new Socket(host, ScrabbleTCPServer.TCP_PORT);
                System.out.println("CLIENT: Connected to "+host);
                fromServer = new DataInputStream(clientTCPSocket.getInputStream());
                toServer = new DataOutputStream(clientTCPSocket.getOutputStream());

                //send name
                toServer.writeUTF(username);
                //get MY player index
                playerIndex = fromServer.readInt();
                System.out.println("CLIENT: Assigned player "+playerIndex);
            }
            catch (Exception e) {
                System.out.println(e);
                System.out.println("Failed to set up TCP Client");
                isOpen = false;
            }

            String sentByServer;
            int command = -1;
            while (isOpen) {
                //read next command
                try {
                sentByServer = fromServer.readUTF(); //get from client (0) or server (1)
                if (sentByServer.equals(ScrabbleTCPServer.FROM_CLIENT))
                    System.out.println("!!!Client got data sent by client!!!");
                else
                    {
                    command = fromServer.readInt();
                    ScrabbleCommand convert = ScrabbleCommand.values()[command];
                    System.out.println("CLIENT: received command "+command+", "+convert.toString());
                    
                    switch(convert) {

                        case ADD_HAND:
                            int init = fromServer.readInt();
                            String drawnTiles = fromServer.readUTF();
                            char newChar;

                            for (int i = 0; i < drawnTiles.length(); i++) {
                                newChar = drawnTiles.charAt(i);

                                currentTileCounts[newChar - 'A']--;

                                players.get(turn).addTile(new ScrabbleTile(newChar));
                            }

                            turn++;
                            if (turn == getPlayerCount()) {
                                turn = 0; //if everyone has had a turn this round
                            }

                            if (init == 1 && getMyTurn() && turn != 0) {
                                sendAddHand(ScrabblePlayer.HAND_SIZE, 1);
                            }

                            break;
                        case REM_HAND:
                            char toRemove = fromServer.readChar();
                            ScrabblePlayer currentPlayer = players.get(turn);
                            for(int i = 0; i < currentPlayer.getHandSize(); i++){
                                if (currentPlayer.getTile(i).getLetter() == toRemove) {
                                    currentPlayer.removeTile(i);
                                    break;
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
                            System.out.println("END TURN");
                            //score already-verified tiles
                            int score = board.validatePlacedTiles();
                            
                            if (score > 0) {
                                ScrabblePlayer play = players.get(turn);
                                play.addScore(score);
                                if (getMyTurn())
                                    sendAddHand(ScrabblePlayer.HAND_SIZE - play.getHandSize(), 0);
                            }
                            //if score > 0, addHand

                            break;
                        case PASS_TURN:
                            //System.out.println("pass turn");
                            
                            //int ghost = board.validatePlacedTiles();//get total points of placed tiles (0 if invalid)
                            //if (ghost != 0){//valid board
                            //System.out.println("Valid board with score of "+Integer.toString(ghost));
                            
                            //whether to automatically pass for the next player
                            //int autoSkip = fromServer.readInt();
                            /*
                            if (getMyTurn()) {
                                turnStart(turn);
                            }
                            */
                            turn++;
                            if (turn == getPlayerCount()) {
                                turn = 0;//if everyone has had a turn this round
                            }

                            System.out.println("TURN "+turn);

                            //}
                            //else{//invalid board
                            //    System.out.println("Error: Board is invalid.");
                            //}
                            break;
                        case START_GAME:
                            //set up game on client-end

                            for (int i = 0; i < playerCount; i++) {
                                players.add(new ScrabblePlayer(playerNameList.get(i)));
                            }
                            //each player should have an automatic pass to refill hand

                            if (getMyTurn())
                                sendAddHand(ScrabblePlayer.HAND_SIZE, 1);
                                //sendGameInit(1);
                            //turnStart();

                            break;
                        case END_GAME:

                            System.exit(0);
                            break;
                        case PLAYER_INFO://read info from a new connecting player.
                            playerNameList.clear();
                            int clientCount = fromServer.readInt();
                            for (int i = 0; i < clientCount; i++)
                                playerNameList.add(fromServer.readUTF());
                            
                            playerCount = clientCount;
                            break;
                        case GAME_INIT:
                        /*
                            int autoSkip = fromServer.readInt();

                            int nextAuto = 0;
                            if (autoSkip == 1) {
                                if (turn < getPlayerCount() - 1)
                                    nextAuto = 1;
                            }

                            if (getMyTurn()) {
                                sendAddHand(ScrabblePlayer.HAND_SIZE);
                                //sendPassTurn();
                                if (nextAuto == 1)
                                    sendGameInit(1);
                                else
                                    sendPassTurn();
                            }
                        */
                            break;
                    }
                }
            } catch(Exception e) {
                System.out.println("ERROR READING!" + e + ", occured at line #"+Integer.toString(e.getStackTrace()[0].getLineNumber()));
                System.exit(0);
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
                    System.out.println("UDP Client Closed");
                }
            }
        }
    }
}
