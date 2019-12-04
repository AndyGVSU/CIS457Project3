import java.io.*;
import java.net.*;
import java.util.*;

public class ScrabbleTCPServer extends Thread {

static int TCP_PORT = 11657;
static String FROM_SERVER = "FS";
static String FROM_CLIENT = "FC";
final private int[] tileCounts = {9, 2, 2, 4, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1, 2};
private int[] currentTileCounts = tileCounts.clone();
private char[] tileCharacters = new char[tileCounts.length];

public boolean acceptingClients;
public ServerSocket control;
public Vector<Socket> clients;
public Vector<String> clientNames;
public Vector<DataInputStream> fromClients;
public Vector<DataOutputStream> toClients;
private boolean isOpen = true;
private int maxPlayers;

    public ScrabbleTCPServer(int maxPlayers) {
        this.maxPlayers = maxPlayers;
        acceptingClients = true;
        try {
        control = new ServerSocket(TCP_PORT);
        }
        catch (Exception e) {
            System.out.println(e);
        }
        clients = new Vector<Socket>();
        clientNames = new Vector<String>();
        fromClients = new Vector<DataInputStream>();
        toClients = new Vector<DataOutputStream>();

        char startChar = 'A';
        for (int i = 0; i < tileCounts.length; i++) {
            tileCharacters[i] = startChar;
            startChar++;
        }
    }

    public void run() {
        System.out.println("TCP Server Running");

        Socket nextSocket;
        int playerIndex = 0;

        DataInputStream nextInput;
        DataOutputStream nextOutput;

        new ConnectionListener().start();

        while (true) {
            if (clients.size() > 0)
                break;
        }
        //can only receive data from ONE PLAYER at a time
        playerIndex = 0;
        //byte[] mirror = new byte[2048];
        int cmd;
        String sentBy;
        //boolean extraData = false;

        try {
        while (isOpen) {
            //mirror received data from players serially
            nextInput = getClientInputStream(playerIndex);
            sentBy = nextInput.readUTF();

            //server can only mirror packets sent by clients
            //avoids duplicates (since client in this case has a server)
            if (sentBy.equals(ScrabbleTCPServer.FROM_SERVER)) {
                System.out.println("!!!Server got data sent by server!!!");
            }
            else {
                cmd = nextInput.readInt();
                System.out.println("SERVER: received command: "+cmd);
                
                //extraData = false;
                
                /*
                while (nextInput.available() > 0) {
                    nextInput.readFully(mirror);
                    extraData = true;
                }
                */

                
                //if (extraData)
                    //o.write(mirror);
                
                    
                //read extra-parametered commands
                int toDraw = 0, init = 0, r = 0, c = 0, autoSkip = 0;
                char letter = ' ', newChar = ' ';
                String sentTiles = "";

                if (cmd == 0) {
                    toDraw = nextInput.readInt();
                    init = nextInput.readInt();

                    //handle ALL drawn tiles
                    Vector<String> tiles = new Vector<String>();
                    sentTiles = "";
                    boolean noMoreTiles = false;

                    int tcount;
                    for (int ch = 0; ch < tileCharacters.length; ch++) {
                        tcount = currentTileCounts[ch];
                        for (int repeat = 0; repeat < tcount; repeat++)
                            tiles.add(String.valueOf(tileCharacters[ch]));
                    }
                    
                    char[] fromString;
                    Random grab = new Random();
                    for(int i = 0; i < toDraw; i++){
                        if (tiles.size() > 0) {
                            String nextChar = tiles.remove(grab.nextInt(tiles.size()));
                            fromString = nextChar.toCharArray();
                            sentTiles += fromString[0];
                            currentTileCounts[fromString[0] - 'A']--;
                        }
                        else {
                            noMoreTiles = true;
                            break;
                        }
                    }   
            }
                if (cmd == 1) {
                    newChar = nextInput.readChar();
                }
                if (cmd == 2) {
                    r = nextInput.readInt();
                    c = nextInput.readInt();
                    letter = nextInput.readChar();
                }
                if (cmd == 3) {
                    r = nextInput.readInt();
                    c = nextInput.readInt();
                    //o.writeChar(nextInput.readChar());
                }
                if (cmd == 9) {
                    autoSkip = nextInput.readInt();
                }

                for (DataOutputStream o : toClients) {
                    o.writeUTF(ScrabbleTCPServer.FROM_SERVER); //sent from server
                    o.writeInt(cmd);

                    //write extra-parametered commands
                    System.out.println("SERVER: sending command #"+Integer.toString(cmd)+
                    " to client "+ toClients.indexOf(o));

                    if (cmd == 0) {
                        o.writeInt(init);
                        o.writeUTF(sentTiles);
                    }
                    if (cmd == 1) {
                        o.writeChar(newChar);
                    }
                    if (cmd == 2) {
                        o.writeInt(r);
                        o.writeInt(c);
                        o.writeChar(letter);
                    }
                    if (cmd == 3) {
                        o.writeInt(r);
                        o.writeInt(c);
                    }
                    if (cmd == 9) {
                        o.writeInt(autoSkip);
                    }
                }

                //changes which player is sending commands
                if (cmd == ScrabbleClient.ScrabbleCommand.ADD_HAND.ordinal()) {
                    //cmd == ScrabbleClient.ScrabbleCommand.END_TURN.ordinal() ||
                //cmd == ScrabbleClient.ScrabbleCommand.PASS_TURN.ordinal() ||
                //cmd == ScrabbleClient.ScrabbleCommand.GAME_INIT.ordinal()) {
                    playerIndex++;
                    if (playerIndex >= clients.size()) {
                        playerIndex = 0;
                    }
                    System.out.println("SERVER turn = "+playerIndex);
                }
            }
        }
        }
        catch (Exception e) {
            System.out.println(e+", thrown at line # "+Integer.toString(e.getStackTrace()[0].getLineNumber()));
            
        }
    }

    public void closeLobby() {
        acceptingClients = false;
    }

    private void addClient(Socket s, String u, DataInputStream i, DataOutputStream o) {
        clients.add(s);
        clientNames.add(u);
        fromClients.add(i);
        toClients.add(o);
    }

    private Socket getClientSocket(int playerIndex) {
        return clients.get(playerIndex);
    }

    private String getClientName(int playerIndex) {
        return clientNames.get(playerIndex);
    }

    private DataOutputStream getClientOutputStream(int playerIndex) {
        return toClients.get(playerIndex);
    }

    private DataInputStream getClientInputStream(int playerIndex) {
        return fromClients.get(playerIndex);
    }

    private class ConnectionListener extends Thread {
        public void run() {
            Socket nextSocket;
            int playerIndex = 0;
    
            DataInputStream nextInput;
            DataOutputStream nextOutput;

            while (acceptingClients) {
                try {
                    nextSocket = control.accept();
                    System.out.println("SERVER: Client connected");
                    nextInput = new DataInputStream(nextSocket.getInputStream());
                    nextOutput = new DataOutputStream(nextSocket.getOutputStream());
                    String username = nextInput.readUTF();
    
                    addClient(nextSocket, username, nextInput, nextOutput);
                    //System.out.println("Server client info sending: "+Integer.toString(playerIndex));
                    getClientOutputStream(playerIndex).writeInt(playerIndex);
                    
                    int outputIndex = 0;
                    for (DataOutputStream o : toClients) {//tell each client of the new arriving player so they can update player counts
                        //if (o != toClients.lastElement()){//if they are not the new client
                            outputIndex = toClients.indexOf(o);
                            for (int i = outputIndex; i > -1; i--) {
                                o.writeUTF(ScrabbleTCPServer.FROM_SERVER);; //from server
                                o.writeInt(ScrabbleClient.ScrabbleCommand.PLAYER_INFO.ordinal());
                                o.writeUTF(getClientName(playerIndex));
                            }
                        //}
                        //else{System.out.println("Client rejected");}
                    }
                    //}
                }
                catch (Exception e) {
                    System.out.println(e);
                    System.out.println("TCP Server: Failed to set up client");
                }
                if (clients.size() == maxPlayers)
                    closeLobby();
    
                playerIndex++;
            }
        }
    }
}
