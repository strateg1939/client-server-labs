package client_server.client;

import client_server.exceptions.ClientDataException;
import client_server.models.LoginPostDto;
import client_server.models.LoginResponseDto;
import client_server.models.Response;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.*;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

public class Storage {
    private ArrayList<Group> AllGroups;
    private static final String SERVER_URL = "https://localhost:1337/api/";
    private static HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String userName = "admin";
    private static final String password = "pass";
    private static String token;

    public Storage() {
        try {
            TrustManager [] trustAllCerts = new TrustManager [] {new X509ExtendedTrustManager() {
                @Override
                public void checkClientTrusted (X509Certificate [] chain, String authType, Socket socket) {

                }

                @Override
                public void checkServerTrusted (X509Certificate [] chain, String authType, Socket socket) {

                }

                @Override
                public void checkClientTrusted (X509Certificate [] chain, String authType, SSLEngine engine) {

                }

                @Override
                public void checkServerTrusted (X509Certificate [] chain, String authType, SSLEngine engine) {

                }

                @Override
                public java.security.cert.X509Certificate [] getAcceptedIssuers () {
                    return null;
                }

                @Override
                public void checkClientTrusted (X509Certificate [] certs, String authType) {
                }

                @Override
                public void checkServerTrusted (X509Certificate [] certs, String authType) {
                }

            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpClient.Builder clientbuilder = HttpClient.newBuilder();
            clientbuilder.sslContext(sc);
            httpClient = clientbuilder.build();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        try {
            LoginPostDto loginPostDto = new LoginPostDto(userName, password);
            String json = OBJECT_MAPPER.writeValueAsString(loginPostDto);
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI("https://localhost:1337/auth/login")).POST(HttpRequest.BodyPublishers.ofString(json)).build();
            HttpResponse<String> response = handleError(httpRequest);
            LoginResponseDto responseDto = OBJECT_MAPPER.readValue(response.body(), LoginResponseDto.class);
            token = responseDto.getToken();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void loadGroups() throws ClientDataException {
        try {
            HttpRequest httpRequest = addAuthToken(HttpRequest.newBuilder().uri(new URI(SERVER_URL + "group")).GET());
            HttpResponse<String> response = handleError(httpRequest);
            this.AllGroups = OBJECT_MAPPER.readValue(response.body(), new TypeReference<ArrayList<Group>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClientDataException(e.getMessage());
        }
    }
    public ArrayList<Group> getAllGroups() {
        return AllGroups;
    }

    public void addGroup(Group group) throws ClientDataException {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(group);
            HttpRequest httpRequest = addAuthToken(HttpRequest.newBuilder().uri(new URI(SERVER_URL + "group")).POST(HttpRequest.BodyPublishers.ofString(json)));
            HttpResponse<String> response = handleError(httpRequest);
            Group resultGroup = OBJECT_MAPPER.readValue(response.body(), Group.class);
            AllGroups.add(resultGroup);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClientDataException(e.getMessage());
        }
    }

    public void loadProducts(Group group) throws ClientDataException {
        try {
            HttpRequest httpRequest = addAuthToken(HttpRequest.newBuilder().uri(new URI(SERVER_URL + "products-by-group/" + group.id)).GET());
            HttpResponse<String> response = handleError(httpRequest);
            group.products = OBJECT_MAPPER.readValue(response.body(), new TypeReference<ArrayList<Product>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClientDataException(e.getMessage());
        }
    }

    public void deleteGroup(Group group, int idx) throws ClientDataException {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(group);
            HttpRequest httpRequest = addAuthToken(HttpRequest.newBuilder().uri(new URI(SERVER_URL + "group/" + group.id)).DELETE());
            HttpResponse<String> response = handleError(httpRequest);
            AllGroups.remove(idx);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClientDataException(e.getMessage());
        }
    }

    public boolean updateGroup(Group group) throws ClientDataException {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(group);
            HttpRequest httpRequest = addAuthToken(HttpRequest.newBuilder().uri(new URI(SERVER_URL + "group/" + group.id)).PUT(HttpRequest.BodyPublishers.ofString(json)));
            HttpResponse<String> response = handleError(httpRequest);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClientDataException(e.getMessage());
        }
        return true;
    }

    public void addProduct(Product product, Group group) throws ClientDataException {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(product);
            HttpRequest httpRequest = addAuthToken(HttpRequest.newBuilder().uri(new URI(SERVER_URL + "product")).POST(HttpRequest.BodyPublishers.ofString(json)));
            HttpResponse<String> response = handleError(httpRequest);
            Product resultProduct = OBJECT_MAPPER.readValue(response.body(), Product.class);
            group.products.add(resultProduct);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClientDataException(e.getMessage());
        }
    }

    public void deleteProduct(Product product, Group group, int idx) throws ClientDataException {
        try {
            HttpRequest httpRequest = addAuthToken(HttpRequest.newBuilder().uri(new URI(SERVER_URL + "product/" + product.id)).DELETE());
            HttpResponse<String> response = handleError(httpRequest);
            group.products.remove(idx);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClientDataException(e.getMessage());
        }
    }

    public boolean updateProduct(Product product) throws ClientDataException {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(product);
            HttpRequest httpRequest = addAuthToken(HttpRequest.newBuilder().uri(new URI(SERVER_URL + "product/" + product.id)).PUT(HttpRequest.BodyPublishers.ofString(json)));
            HttpResponse<String> response = handleError(httpRequest);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClientDataException(e.getMessage());
        }
        return true;
    }

    public HttpResponse<String> handleError(HttpRequest request) throws IOException, InterruptedException, ClientDataException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() >= 400) {
            Response message = OBJECT_MAPPER.readValue(response.body(), Response.class);
            throw new ClientDataException(message.getMessage());
        }
        return response;
    }

    private HttpRequest addAuthToken(HttpRequest.Builder builder) {
        return builder.header("Authorization", token).build();
    }

}

