import java.io.*;
import java.net.*;
import java.util.*;

//game class
public class ScrabbleClient {

    private ScrabbleBoard board;
    private boolean host;
    final private int[] tileCounts = {9, 2, 2, 4, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1, 2};
    private char[] tileCharacters = new char[tileCounts.length];
    private Vector<ScrabbleTile> tileBag;
    private int turn;
    private Vector<String> serverNameList = new Vector<String>();
    private Vector<InetAddress> serverIPList = new Vector<InetAddress>();
    private Vector<String> playerNameList = new Vector<String>();
    private MulticastSocket clientUDPSocket;
    private Socket clientTCPSocket;
    private ClientTCP client;
    private int playerIndex = -1;
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
        return 4;
        //return players.length;
    }

    public ScrabbleBoard getBoard() {
        return board;
    }
    
    public void endTurn() {};
    
    public void passTurn() {};

    public void setIsHost(boolean h) {
        host = h;
    }

    public boolean getIsHost() {
        return host;
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
        int l = lastCommand;
        lastCommand = -1;
        return l;
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
                //get player index
                playerIndex = fromServer.readInt();
                /*
                for (int i = playerIndex - 1; i > -1; i--) {
                    
                }
                playerNameList.add(username);
                */
            }
            catch (Exception e) {
                System.out.println(e);
                System.out.println("Failed to set up TCP Client");
                isOpen = false;
            }

            int command = -1;
            while (isOpen) {
                //read next command
                try {
                command = fromServer.readInt();
                ScrabbleCommand convert = ScrabbleCommand.values()[command];

                switch(convert) {
                    case ADD_HAND:

                        break;
                    case REM_HAND:
                    
                        break;
                    case ADD_BOARD_TILE:
                    
                        break;
                    case REM_BOARD_TILE:
                    
                        break;
                    case END_TURN:
                    
                        break;
                    case PASS_TURN:
                    
                        break;
                    case START_GAME:

                        break;
                    case END_GAME:
                    
                        break;
                    case PLAYER_INFO:
                        playerNameList.add(fromServer.readUTF());
                        break;
                }
            } catch(Exception e) {}
            setLastCommand(command);
            }
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