import java.net.*;
import java.util.concurrent.TimeUnit;

public class ScrabbleUDPServer extends Thread {
    static String UDP_MULTICAST = "237.253.253.253";
    static int UDP_PORT = 11656;
    private String serverName;

    public ScrabbleUDPServer(String name) {
        serverName = name;
    }
    public void run() {
        try {
            byte[] sendData = new byte[1024];
            sendData = serverName.getBytes();
            InetAddress group = InetAddress.getByName(UDP_MULTICAST);

            MulticastSocket serverSocket = new MulticastSocket(UDP_PORT);
            serverSocket.joinGroup(group);

            System.out.println("UDP Server Running");

            DatagramPacket sendName = new DatagramPacket(sendData,sendData.length, group, UDP_PORT);

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
