package client_server.http_server;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import client_server.Constants;
import client_server.client.Group;
import client_server.exceptions.DataAccessException;
import client_server.exceptions.DataIncorrectException;
import client_server.models.*;
import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpServer;



public class Server {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final UserService _userService = new UserService("file.db");
    private final ProductService _productService = new ProductService("file.db");
    private final GroupService _groupService = new GroupService("file.db");
    private final List<Endpoint> apiEndpoints = new ArrayList<>();

    private final HttpServer server;

    public Server() throws IOException {
        try {
            _userService.deleteAll();
            _userService.insert(new User("admin", "pass"));
        } catch (DataAccessException e) {
            System.out.println(e.getMessage());
        }
        apiEndpoints.add(new Endpoint(Pattern.compile("\\S*/login"), this::loginHandler, (a, b) -> new HashMap<>()));
        apiEndpoints.add(new Endpoint(Pattern.compile("\\S*/product"), this::postProductHandler, (a, b) -> new HashMap<>()));
        apiEndpoints.add(new Endpoint(Pattern.compile("\\S*/product/(\\d+)$"), this::getOrDeleteOrUpdateProductById, this::getProductParamId));
        apiEndpoints.add(new Endpoint(Pattern.compile("\\S*/group"), this::postGroupHandler, (a, b) -> new HashMap<>()));
        apiEndpoints.add(new Endpoint(Pattern.compile("\\S*/group/(\\d+)$"), this::getOrDeleteOrUpdateGroupById, this::getProductParamId));


        this.server = HttpServer.create();
        server.bind(new InetSocketAddress(Constants.TCP_PORT), 0);
        server.createContext("/api", this::rootHandler);
   //         .setAuthenticator(new MyAuthenticator());
        server.createContext("/auth", this::rootHandler);
        server.start();
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

    private void loginHandler(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
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


    private void getAllGroups(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        var list = new ArrayList<Group>();
        list.add(new Group("test", "test"));
        writeResponse(exchange, 200, list);
    }
    private void getOrDeleteOrUpdateProductById(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        try (InputStream requestBody = exchange.getRequestBody()) {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            String method = exchange.getRequestMethod();

            int productId = Integer.parseInt(pathParams.get("productId"));
            Product product = _productService.get(productId);
            if (method.equals("GET")) {
                if (product != null) {
                    writeResponse(exchange, 200, product);
                } else {
                    writeResponse(exchange, 404, new Response("No product with such id"));
                }
            } else if (method.equals("DELETE")) {
                if (product != null) {
                    _productService.delete(productId);
                    exchange.sendResponseHeaders(204, -1);
                } else {
                    writeResponse(exchange, 404, new Response("No product with such id"));
                }
            } else if(method.equals("PUT")) {
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

                    String description = productReceived.getProductGroupName();
                    if (description != null) {
                        product.setProductGroupName(description);
                    }
                    _productService.update(product);
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

            int groupId = Integer.parseInt(pathParams.get("productId"));
            ProductGroup group = _groupService.getOneGroup(groupId);
            if (method.equals("GET")) {
                if (group != null) {
                    writeResponse(exchange, 200, group);
                } else {
                    writeResponse(exchange, 404, new Response("No group with such id"));
                }
            } else if (method.equals("DELETE")) {
                if (group != null) {
                    _productService.delete(groupId);
                    exchange.sendResponseHeaders(204, -1);
                } else {
                    writeResponse(exchange, 404, new Response("No group with such id"));
                }
            } else if(method.equals("PUT")) {
                Product productReceived = OBJECT_MAPPER.readValue(requestBody, Product.class);
                if (group != null) {
                    String name = productReceived.getName();
                    if (name != null) {
                        group.setName(name);
                    }

                    String description = productReceived.getProductGroupName();
                    if (description != null) {
                        group.setName(description);
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
                Product product = OBJECT_MAPPER.readValue(requestBody, Product.class);
                if (product != null) {
                    if (product.getAmount() >= 0 && product.getPrice() > 0) {
                        long id = _productService.insert(product);
                        writeResponse(exchange, 201, new Response("{ \"id\" : " + id + "}"));
                    } else {
                        writeResponse(exchange, 409, new Response("Wrong input"));
                    }
                } else {
                    writeResponse(exchange, 409, new Response("Wrong input"));
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

    private void postGroupHandler(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        try (InputStream requestBody = exchange.getRequestBody()) {
            exchange.getResponseHeaders()
                    .add("Content-Type", "application/json");
            String method = exchange.getRequestMethod();

            if (method.equals("POST")) {
                ProductGroup group = OBJECT_MAPPER.readValue(requestBody, ProductGroup.class);
                if (group != null) {

                        ProductGroup group1 = _groupService.createGroup(group);
                        long id = group1.getId();
                        writeResponse(exchange, 201, new Response("{ \"id\" : " + id + "}"));

                        writeResponse(exchange, 409, new Response("Wrong input"));
                } else {
                    writeResponse(exchange, 409, new Response("Wrong input"));
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

    private Map<String, String> getProductParamId(String uri, Pattern pattern) {
        Matcher matcher = pattern.matcher(uri);
        matcher.find();

        return new HashMap<String, String>() {{
            put("productId", matcher.group(1));
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
