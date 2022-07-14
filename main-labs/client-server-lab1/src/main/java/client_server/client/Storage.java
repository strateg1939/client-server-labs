package client_server.client;

import client_server.models.LoginPostDto;
import client_server.models.LoginResponseDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class Storage {
    private ArrayList<Group> AllGroups;
    private static final String SERVER_URL = "http://localhost:1337/api/";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String userName = "admin";
    private static final String password = "pass";
    private static String token;
    /**
     * file name
     */
    public static final String groupFileName = "groups.txt";

    public Storage() {
        try {
            LoginPostDto loginPostDto = new LoginPostDto(userName, password);
            String json = OBJECT_MAPPER.writeValueAsString(loginPostDto);
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI("http://localhost:1337/auth/login")).POST(HttpRequest.BodyPublishers.ofString(json)).build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            LoginResponseDto responseDto = OBJECT_MAPPER.readValue(response.body(), LoginResponseDto.class);
            token = responseDto.getToken();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void loadGroups() {
        try {
            HttpRequest httpRequest = addAuthToken(HttpRequest.newBuilder().uri(new URI(SERVER_URL + "group")).GET());
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            this.AllGroups = OBJECT_MAPPER.readValue(response.body(), new TypeReference<ArrayList<Group>>() {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public ArrayList<Group> getAllGroups() {
        return AllGroups;
    }

    public void addGroup(Group group) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(group);
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(SERVER_URL + "group")).POST(HttpRequest.BodyPublishers.ofString(json)).build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            Group resultGroup = OBJECT_MAPPER.readValue(response.body(), Group.class);
            AllGroups.add(resultGroup);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadProducts(Group group) {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(SERVER_URL + "products-by-group/" + group.id)).GET().build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            group.products = OBJECT_MAPPER.readValue(response.body(), new TypeReference<ArrayList<Product>>() {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteGroup(Group group, int idx) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(group);
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(SERVER_URL + "group/" + group.id)).DELETE().build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() < 300) {
                AllGroups.remove(idx);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean updateGroup(Group group) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(group);
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(SERVER_URL + "group/" + group.id)).PUT(HttpRequest.BodyPublishers.ofString(json)).build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() < 300;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addProduct(Product product, Group group) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(product);
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(SERVER_URL + "product")).POST(HttpRequest.BodyPublishers.ofString(json)).build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            Product resultProduct = OBJECT_MAPPER.readValue(response.body(), Product.class);
            group.products.add(resultProduct);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteProduct(Product product, Group group, int idx) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(product);
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(SERVER_URL + "product/" + product.id)).DELETE().build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() < 300) {
                group.products.remove(idx);
            } else throw new RuntimeException("status " + response.statusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean updateProduct(Product product) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(product);
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(SERVER_URL + "product/" + product.id)).PUT(HttpRequest.BodyPublishers.ofString(json)).build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() < 300;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private HttpRequest addAuthToken(HttpRequest.Builder builder) {
        return builder.header("Authorization", token).build();
    }

}

