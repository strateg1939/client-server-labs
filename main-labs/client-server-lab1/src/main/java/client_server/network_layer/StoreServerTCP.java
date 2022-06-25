package client_server.network_layer;

import client_server.Constants;
import client_server.Message;
import client_server.network_layer.FakeSender;
import client_server.network_layer.Sender;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class StoreServerTCP implements Sender {
    private ThreadPoolExecutor storeTcpReceiver = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    private Map<Byte, Socket> map = new ConcurrentHashMap<>();
    private ServerSocket serverSocket;
    public StoreServerTCP(){
        try {
            serverSocket = new ServerSocket(Constants.TCP_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        storeTcpReceiver.submit(new StoreServerTCPTask(serverSocket));
    }

    public Map<Byte, Socket> getSocketMap(){
        return map;
    }

    public void shutdown(){
        storeTcpReceiver.shutdownNow();
    }

    @Override
    public void sendMessage(byte[] message, byte srcId) throws IOException {
        Socket socket = map.get(srcId);
        OutputStream out = socket.getOutputStream();
        out.write(message);
        out.flush();
        out.close();
        socket.close();
        map.remove(srcId);
    }
}

