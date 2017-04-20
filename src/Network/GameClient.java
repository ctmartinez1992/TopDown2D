package Network;

import Entity.PlayerMP;
import Game.Game;
import Network.Packet.Packet;
import Network.Packet.Packet.PacketTypes;
import Network.Packet.Packet00Login;
import Network.Packet.Packet01Disconnect;
import java.io.IOException;
import java.net.*;

public class GameClient extends Thread {

    private InetAddress ipAddress;
    private DatagramSocket socket;
    private Game game;

    public GameClient(Game game, String ipAddress) {
        this.game = game;
        
        try {
            this.socket = new DatagramSocket();
            this.ipAddress = InetAddress.getByName(ipAddress);
        } catch (SocketException | UnknownHostException e) {
        }
    }

    @Override
    public void run() {
        while (true) {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
            }
            
            this.parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
        }
    }

    private void parsePacket(byte[] data, InetAddress address, int port) {
        String message = new String(data).trim();
        PacketTypes type = Packet.lookupPacket(message.substring(0, 2));
        Packet packet;
        
        switch (type) {
            default:
                break;
                
            case INVALID:
                break;
                
            case LOGIN:
                packet = new Packet00Login(data);
                
                System.out.println("[" + address.getHostAddress() + ":" + port + "] " + ((Packet00Login) packet).getUsername() + " has joined the game...");
                
                PlayerMP player = new PlayerMP(game.level, 100, 100, ((Packet00Login) packet).getUsername(), address, port);
                game.level.addEntity(player);
                
                break;
                
            case DISCONNECT:
                packet = new Packet01Disconnect(data);
                
                System.out.println("[" + address.getHostAddress() + ":" + port + "] " + ((Packet01Disconnect) packet).getUsername() + " has left the world...");
                
                game.level.removePlayerMP(((Packet01Disconnect) packet).getUsername());
                
                break;
        }
    }

    public void sendData(byte[] data) {
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, 1331);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
