package client_server;


import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class App
{
    public static void main(String[] args) throws NoSuchAlgorithmException {

        Message message = new Message(1,1,new MessageObject("message"));
        byte srcId = 34;
        PacketSerializer packetSerializer = new PacketSerializer(message, srcId);
        Packet packet = new Packet(packetSerializer.getPacket());
        int a  = 1;
    }
}
