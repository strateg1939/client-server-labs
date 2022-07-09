package client_server.http_server;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

public class Endpoint {

    private Pattern urlPattern;
    private CustomHandler httpHandler;
    private BiFunction<String, Pattern, Map<String, String>> paramsExtractor;

    public Endpoint(Pattern urlPattern, CustomHandler httpHandler, BiFunction<String, Pattern, Map<String, String>> paramsExtractor) {
        this.urlPattern = urlPattern;
        this.httpHandler = httpHandler;
        this.paramsExtractor = paramsExtractor;
    }

    public boolean matches(String url) {
        return urlPattern.matcher(url).matches();
    }

    public HttpHandler handler() {
        return httpExchange -> {
            Map<String, String> params = paramsExtractor.apply(httpExchange.getRequestURI().toString(), urlPattern);
            httpHandler.handle(httpExchange, params);
        };
    }

    public interface CustomHandler {
        void handle(HttpExchange exchange, Map<String, String> pathParams) throws IOException;
    }

}
