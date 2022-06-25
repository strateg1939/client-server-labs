package client_server.network_layer;

import client_server.Constants;
import client_server.Packet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class StoreServerTCPTask extends Thread{
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private InputStream in;

    public StoreServerTCPTask(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    public void run() {
        try {
            while(!Thread.interrupted()) {
                clientSocket = serverSocket.accept();
                in = clientSocket.getInputStream();
                byte[] packet = in.readAllBytes();
                Constants.DECRYPTOR.decrypt(packet,clientSocket);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}

