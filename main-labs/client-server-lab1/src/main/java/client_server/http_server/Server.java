package client_server.http_server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import client_server.client.Group;
import client_server.exceptions.DataAccessException;
import client_server.exceptions.DataIncorrectException;
import client_server.models.*;
import com.sun.net.httpserver.*;
import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.*;


public class Server {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final UserService _userService = new UserService("file.db");
    private final ProductService _productService = new ProductService("file.db");
    private final GroupService _groupService = new GroupService("file.db");
    private final List<Endpoint> apiEndpoints = new ArrayList<>();
    private static final String ID_PARAM = "id";

    private final HttpsServer server;

    public Server() throws IOException {
        try {
            _userService.deleteAll();
            _userService.insert(new User("admin", "pass"));
        } catch (DataAccessException e) {
            System.out.println(e.getMessage());
        }
        apiEndpoints.add(new Endpoint(Pattern.compile("\\S*/login"), this::loginHandler, (a, b) -> new HashMap<>()));
        apiEndpoints.add(new Endpoint(Pattern.compile("\\S*/product"), this::postProductHandler, (a, b) -> new HashMap<>()));
        apiEndpoints.add(new Endpoint(Pattern.compile("\\S*/products-by-group/((\\d+)$)"), this::getProductByGroup, this::getParamId));
        apiEndpoints.add(new Endpoint(Pattern.compile("\\S*/product/(\\d+)$"), this::getOrDeleteOrUpdateProductById, this::getParamId));
        apiEndpoints.add(new Endpoint(Pattern.compile("\\S*/group"), this::postGroupHandler, (a, b) -> new HashMap<>()));
        apiEndpoints.add(new Endpoint(Pattern.compile("\\S*/group/(\\d+)$"), this::getOrDeleteOrUpdateGroupById, this::getParamId));


        this.server = HttpsServer.create();
        server.bind(new InetSocketAddress(1337), 0);
        server.createContext("/api", this::rootHandler).setAuthenticator(new MyAuthenticator());
        server.createContext("/auth", this::rootHandler);
        try {
            addSSl();
        } catch (Exception e) {
            e.printStackTrace();
        }
        server.start();
    }

    private void addSSl() throws NoSuchAlgorithmException, IOException, KeyStoreException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        char[] password = "password".toCharArray();
        KeyStore keyStore = KeyStore.getInstance("JKS");
        FileInputStream stream = new FileInputStream("testkey.jks");
        keyStore.load(stream, password);
        KeyManagerFactory keyManager = KeyManagerFactory.getInstance("SunX509");
        keyManager.init(keyStore, password);
        TrustManagerFactory trustManager = TrustManagerFactory.getInstance("SunX509");
        trustManager.init(keyStore);
        sslContext.init(keyManager.getKeyManagers(), trustManager.getTrustManagers(), null);

        server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            public void configure(HttpsParameters params) {
                try {
                    SSLContext sslContext = getSSLContext();
                    SSLEngine sslEngine = sslContext.createSSLEngine();
                    params.setNeedClientAuth(false);
                    params.setCipherSuites(sslEngine.getEnabledCipherSuites());
                    params.setProtocols(sslEngine.getEnabledProtocols());
                    SSLParameters sslParameters = sslContext.getSupportedSSLParameters();
                    params.setSSLParameters(sslParameters);
                } catch (Exception e) {
                    System.out.println("Failed to create the HTTPS port");
                }
            }
        });

    }

    public void stop() {
        this.server.stop(1);
    }

    private void rootHandler(HttpExchange exchange) throws IOException {
        String url = exchange.getRequestURI().toString();
        System.out.println(url);

        Optional<Endpoint> endpoint = apiEndpoints.stream().filter(e -> e.matches(url)).findFirst();
        if (endpoint.isPresent()) {
            endpoint.get().handler().handle(exchange);
        } else {
            handlerNoFound(exchange);
        }
    }

    private void getProductByGroup(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        try (InputStream requestBody = exchange.getRequestBody()) {
            exchange.getResponseHeaders()
                    .add("Content-Type", "application/json");
            String method = exchange.getRequestMethod();
            int id = Integer.parseInt(pathParams.get(ID_PARAM));
            if (method.equals("GET")) {
                List<Product> products = _productService.getByGroupId(id);
                writeResponse(exchange, 200, products);
            } else {
                writeResponse(exchange, 404, new Response("Not appropriate command"));
            }

        } catch (DataAccessException e) {
            writeResponse(exchange, 500, new Response("get products by group fail"));
            e.printStackTrace();
        } catch (DataIncorrectException e) {
            writeResponse(exchange, 409, new Response(e.getMessage()));
        }
    }

    private void loginHandler(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        System.out.println("login");
        try (InputStream requestBody = exchange.getRequestBody()) {
            LoginPostDto loginPostDto = OBJECT_MAPPER.readValue(requestBody, LoginPostDto.class);
            User user = _userService.getByName(loginPostDto.getLogin());

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            if (user != null) {
                System.out.println(user.getPassword());
                System.out.println(DigestUtils.md5Hex(loginPostDto.getPassword()));
                if (user.getPassword().equals(DigestUtils.md5Hex(loginPostDto.getPassword()))) {
                    LoginResponseDto loginResponse = new LoginResponseDto(JwtService.generateToken(user), user.getName());
                    writeResponse(exchange, 200, loginResponse);
                } else {
                    writeResponse(exchange, 401, new Response("invalid password"));
                }
            } else {
                writeResponse(exchange, 401, new Response("unknown user"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            writeResponse(exchange, 500, new Response("Server error"));
        }
    }

    private void getOrDeleteOrUpdateProductById(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        try (InputStream requestBody = exchange.getRequestBody()) {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            String method = exchange.getRequestMethod();

            int productId = Integer.parseInt(pathParams.get(ID_PARAM));
            Product product = _productService.getOneProduct(productId);
            if (method.equals("GET")) {
                if (product != null) {
                    writeResponse(exchange, 200, product);
                } else {
                    writeResponse(exchange, 404, new Response("No product with such id"));
                }
            } else if (method.equals("DELETE")) {
                if (product != null) {
                    _productService.deleteProduct(productId);
                    exchange.sendResponseHeaders(204, -1);
                } else {
                    writeResponse(exchange, 404, new Response("No product with such id"));
                }
            } else if (method.equals("PUT")) {
                Product productReceived = OBJECT_MAPPER.readValue(requestBody, Product.class);
                if (product != null) {
                    String name = productReceived.getName();
                    if (name != null) {
                        product.setName(name);
                    }
                    double price = productReceived.getPrice();
                    if (price > 0) {
                        product.setPrice(price);
                    } else if (price < 0) {
                        writeResponse(exchange, 409, new Response("Wrong input"));
                        return;
                    }

                    int amount = productReceived.getAmount();
                    if (amount > 0) {
                        product.setAmount(amount);
                    } else if (amount < 0) {
                        writeResponse(exchange, 409, new Response("Wrong input"));
                        return;
                    }

                    String description = productReceived.getDescription();
                    if (description != null) {
                        product.setDescription(description);
                    }
                    String producer = productReceived.getProducer();
                    if (producer != null) {
                        product.setProducer(producer);
                    }
                    _productService.updateProduct(productId, product);
                    exchange.sendResponseHeaders(204, -1);
                } else {
                    writeResponse(exchange, 404, new Response("No such product"));
                }
            } else {
                writeResponse(exchange, 404, new Response("Not appropriate command"));
            }

        } catch (DataAccessException e) {
            writeResponse(exchange, 500, new Response("Delete fail"));
            e.printStackTrace();
        } catch (DataIncorrectException e) {
            writeResponse(exchange, 409, new Response(e.getMessage()));
        }
    }

    private void getOrDeleteOrUpdateGroupById(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        try (InputStream requestBody = exchange.getRequestBody()) {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            String method = exchange.getRequestMethod();

            int groupId = Integer.parseInt(pathParams.get(ID_PARAM));
            ProductGroup group = _groupService.getOneGroup(groupId);
            if (method.equals("GET")) {
                if (group != null) {
                    writeResponse(exchange, 200, group);
                } else {
                    writeResponse(exchange, 404, new Response("No group with such id"));
                }
            } else if (method.equals("DELETE")) {
                if (group != null) {
                    _groupService.deleteGroup(groupId);
                    exchange.sendResponseHeaders(204, -1);
                } else {
                    writeResponse(exchange, 404, new Response("No group with such id"));
                }
            } else if (method.equals("PUT")) {
                ProductGroup productGroup = OBJECT_MAPPER.readValue(requestBody, ProductGroup.class);
                if (group != null) {
                    String name = productGroup.getName();
                    if (name != null) {
                        group.setName(name);
                    }

                    String description = productGroup.getDescription();
                    if (description != null) {
                        group.setDescription(description);
                    }
                    _groupService.updateGroup(groupId, group);
                    exchange.sendResponseHeaders(204, -1);
                } else {
                    writeResponse(exchange, 404, new Response("No such product"));
                }
            } else {
                writeResponse(exchange, 404, new Response("Not appropriate command"));
            }

        } catch (DataAccessException e) {
            writeResponse(exchange, 500, new Response("Delete fail"));
            e.printStackTrace();
        } catch (DataIncorrectException e) {
            writeResponse(exchange, 409, new Response(e.getMessage()));
        }
    }

    private void postProductHandler(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        try (InputStream requestBody = exchange.getRequestBody()) {
            exchange.getResponseHeaders()
                    .add("Content-Type", "application/json");
            String method = exchange.getRequestMethod();

            if (method.equals("POST")) {
                System.out.println("post product");
                Product product = OBJECT_MAPPER.readValue(requestBody, Product.class);
                if (product != null) {
                    if (product.getAmount() >= 0 && product.getPrice() >= 0) {
                        Product created = _productService.createProduct(product);
                        writeResponse(exchange, 201, created);
                    } else {
                        System.out.println("incorrect");
                        writeResponse(exchange, 409, new Response("Wrong input"));
                    }
                } else {
                    System.out.println("null");
                    writeResponse(exchange, 409, new Response("Wrong input"));
                }
            } else if (method.equals("GET")) {
                List<Product> products = _productService.getAllProducts();
                writeResponse(exchange, 200, products);
            } else {
                writeResponse(exchange, 404, new Response("Not appropriate command"));
            }

        } catch (DataAccessException e) {
            System.out.println("fail");
            writeResponse(exchange, 500, new Response("post fail"));
            e.printStackTrace();
        } catch (DataIncorrectException e) {
            System.out.println("fail");
            writeResponse(exchange, 409, new Response(e.getMessage()));
        } catch (Exception e) {
            System.out.println("Major fail");
            e.printStackTrace();
        }
    }

    private void postGroupHandler(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        try (InputStream requestBody = exchange.getRequestBody()) {
            exchange.getResponseHeaders()
                    .add("Content-Type", "application/json");
            String method = exchange.getRequestMethod();

            if (method.equals("POST")) {
                ProductGroup group = OBJECT_MAPPER.readValue(requestBody, ProductGroup.class);
                if (group != null) {
                    ProductGroup created = _groupService.createGroup(group);
                    writeResponse(exchange, 200, created);
                } else {
                    writeResponse(exchange, 409, new Response("Wrong input"));
                }
            } else if (method.equals("GET")) {
                List<ProductGroup> productGroups = _groupService.getAllGroups();
                writeResponse(exchange, 200, productGroups);
            } else {
                writeResponse(exchange, 404, new Response("Not appropriate command"));
            }

        } catch (DataAccessException e) {
            writeResponse(exchange, 500, new Response("Delete fail"));
            e.printStackTrace();
        } catch (DataIncorrectException e) {
            writeResponse(exchange, 409, new Response(e.getMessage()));
        }
    }

    private Map<String, String> getParamId(String uri, Pattern pattern) {
        Matcher matcher = pattern.matcher(uri);
        matcher.find();

        return new HashMap<String, String>() {{
            put(ID_PARAM, matcher.group(1));
        }};
    }

    private void handlerNoFound(HttpExchange exchange) {
        try {
            exchange.sendResponseHeaders(404, 0);
            exchange.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
        byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(response);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private class MyAuthenticator extends Authenticator {
        @Override
        public Result authenticate(HttpExchange httpExchange) {
            String token = httpExchange.getRequestHeaders().getFirst(AUTHORIZATION_HEADER);
            if (token != null) {
                try {
                    String username = JwtService.getUsernameFromToken(token);
                    User user = _userService.getByName(username);
                    if (user != null) {
                        return new Success(new HttpPrincipal(username, user.getName()));
                    } else {
                        return new Retry(401);
                    }
                } catch (Exception e) {
                    return new Failure(403);
                }
            }
            return new Retry(401);
        }
    }

}
