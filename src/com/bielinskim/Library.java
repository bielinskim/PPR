package com.bielinskim;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Library {

    private static final String BASEDIR = "static";

    private static final int PORT = 8090;

    private HttpServer server;

    ExecutorService httpThreadPool = Executors.newFixedThreadPool(5);

    LocalDateTime startTime;

    Charset utf8 = Charset.forName("UTF-8");

    public List<Book> books;
    public int bookId = 0;

    public static void main(String[] args) throws Exception {
        Library server = new Library();
        server.start();
    }

    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

       server.createContext("/static", new StaticFileHandler(BASEDIR));

        server.setExecutor(this.httpThreadPool);

        books = new ArrayList<>();

       // books.add(new Book(getBookId(), "Mateusz", "Ksiazka1", "19.02.2007", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."));
        //books.add(new Book(getBookId(), "Mateusz", "Ksiazka2", "05.11.2013", "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."));
       // books.add(new Book(getBookId(), "Adam", "Ksiazka3", "20.06.2009", "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur."));

        server.createContext("/", (HttpExchange httpExchange) -> {
            if (httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {

                Map<String, Object> bodyParameters = new HashMap<String, Object>();


                InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String query = br.readLine();
                parseQuery(query, bodyParameters);


                httpExchange.getResponseHeaders().add("Content-Type", "text/html");
                httpExchange.sendResponseHeaders(200, 0);

                try (OutputStream out = httpExchange.getResponseBody()) {

                    out.write("<html><head><meta charset=\"UTF-8\"><title>Lista ksiazek</title></head><body>".getBytes(utf8));
                    out.write("<h1>Lista ksiazek</h1>".getBytes(utf8));


                    if(bodyParameters.get("role").equals("author")) {


                        out.write("<table><tr><th>Autor</th><th>Tytul</th><th>Data</th><th>Edytuj</th><th>Usuń</th><th>Zablokuj</th></tr>".getBytes(utf8));
                        for(int i=0; i<books.size(); i++) {


                            StringBuilder sb = new StringBuilder();
                            String row;

                            if (bodyParameters.get("name").equals(books.get(i).getAuthor())) {

                                row = "<tr><td>"
                                        + books.get(i).getAuthor()
                                        + "</td><td>"
                                        + books.get(i).getTitle()
                                        + "</td><td>"
                                        + books.get(i).getDate()
                                        + "</td>";
                                sb.append(row);

                                if (books.get(i).getUsers().size() == 0 && books.get(i).isBlocked()) {
                                    row = "<td><a href='edit?nr="
                                            + books.get(i).getNr()
                                            + "&name=" + bodyParameters.get("name")
                                            + "'>Edytuj</a></td><td><a href='delete?nr="
                                            + books.get(i).getNr()
                                            + "&name=" + bodyParameters.get("name")
                                            + "'>Usuń</a></td>";

                                    sb.append(row);
                                }
                                if (!books.get(i).isBlocked()) {
                                    row = "<td><a href='block?nr=" +
                                            +books.get(i).getNr()
                                            + "&name=" + bodyParameters.get("name")
                                            + "'>Zablokuj</a></td>";
                                    sb.append(row);
                                }

                                row = "</tr>";
                                sb.append(row);

                                out.write(sb.toString().getBytes(utf8));
                            }



                        }
                        out.write("</table><br>".getBytes());
                        String addLink = "<a href='add?name="
                                + bodyParameters.get("name")
                                +"'>Dodaj</a><br>";
                        out.write(addLink.getBytes(utf8));

                        out.write("<a href='/'>Powrót</a><br>".getBytes(utf8));
                        out.write("</body></html>".getBytes());


                    } else {
//                        String script = "<script> location.replace('/list') </script>";
//                        out.write(script.getBytes());


                        out.write("<form method='POST'>".getBytes(utf8));

                        String form = "<input name='role' type='hidden' value='user'><input name='name' type='hidden' value='"
                                +bodyParameters.get("name")
                                + "'><b>Wyszukaj książkę</b><br><input name='search'>";
                        out.write(form.getBytes(utf8));
                        out.write("<input type='submit' value='Szukaj'>".getBytes(utf8));
                        out.write("</form>\n".getBytes(utf8));

                        boolean searchEnabled = false;
                        String search = "";
                        if(bodyParameters.get("search") != null) {
                            searchEnabled = true;
                            search = (String) bodyParameters.get("search");
                        }


                            out.write("<table><tr><th>Autor</th><th>Tytul</th><th>Data</th><th>Wypożycz</th><th>Oddaj</th></tr>".getBytes(utf8));
                            for(int i=0; i<books.size(); i++) {

                                if((searchEnabled && books.get(i).getTitle().contains((CharSequence) search) || !searchEnabled)) {


                                StringBuilder sb = new StringBuilder();
                                String row;

                                row = "<tr><td>"
                                        + books.get(i).getAuthor()
                                        + "</td><td>"
                                        + books.get(i).getTitle()
                                        + "</td><td>"
                                        + books.get(i).getDate()
                                        + "</td>";
                                sb.append(row);


                                boolean isBorrowedByUser = false;
                                for (int j = 0; j < books.get(i).getUsers().size(); j++) {
                                    if (books.get(i).getUsers().get(j).equals(bodyParameters.get("name"))) {
                                        isBorrowedByUser = true;
                                    }
                                }
                                if (!isBorrowedByUser && !books.get(i).isBlocked()) {
                                    row = "<td><a href='borrow?nr="
                                            + books.get(i).getNr()
                                            + "&name=" + bodyParameters.get("name")
                                            + "'>Wypożycz</a></td>";
                                    sb.append(row);
                                }

                                if (isBorrowedByUser) {
                                    row = "<td><a href='return?nr="
                                            + books.get(i).getNr()
                                            + "&name=" + bodyParameters.get("name")
                                            + "'>Oddaj</a></td>";
                                    sb.append(row);
                                }


                                row = "</tr>";
                                sb.append(row);

                                out.write(sb.toString().getBytes(utf8));
                            }

                            }
                            out.write("</table>".getBytes());
                            out.write("<a href='/'>Powrót</a><br>".getBytes(utf8));
                            out.write("</body></html>".getBytes());
                    }
                }

            } else {
                httpExchange.getResponseHeaders().add("Content-Type", "text/html");
                httpExchange.sendResponseHeaders(200, 0);

                try (OutputStream out = httpExchange.getResponseBody()) {

                    String form = "<b>Wybierz rolę</b><br><input type=\"radio\" name=\"role\" value=\"user\">\n" +
                            "  <label for=\"user\">Czytelnik</label><br>\n" +
                            "  <input type=\"radio\" name=\"role\" value=\"author\">\n" +
                            "  <label for=\"author\">Pisarz</label><br>";

                    out.write("<html><head><meta charset=\"UTF-8\"><title>Strona główna</title></head><body>".getBytes(utf8));
                    out.write("<h1>Strona główna</h1>".getBytes(utf8));
                    out.write("<form method=\"POST\">\n".getBytes(utf8));
                    out.write("<b>Wpisz swoje imię</b><br>\n".getBytes(utf8));
                    out.write("<input name=\"name\"><br>\n".getBytes(utf8));
                    out.write(form.getBytes(utf8));
                    out.write("<input type=\"submit\" value=\"Wyślij\">\n".getBytes(utf8));
                    out.write("</form>\n".getBytes(utf8));

                   // out.write("<a href=\"list\">Lista</a><br>".getBytes(utf8));
                    out.write("</body></html>".getBytes());
                }catch(Exception e)
                {
                    //System.err.println("Błąd przy przetwarzaniu żądania: " + e.getLocalizedMessage());
                }
            }

        });

        server.createContext("/borrow", (HttpExchange httpExchange) -> {

            Map<String, Object> urlParameters = new HashMap<String, Object>();
            String query = query = httpExchange.getRequestURI().getQuery();
            parseQuery(query, urlParameters);

            int nr = Integer.parseInt((String) urlParameters.get("nr"));

            var book = books.stream().filter(bk -> bk.getNr() == nr).findFirst().get();

            book.getUsers().add(urlParameters.get("name"));

            System.out.println("Ksiazka: "+ book.getTitle() + " zostala wypozyczona przez " + urlParameters.get("name"));

            httpExchange.getResponseHeaders().add("Content-Type", "text/html");
            httpExchange.sendResponseHeaders(200, 0);


            try (OutputStream out = httpExchange.getResponseBody()) {

                out.write("<html><head><meta charset='UTF-8'><title>Library</title></head><body>".getBytes(utf8));
                out.write("<a href='/'>Powrót</a><br>".getBytes(utf8));
                out.write("</body></html>".getBytes());

            }catch(Exception e)
            {
                //System.err.println("Błąd przy przetwarzaniu żądania: " + e.getLocalizedMessage());
            }
        });

        server.createContext("/return", (HttpExchange httpExchange) -> {

            Map<String, Object> urlParameters = new HashMap<String, Object>();
            String query = query = httpExchange.getRequestURI().getQuery();
            parseQuery(query, urlParameters);

            int nr = Integer.parseInt((String) urlParameters.get("nr"));

            var book = books.stream().filter(bk -> bk.getNr() == nr).findFirst().get();

            for(int i=0; i<book.getUsers().size(); i++) {
                if(book.getUsers().get(i).equals(urlParameters.get("name"))) {
                    book.getUsers().remove(i);
                }
            }

            System.out.println("Ksiazka: "+ book.getTitle() + " zostala zwrocona przez " + urlParameters.get("name"));

            httpExchange.getResponseHeaders().add("Content-Type", "text/html");
            httpExchange.sendResponseHeaders(200, 0);


            try (OutputStream out = httpExchange.getResponseBody()) {

                out.write("<html><head><meta charset='UTF-8'><title>Library</title></head><body>".getBytes(utf8));
                out.write("<a href='/'>Powrót</a><br>".getBytes(utf8));
                out.write("</body></html>".getBytes());

            }catch(Exception e)
            {
                //System.err.println("Błąd przy przetwarzaniu żądania: " + e.getLocalizedMessage());
            }
        });

        server.createContext("/block", (HttpExchange httpExchange) -> {

            Map<String, Object> urlParameters = new HashMap<String, Object>();
            String query = query = httpExchange.getRequestURI().getQuery();
            parseQuery(query, urlParameters);

            int nr = Integer.parseInt((String) urlParameters.get("nr"));

            var book = books.stream().filter(bk -> bk.getNr() == nr).findFirst().get();

            book.setBlocked(true);

            System.out.println("Ksiazka: "+ book.getTitle() + " zostala zablokowana przez " + urlParameters.get("name"));

            httpExchange.getResponseHeaders().add("Content-Type", "text/html");
            httpExchange.sendResponseHeaders(200, 0);


            try (OutputStream out = httpExchange.getResponseBody()) {

                out.write("<html><head><meta charset='UTF-8'><title>Library</title></head><body>".getBytes(utf8));
                out.write("<a href='/'>Powrót</a><br>".getBytes(utf8));
                out.write("</body></html>".getBytes());

            }catch(Exception e)
            {
                //System.err.println("Błąd przy przetwarzaniu żądania: " + e.getLocalizedMessage());
            }
        });

        server.createContext("/report", (HttpExchange httpExchange) -> {
            httpExchange.getResponseHeaders().add("Content-Type", "text/html");
            httpExchange.sendResponseHeaders(200, 0);


            try (OutputStream out = httpExchange.getResponseBody()) {

                out.write("<table><tr><th>Autor</th><th>Tytul</th><th>Data</th><th>Zablokowana</th><th>Uzytkownicy</th></tr>".getBytes(utf8));
                for(int i=0; i<books.size(); i++) {

                    StringBuilder sb = new StringBuilder();
                    String row;

                        row = "<tr><td>"
                                + books.get(i).getAuthor()
                                + "</td><td>"
                                + books.get(i).getTitle()
                                + "</td><td>"
                                + books.get(i).getDate()
                                + "</td><td>"
                                + books.get(i).isBlocked()
                                + "</td>";

                        sb.append(row);

                    for(int j=0; j<books.get(i).getUsers().size(); j++) {
                       row = "<td>" + books.get(i).getUsers().get(j) + "</td>";
                       sb.append(row);
                    }

                        row = "</tr>";
                        sb.append(row);

                        out.write(sb.toString().getBytes(utf8));


                }

            }catch(Exception e)
            {
                //System.err.println("Błąd przy przetwarzaniu żądania: " + e.getLocalizedMessage());
            }
        });

        server.createContext("/edit", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                if (httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    Map<String, Object> bodyParameters = new HashMap<String, Object>();
                    Map<String, Object> urlParameters = new HashMap<String, Object>();

                    InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
                    BufferedReader br = new BufferedReader(isr);
                    String query = br.readLine();
                    parseQuery(query, bodyParameters);

                    query = httpExchange.getRequestURI().getQuery();
                    parseQuery(query, urlParameters);
                    int nr = Integer.parseInt((String) urlParameters.get("nr"));

                    var book = books.stream().filter(bk -> bk.getNr() == nr).findFirst().get();
                    book.setTitle((String) bodyParameters.get("title"));
                    book.setDate((String) bodyParameters.get("date"));
                    book.setContent((String) bodyParameters.get("content"));
                    book.setBlocked(false);

                    System.out.println("Ksiazka: "+ book.getTitle() + " zostala zedytowana przez " + urlParameters.get("name"));

                    httpExchange.getResponseHeaders().add("Content-Type", "text/html");
                    httpExchange.sendResponseHeaders(200, 0);

                    try (OutputStream out = httpExchange.getResponseBody()) {

                        out.write("<html><head><meta charset='UTF-8'><title>Library</title></head><body>".getBytes(utf8));
                        out.write("<a href='/'>Powrót</a><br>".getBytes(utf8));
                        out.write("</body></html>".getBytes());

                    }
                } else {
                    httpExchange.getResponseHeaders().add("Content-Type", "text/html");
                    httpExchange.sendResponseHeaders(200, 0);

                    Map<String, Object> urlParameters = new HashMap<String, Object>();
                    String query = httpExchange.getRequestURI().getQuery();
                    parseQuery(query, urlParameters);
                    int nr = Integer.parseInt((String) urlParameters.get("nr"));

                    var book = books.stream().filter(bk -> bk.getNr() == nr).findFirst().get();


                    try (OutputStream out = httpExchange.getResponseBody()) {

                        String form = "<b>Tytuł</b><br><input name='title' value='"
                                + book.getTitle()
                                +"'><br><b>Data</b><br><input name='date' value='"
                                + book.getDate()
                                +"'><br><b>Treść</b><br><input name='content' value='"
                                + book.getContent()
                                +"'><br>";

                        out.write("<html><head><meta charset='UTF-8'><title>Strona powitalna</title></head><body>".getBytes(utf8));
                        out.write("<h1>Edytuj książkę</h1>".getBytes(utf8));
                        out.write("<form method='POST'>".getBytes(utf8));
                        out.write(form.getBytes(utf8));
                        out.write("<input type='submit' value='Wyślij'>".getBytes(utf8));
                        out.write("</form>\n".getBytes(utf8));
                        out.write("</body></html>".getBytes());
                    } catch (Exception e) {
                        //System.err.println("Błąd przy przetwarzaniu żądania: " + e.getLocalizedMessage());
                    }
                }
            }
        });

        server.createContext("/add", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                if (httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    Map<String, Object> bodyParameters = new HashMap<String, Object>();
                    Map<String, Object> urlParameters = new HashMap<String, Object>();

                    InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
                    BufferedReader br = new BufferedReader(isr);
                    String query = br.readLine();
                    parseQuery(query, bodyParameters);

                    query = httpExchange.getRequestURI().getQuery();
                    parseQuery(query, urlParameters);

                    var book = new Book();
                    book.setAuthor((String) bodyParameters.get("author"));
                    book.setTitle((String) bodyParameters.get("title"));
                    book.setDate((String) bodyParameters.get("date"));
                    book.setContent((String) bodyParameters.get("content"));
                    book.setBlocked(false);

                    books.add(book);

                    System.out.println("Ksiazka: "+ book.getTitle() + " zostala dodana przez " + urlParameters.get("name"));

                    httpExchange.getResponseHeaders().add("Content-Type", "text/html");
                    httpExchange.sendResponseHeaders(200, 0);

                    try (OutputStream out = httpExchange.getResponseBody()) {

                        out.write("<html><head><meta charset='UTF-8'><title>Library</title></head><body>".getBytes(utf8));
                        out.write("<a href='/'>Powrót</a><br>".getBytes(utf8));
                        out.write("</body></html>".getBytes());

                    }
                } else {
                    httpExchange.getResponseHeaders().add("Content-Type", "text/html");
                    httpExchange.sendResponseHeaders(200, 0);

                    Map<String, Object> urlParameters = new HashMap<String, Object>();
                    String query = httpExchange.getRequestURI().getQuery();
                    parseQuery(query, urlParameters);


                    try (OutputStream out = httpExchange.getResponseBody()) {

                        String form = "<input name='author' type='hidden' value='"
                                +urlParameters.get("name")
                                +"'><br><b>Tytuł</b><br><input name='title'><br><b>Data</b><br><input name='date'><br><b>Treść</b><br><input name='content'><br>";

                        out.write("<html><head><meta charset='UTF-8'><title>Library</title></head><body>".getBytes(utf8));
                        out.write("<h1>Dodaj książkę</h1>".getBytes(utf8));
                        out.write("<form method='POST'>".getBytes(utf8));
                        out.write(form.getBytes(utf8));
                        out.write("<input type='submit' value='Wyślij'>".getBytes(utf8));
                        out.write("</form>\n".getBytes(utf8));
                        out.write("</body></html>".getBytes());
                    } catch (Exception e) {
                        //System.err.println("Błąd przy przetwarzaniu żądania: " + e.getLocalizedMessage());
                    }
                }
            }
        });

        server.createContext("/delete", (HttpExchange httpExchange) -> {

            Map<String, Object> urlParameters = new HashMap<String, Object>();
            String query = query = httpExchange.getRequestURI().getQuery();
            parseQuery(query, urlParameters);

            int nr = Integer.parseInt((String) urlParameters.get("nr"));

            var book = books.stream().filter(bk -> bk.getNr() == nr).findFirst().get();

            books.remove(book);

            System.out.println("Ksiazka: "+ book.getTitle() + " zostala usunieta przez " + urlParameters.get("name"));

            httpExchange.getResponseHeaders().add("Content-Type", "text/html");
            httpExchange.sendResponseHeaders(200, 0);


            try (OutputStream out = httpExchange.getResponseBody()) {

                out.write("<html><head><meta charset='UTF-8'><title>Library</title></head><body>".getBytes(utf8));
                out.write("<a href='/'>Powrót</a><br>".getBytes(utf8));
                out.write("</body></html>".getBytes());

            }catch(Exception e)
            {
                //System.err.println("Błąd przy przetwarzaniu żądania: " + e.getLocalizedMessage());
            }
        });


        server.createContext("/shutdown", (httpExchange) -> {
            httpExchange.getResponseHeaders().add("Content-Type", "text/plain");

            httpExchange.sendResponseHeaders(200, 0);

            try (OutputStream out = httpExchange.getResponseBody()) {

                out.write(("Zamykanie serwera!").getBytes(utf8));

            }

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

    public int getBookId() {
        return bookId++;
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
