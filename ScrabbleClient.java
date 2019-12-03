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
    
    public void endTurn() {
        try {
        sendCommand(ScrabbleCommand.END_TURN);
        }
        catch(Exception e) {
            System.out.println(e);
        }
    };
    
    public void passTurn() {
        try {
        sendCommand(ScrabbleCommand.PASS_TURN);
        }
        catch(Exception e) {
            System.out.println(e);
        }
    };

    public int[] getCurrentTileCounts() {
    	return currentTileCounts;
    }
    
    public void startGame() {
        try {
        sendCommand(ScrabbleCommand.START_GAME);
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public void turnStart() {
        int handSize = players.get(playerIndex).getHandSize();
        
        sendCommand(ScrabbleCommand.ADD_HAND);
    }

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
                command = fromServer.readInt();
                ScrabbleCommand convert = ScrabbleCommand.values()[command];
                System.out.println("Client received command: "+command);

                switch(convert) {
                    case ADD_HAND:
                        tileBag = new Vector<ScrabbleTile>();
                    	for(int i = 0; i < tileCounts.length; i++){
                            char nextChar = tileCharacters[i];
                            players.get(playerIndex).addTile(new ScrabbleTile(nextChar));
                        }
                        toServer.writeUTF("ADD_HAND");
                        break;
                    case REM_HAND:
                        for(int i = 0; i < players.get(playerIndex).getHandSize(); i++){
                            players.get(playerIndex).removeTile(i);
                        }
                        toServer.writeUTF("REM_HAND");
                        break;
                    case ADD_BOARD_TILE:
                        int row = fromServer.readInt();
                        int column = fromServer.readInt();
                        ScrabbleTile tileName = fromServer.readUTF;
                        board.addBoardTile(row, column, tileName);
                        //currentTileCounts[char index]--; //this is for keeping track of tiles left
                        toServer.writeUTF("ADD_BOARD_TILE");
                        break;
                    case REM_BOARD_TILE:
                        int rw = fromServer.readInt();
                        int col = fromServer.readInt();
                        board.removeBoardTile(rw, col);
                        toServer.writeUTF("REM_BOARD_TILE");
                        break;
                    case END_TURN:
                        //score already-verified tiles
                        board.scorePlacedTiles();
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
                        
                        if (getMyTurn()){
                            turnStart();
                        }
                        break;
                    case START_GAME:
                        //set up game on client-end
                        for (int i = 0; i < playerCount; i++) {
                            players.add(new ScrabblePlayer(playerNameList.get(i)));
                        }
                        
                        //System.out.println(Integer.toString(playerIndex)+" YEEE");
                        
                        
                        //each player should have an automatic pass to refill hand

                        if (getMyTurn())
                            turnStart();
                        break;
                    case END_GAME:

                        break;
                    case PLAYER_INFO:
                        playerNameList.add(fromServer.readUTF());
                        playerCount++;
                        break;
                }
            } catch(Exception e) {
                System.out.println(e);
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
