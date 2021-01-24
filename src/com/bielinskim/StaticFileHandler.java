package com.bielinskim;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;


public class StaticFileHandler implements HttpHandler {

    private final String baseDir;

    public StaticFileHandler(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        URI uri = ex.getRequestURI();
        String name = new File(uri.getPath()).getName();
        if(name.startsWith(ex.getHttpContext().getPath()))
            name = name.substring(ex.getHttpContext().getPath().length());
        File path = new File(baseDir, name);

        System.err.println("Returning File: "+path.getAbsolutePath());
        Headers h = ex.getResponseHeaders();
        String filename = path.getName();
        if(filename.endsWith(".html")||filename.endsWith(".htm"))
            h.add("Content-Type", "text/html");
        else
            h.add("Content-Type", "text/plain");

        try (OutputStream out = ex.getResponseBody()) {
            if (path.exists()) {
                ex.sendResponseHeaders(200, path.length());
                out.write(Files.readAllBytes(path.toPath()));
            } else {
                System.err.println("File not found: " + path.getAbsolutePath());

                ex.sendResponseHeaders(404, 0);
                out.write("404 File not found.".getBytes());
            }
        }
    }

}
