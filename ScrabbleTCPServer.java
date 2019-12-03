import java.io.*;
import java.net.*;
import java.util.*;

public class ScrabbleTCPServer extends Thread {

static int TCP_PORT = 11657;
public boolean acceptingClients;
public ServerSocket control;
public Vector<Socket> clients;
public Vector<String> clientNames;
public Vector<DataInputStream> fromClients;
public Vector<DataOutputStream> toClients;
private boolean isOpen = true;

    public ScrabbleTCPServer() {
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
        byte[] mirror = new byte[2048];
        int cmd;
        boolean sentBy;
        boolean extraData = false;

        try {
        while (isOpen) {
            //mirror received data from players serially
            nextInput = getClientInputStream(playerIndex);
            sentBy = nextInput.readBoolean();

            //server can only mirror packets sent by clients
            //avoids duplicates (since client in this case has a server)
            if (!sentBy) {
                cmd = nextInput.readInt();
                System.out.println("Server received command: "+cmd);
                
                //extraData = false;
                
                /*
                while (nextInput.available() > 0) {
                    nextInput.readFully(mirror);
                    extraData = true;
                }
                */

                for (DataOutputStream o : toClients) {
                    o.writeBoolean(true);
                    o.writeInt(cmd);
                    //if (extraData)
                        //o.write(mirror);
                    System.out.println("Server sending command #"+Integer.toString(cmd));
                    if (cmd == 0)
                        o.writeInt(nextInput.readInt());
                    if (cmd == 1)
                        o.writeChar(nextInput.readChar());
                    if (cmd == 2) {
                        o.writeInt(nextInput.readInt());
                        o.writeInt(nextInput.readInt());
                        o.writeChar(nextInput.readChar());
                    }
                    if (cmd == 3) {
                        o.writeInt(nextInput.readInt());
                        o.writeInt(nextInput.readInt());
                        o.writeChar(nextInput.readChar());
                    }
                }

                //changes which player is sending commands
                if (cmd == ScrabbleClient.ScrabbleCommand.END_TURN.ordinal() ||
                cmd == ScrabbleClient.ScrabbleCommand.PASS_TURN.ordinal()) {
                    playerIndex++;
                    if (playerIndex >= clients.size()) {
                        playerIndex = 0;
                    }
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
        
        try {
        for (DataOutputStream out : toClients) {
            out.writeInt(ScrabbleClient.ScrabbleCommand.START_GAME.ordinal());
        }
        } catch(Exception e) {}
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
                    System.out.println("Client connected");
                    nextInput = new DataInputStream(nextSocket.getInputStream());
                    nextOutput = new DataOutputStream(nextSocket.getOutputStream());
                    String username = nextInput.readUTF();
    
                    addClient(nextSocket, username, nextInput, nextOutput);
                    //System.out.println("Server client info sending: "+Integer.toString(playerIndex));
                    getClientOutputStream(playerIndex).writeInt(playerIndex);
                    //
                    int outputIndex = 0;
                    for (DataOutputStream o : toClients) {//tell each client of the new arriving player so they can update player counts
                        //if (o != toClients.lastElement()){//if they are not the new client
                            outputIndex = toClients.indexOf(o);
                            for (int i = outputIndex; i > -1; i--) {
                                o.writeBoolean(true);
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
                if (clients.size() == 4)
                    closeLobby();
    
                playerIndex++;
            }
        }
    }
}
