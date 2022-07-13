package client_server.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class Storage implements CreateDelete {
    private ArrayList<Group> AllGroups;
    private static final String SERVER_URL = "http://localhost:1337/api/";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    /**
     * file name
     */
    public static final String groupFileName = "groups.txt";

    public Storage() {
    }

    public void loadGroups() {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(SERVER_URL + "groups")).GET().build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            this.AllGroups = OBJECT_MAPPER.readValue(response.body(), new TypeReference<ArrayList<Group>>() {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public ArrayList<Group> getAllGroups() {
        return AllGroups;
    }

    /**
     * save all information
     */
    public void saveGroups() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(groupFileName));
            oos.writeObject(AllGroups);
            oos.close();
        } catch (Exception e) {
            System.out.println("Something is wrong with save");
        }
    }

    @Override
    public void add(Object group) {
        AllGroups.add((Group) group);
    }

    @Override
    public void remove(int index) {
        AllGroups.remove(index);
    }
}

