import java.net.*;
import java.util.*;

//game class
public class ScrabbleClient {

    final static int UDP_Port = 10165;
    final static int TCP_Port = 10166;
    //private ScrabblePlayer[] players;
    private ScrabbleBoard board;
    private boolean host;
    final private int[] tileCounts = {9, 2, 2, 4, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1, 2};
    private char[] tileCharacters = new char[tileCounts.length];
    private Vector<ScrabbleTile> tileBag;
    private int turn;
    private Vector<String> serverNameList = new Vector<String>();
    private Vector<InetAddress> serverIPList = new Vector<InetAddress>();
    private MulticastSocket clientUDPSocket;

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

    private class ClientUDP extends Thread {
        public void run() {

            clientUDPSocket = null;
            try {
                clientUDPSocket = new MulticastSocket(UDP_Port);
                InetAddress group = InetAddress.getByName("237.253.253.253");
                clientUDPSocket.joinGroup(group);
            }
            catch (Exception e) 
            {
                System.out.println(e);
            }
            byte[] receiveData = new byte[1024];
            String newName;

            System.out.println("UDP Client Running");

            while(clientUDPSocket != null && !clientUDPSocket.isClosed()) {
                DatagramPacket receiveName = new DatagramPacket(receiveData, receiveData.length);
                try {
                    clientUDPSocket.receive(receiveName);
                }
                catch (Exception e)
                {}
                InetAddress sender = receiveName.getAddress();
                newName = new String(receiveName.getData());
                newName.trim();

                if (serverIPList.indexOf(sender) == -1) {
                    serverNameList.add(newName);
                    serverIPList.add(sender);
                    }
            }

            System.out.println("UDP Client Closed");
        }
    }
}