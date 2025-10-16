package com.autumn.web;

import com.autumn.web.converter.HttpMessageConverter;
import com.autumn.web.filter.Filter;
import com.autumn.web.mapping.HandlerMapping;
import com.autumn.web.mapping.RequestBody;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MiniDispatcher {

    private final HandlerMapping handlerMapping = new HandlerMapping();
    private final List<Filter> filters = new ArrayList<>();
    private final List<HttpMessageConverter> converters = new ArrayList<>();

    public void registerController(Class<?> controllerClass) {
        handlerMapping.registerController(controllerClass);
    }

    public void addFilter(Filter filter) {
        filters.add(filter);
    }

    public void addConverter(HttpMessageConverter converter) {
        converters.add(converter);
    }

    public void start(int port) throws IOException {
        try (var server = new ServerSocket(port)) {
            System.out.println("Autumn REST API running on port " + port);

            while (true) {
                var client = server.accept();
                new Thread(() -> handleClient(client)).start();
            }
        }
    }

    private void handleClient(Socket client) {
        try (
                var in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                var out = client.getOutputStream()) {
            var requestLine = in.readLine();
            if (requestLine == null || requestLine.isBlank())
                return;

            var parts = requestLine.split(" ");
            var method = parts[0];
            var path = parts[1];

            var request = new HttpRequest(method, path);

            // Read headers
            var headers = new HashMap<String, String>();
            String line;
            while (!(line = in.readLine()).isEmpty()) {
                var headerParts = line.split(":", 2);
                if (headerParts.length == 2) {
                    headers.put(headerParts[0].trim(), headerParts[1].trim());
                }
            }

            // Read body if POST
            String body = null;
            if ("POST".equalsIgnoreCase(method)) {
                int contentLength = headers.containsKey("Content-Length")
                        ? Integer.parseInt(headers.get("Content-Length"))
                        : 0;
                if (contentLength > 0) {
                    char[] buf = new char[contentLength];
                    in.read(buf, 0, contentLength);
                    body = new String(buf);
                }
            }

            // Apply filters
            for (var filter : filters) {
                if (!filter.doFilter(request)) {
                    client.close();
                    return;
                }
            }

            // Find handler
            var handlerMethod = handlerMapping.getHandler(method, path);
            String responseBody;
            int statusCode = 200;

            if (handlerMethod != null) {
                var controller = handlerMapping.getControllerInstance(handlerMethod.getDeclaringClass());
                var parameters = handlerMethod.getParameters();
                var args = new Object[parameters.length];

                // Map @RequestBody parameters
                for (int i = 0; i < parameters.length; i++) {
                    var param = parameters[i];
                    if (param.isAnnotationPresent(RequestBody.class) && body != null) {
                        // Find converter
                        for (var converter : converters) {
                            if (converter.supports(headers.getOrDefault("Content-Type", ""))) {
                                args[i] = converter.read(body, param.getType());
                                break;
                            }
                        }
                    }
                }

                var result = handlerMethod.invoke(controller, args);
                responseBody = (result != null) ? result.toString() : "";
            } else {
                statusCode = 404;
                responseBody = "Not Found";
            }

            // Convert response with JSON if Accept header asks for it
            String accept = headers.getOrDefault("Accept", "text/plain");
            for (var converter : converters) {
                if (converter.supports(accept)) {
                    responseBody = converter.write(responseBody);
                    break;
                }
            }

            var response = """
                    HTTP/1.1 %d %s
                    Content-Type: %s
                    Content-Length: %d
                    Connection: close

                    %s
                    """.formatted(statusCode, statusCode == 200 ? "OK" : "Not Found",
                    accept, responseBody.getBytes("UTF-8").length, responseBody);

            out.write(response.getBytes("UTF-8"));
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException ignored) {
            }
        }
    }

}
