/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import servers.handlers.StaticFileHandler;

/**
 *
 * @author Marcin
 */
public class Server {

    private static final String BASEDIR = "static";

    private static final int PORT = 8090;
    private static final int PORT2 = 8091;
    private static final int PORT3 = 8092;
    

    private HttpServer server;
    
    ExecutorService httpThreadPool = Executors.newFixedThreadPool(5);
    
    LocalDateTime startTime;
    
    Charset utf8 = Charset.forName("UTF-8");

    public static void main(String[] args) throws Exception {
        Server server1 = new Server();
        Server server2 = new Server();
        Server server3 = new Server();
        server1.start(PORT);
        server2.start(PORT2);
        server3.start(PORT3);
    }

    void start(int PORT) throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/static", new StaticFileHandler(BASEDIR));
        
        server.setExecutor(this.httpThreadPool);
        
        
        // Zad 7
        server.createContext("/zad7", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                //System.out.println("handling hello page");
                if (httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    // parse request
                    Map<String, Object> parameters = new HashMap<String, Object>();
                    InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
                    BufferedReader br = new BufferedReader(isr);
                    String query = br.readLine();
                    parseQuery(query, parameters);
                    httpExchange.getResponseHeaders().add("Content-Type", "text/plain");

                    httpExchange.sendResponseHeaders(200, 0);
                    
                    List<String> list = new ArrayList<String>();
                    
                    int n=0;
                    while(parameters.get("number"+n+"") != null) {
                        list.add(parameters.get("number"+n+"").toString());
                        n++;
                    }
                    
                    List<Integer> numbers = new ArrayList<Integer>();
                    for(int i=0; i<list.size(); i++) {
                            numbers.add(Integer.parseInt(list.get(i)));
                        }
                    
 
                    Collections.sort(numbers); 

                    try (OutputStream out = httpExchange.getResponseBody()) {
                        
                        for(int i=0; i<numbers.size(); i++) {
                            out.write((numbers.get(i)+"\n").getBytes(utf8));
                        }
                    } 
                }
            }
        });
        
        server.createContext("/shutdown", (httpExchange) -> {
            httpExchange.getResponseHeaders().add("Content-Type", "text/plain");

            httpExchange.sendResponseHeaders(200, 0);

            try (OutputStream out = httpExchange.getResponseBody()) {

                out.write(("Zamykanie serwera!").getBytes(utf8));

            }
            
            //tutaj zamykamy serwer
        });
        
        server.start();
        startTime = LocalDateTime.now();
        System.out.println("Server listening on port: " + PORT);
    }

    public void stop() {
        server.stop(0);
        httpThreadPool.shutdown(); 
        try { httpThreadPool.awaitTermination(10, TimeUnit.SECONDS); } 
        catch (InterruptedException e) { System.err.println("Unable to shutdown! "+e.getLocalizedMessage()); }
        System.out.println("Server stopped");
    }

    public static void parseQuery(String query, Map<String, Object> parameters) 
            throws UnsupportedEncodingException {

        if (query != null) {
            String pairs[] = query.split("[&]");
            for (String pair : pairs) {
                String param[] = pair.split("[=]");
                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0],
                            System.getProperty("file.encoding"));
                }

                if (param.length > 1) {
                    value = URLDecoder.decode(param[1],
                            System.getProperty("file.encoding"));
                }

                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    if (obj instanceof List<?>) {
                        List<String> values = (List<String>) obj;
                        values.add(value);

                    } else if (obj instanceof String) {
                        List<String> values = new ArrayList<>();
                        values.add((String) obj);
                        values.add(value);
                        parameters.put(key, values);
                    }
                } else {
                    parameters.put(key, value);
                }
            }
        }
    }

}
