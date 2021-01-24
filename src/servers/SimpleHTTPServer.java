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
public class SimpleHTTPServer {

    private static final String BASEDIR = "static";

    private static final int PORT = 8090;

    private HttpServer server;
    
    ExecutorService httpThreadPool = Executors.newFixedThreadPool(5);
    
    LocalDateTime startTime;
    
    Charset utf8 = Charset.forName("UTF-8");

    public static void main(String[] args) throws Exception {
        SimpleHTTPServer server = new SimpleHTTPServer();
        server.start();
    }

    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/static", new StaticFileHandler(BASEDIR));
        
        server.setExecutor(this.httpThreadPool);
        
        //Main page handling
        server.createContext("/", (HttpExchange httpExchange) -> {
            System.out.println("handling main page");
            httpExchange.getResponseHeaders().add("Content-Type", "text/html");
        
            httpExchange.sendResponseHeaders(200, 0);
            
            try (OutputStream out = httpExchange.getResponseBody()) {

                out.write("<html><head><meta charset=\"UTF-8\"><title>Strona główna</title></head><body>".getBytes(utf8));
                out.write("<h1>Strona główna</h1>".getBytes(utf8));
                out.write("<b>Serwer działa od </b>".getBytes(utf8));
                Duration sot = Duration.between(LocalDateTime.now(), startTime);
                long absSeconds = Math.abs(sot.getSeconds());
                out.write(String.format(
                    "%d godzin, %02d minut, %02d sekund <br>",
                    absSeconds / 3600,
                    (absSeconds % 3600) / 60,
                    absSeconds % 60).getBytes());
                out.write("<a href=\"static\\date.html\">Strona HTML</a><br>".getBytes(utf8));
                out.write("<a href=\"static\\test.txt\">Plik tekstowy</a><br>".getBytes(utf8));
                out.write("<a href=\"zad5\">Zad 5</a><br>".getBytes(utf8));
                out.write("</body></html>".getBytes());
            }catch(Exception e)
            {
                System.err.println("Błąd przy przetwarzaniu żądania: " + e.getLocalizedMessage());
            }
        });
        
        //Hello page handler
        server.createContext("/hello", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                System.out.println("handling hello page");
                if (httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    // parse request
                    Map<String, Object> parameters = new HashMap<String, Object>();
                    InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
                    BufferedReader br = new BufferedReader(isr);
                    String query = br.readLine();
                    parseQuery(query, parameters);
                    httpExchange.getResponseHeaders().add("Content-Type", "text/plain");

                    httpExchange.sendResponseHeaders(200, 0);

                    try (OutputStream out = httpExchange.getResponseBody()) {

                        out.write(("Witaj "+parameters.get("name")+parameters.get("name2")+"!").getBytes(utf8));
                        
                    } 
                } else {
                    httpExchange.getResponseHeaders().add("Content-Type", "text/html");

                    httpExchange.sendResponseHeaders(200, 0);

                    try (OutputStream out = httpExchange.getResponseBody()) {

                        out.write("<html><head><meta charset=\"UTF-8\"><title>Strona powitalna</title></head><body>".getBytes(utf8));
                        out.write("<h1>Strona powitalna</h1>".getBytes(utf8));
                        out.write("<b>Wpisz swoje imię</b><br>\n".getBytes(utf8));
                        out.write("<form method=\"POST\">\n".getBytes(utf8));
                        out.write("<input name=\"name\"><br>\n".getBytes(utf8));
                        out.write("<input type=\"submit\" value=\"Wyślij\">\n".getBytes(utf8));
                        out.write("</form>\n".getBytes(utf8));
                        out.write("</body></html>".getBytes());
                    } catch (Exception e) {
                        System.err.println("Błąd przy przetwarzaniu żądania: " + e.getLocalizedMessage());
                    }
                }
            }
        });
        
        // Zad 5
        server.createContext("/zad5", new HttpHandler() {
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
                    
                    
                    System.out.println(parameters.get("name1"));
                    String[] arrayOfStrings = new String[5];
                    arrayOfStrings[0] = parameters.get("name1").toString();
                    arrayOfStrings[1] = parameters.get("name2").toString();
                    arrayOfStrings[2] = parameters.get("name3").toString();
                    arrayOfStrings[3] = parameters.get("name4").toString();
                    arrayOfStrings[4] = parameters.get("name5").toString();
                    
                    Arrays.sort(arrayOfStrings);
                    

                    try (OutputStream out = httpExchange.getResponseBody()) {
                        
                        for(int i=0; i<arrayOfStrings.length; i++) {
                            out.write((arrayOfStrings[i]+"\n").getBytes(utf8));
                        }
                    } 
                } else {
                    httpExchange.getResponseHeaders().add("Content-Type", "text/html");

                    httpExchange.sendResponseHeaders(200, 0);

                    try (OutputStream out = httpExchange.getResponseBody()) {

                        out.write("<html><head><meta charset=\"UTF-8\"><title>Zad 5</title></head><body>".getBytes(utf8));
                        out.write("<h1>Zad 5</h1>".getBytes(utf8));
                        out.write("<b>Wpisz stringi</b><br>\n".getBytes(utf8));
                        out.write("<form method=\"POST\">\n".getBytes(utf8));
                        out.write("<input name=\"name1\"><br>\n".getBytes(utf8));
                        out.write("<input name=\"name2\"><br>\n".getBytes(utf8));
                        out.write("<input name=\"name3\"><br>\n".getBytes(utf8));
                        out.write("<input name=\"name4\"><br>\n".getBytes(utf8));
                        out.write("<input name=\"name5\"><br>\n".getBytes(utf8));
                        out.write("<input type=\"submit\" value=\"Wyślij\">\n".getBytes(utf8));
                        out.write("</form>\n".getBytes(utf8));
                        out.write("</body></html>".getBytes());
                    } catch (Exception e) {
                        System.err.println("Błąd przy przetwarzaniu żądania: " + e.getLocalizedMessage());
                    }
                }
            }
        });
        
                // Zad 5
        server.createContext("/zad6", new HttpHandler() {
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
                    
                    
                    System.out.println(parameters.get("name1"));
                    double[] array = new double[5];
                    array[0] = Double.parseDouble(parameters.get("number1").toString());
                    array[1] = Double.parseDouble(parameters.get("number2").toString());
                    array[2] = Double.parseDouble(parameters.get("number3").toString());
                    array[3] = Double.parseDouble(parameters.get("number4").toString());
                    array[4] = Double.parseDouble(parameters.get("number5").toString());
                    
                    Arrays.sort(array);
                    

                    try (OutputStream out = httpExchange.getResponseBody()) {
                        
                        for(int i=0; i<array.length; i++) {
                            out.write((array[i]+"\n").getBytes(utf8));
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
