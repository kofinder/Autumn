package com.autumn.web;

import com.autumn.AutumnApplicationContext;
import com.autumn.beans.PathVariable;
import com.autumn.beans.RequestBody;
import com.autumn.beans.RequestParam;
import com.autumn.beans.RestController;
import com.autumn.web.converter.HttpMessageConverter;
import com.autumn.web.filter.Filter;
import com.autumn.web.mapping.HandlerMapping;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class MiniDispatcher {

    private final HandlerMapping handlerMapping = new HandlerMapping();
    private final List<Filter> filters = new ArrayList<>();
    private final List<HttpMessageConverter> converters = new ArrayList<>();

    public MiniDispatcher(AutumnApplicationContext context) {
        // Auto-register controllers
        for (Class<?> beanClass : context.getAllRegisteredBeans()) {
            if (beanClass.isAnnotationPresent(RestController.class)) {
                Object controller = context.getBean(beanClass);
                registerController(beanClass, controller);
            }
        }
    }

    public void addFilter(Filter filter) {
        filters.add(filter);
    }

    public void addConverter(HttpMessageConverter converter) {
        converters.add(converter);
    }

    public void registerController(Class<?> clazz, Object instance) {
        handlerMapping.registerController(clazz, instance);
    }

    public void start(int port) throws IOException {
        try (var server = new ServerSocket(port)) {
            System.out.println("Autumn REST API running on port " + port);
            while (true) {
                Socket client = server.accept();
                new Thread(() -> handleClient(client)).start();
            }
        }
    }

    private void handleClient(Socket client) {
        try (
                var in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                var out = client.getOutputStream()) {
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isBlank())
                return;

            String[] parts = requestLine.split(" ");
            String method = parts[0];
            String path = parts[1];

            Map<String, String> headers = new HashMap<>();
            String line;
            while (!(line = in.readLine()).isEmpty()) {
                String[] kv = line.split(":", 2);
                if (kv.length == 2)
                    headers.put(kv[0].trim(), kv[1].trim());
            }

            int contentLength = headers.containsKey("Content-Length") ? Integer.parseInt(headers.get("Content-Length"))
                    : 0;
            String body = null;
            if (contentLength > 0) {
                char[] buf = new char[contentLength];
                in.read(buf, 0, contentLength);
                body = new String(buf);
            }

            // Apply filters
            for (Filter filter : filters) {
                if (!filter.doFilter(new HttpRequest(method, path))) {
                    client.close();
                    return;
                }
            }

            // Find handler
            var handler = handlerMapping.getHandler(method, path);
            String responseBody;
            int statusCode = 200;

            if (handler != null) {
                var params = handler.getParameters();
                Object[] args = new Object[params.length];

                for (int i = 0; i < params.length; i++) {
                    var param = params[i];

                    // @RequestBody
                    if (param.isAnnotationPresent(RequestBody.class) && body != null) {
                        for (var converter : converters) {
                            if (converter.supports(headers.getOrDefault("Content-Type", ""))) {
                                args[i] = converter.read(body, param.getType());
                                break;
                            }
                        }
                    }

                    // @RequestParam
                    else if (param.isAnnotationPresent(RequestParam.class)) {
                        var rp = param.getAnnotation(RequestParam.class);
                        String query = path.contains("?") ? path.split("\\?", 2)[1] : "";
                        Map<String, String> queryParams = parseQuery(query);
                        args[i] = queryParams.get(rp.value());
                    }

                    // @PathVariable
                    else if (param.isAnnotationPresent(PathVariable.class)) {
                        String[] segments = path.split("/");
                        args[i] = segments[segments.length - 1];
                    }
                }

                Object result = handler.invoke(args);
                responseBody = result != null ? result.toString() : "";
            } else {
                statusCode = 404;
                responseBody = "Not Found";
            }

            String accept = headers.getOrDefault("Accept", "text/plain");
            for (var converter : converters) {
                if (converter.supports(accept)) {
                    responseBody = converter.write(responseBody);
                    break;
                }
            }

            String response = """
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

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query.isEmpty())
            return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2)
                map.put(kv[0], kv[1]);
        }
        return map;
    }
}
