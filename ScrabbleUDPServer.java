import java.net.*;
import java.util.concurrent.TimeUnit;

public class ScrabbleUDPServer extends Thread {
    private String serverName;

    public ScrabbleUDPServer(String name) {
        serverName = name;
    }
    public void run() {
        try {
            byte[] sendData = new byte[1024];
            sendData = serverName.getBytes();
            InetAddress group = InetAddress.getByName("237.253.253.253");

            MulticastSocket serverSocket = new MulticastSocket(ScrabbleClient.UDP_Port);
            serverSocket.joinGroup(group);

            System.out.println("UDP Server Running");

            DatagramPacket sendName = new DatagramPacket(sendData,sendData.length, group, ScrabbleClient.UDP_Port);

            while(!serverSocket.isClosed()) {
                TimeUnit.SECONDS.sleep(2);
                serverSocket.send(sendName);
            }

            serverSocket.leaveGroup(group);
            serverSocket.close();
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }
}
