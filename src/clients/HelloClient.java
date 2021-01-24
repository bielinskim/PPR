/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clients;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marti
 */
public class HelloClient {

    public static void main(String[] args) {
        try {
            double[] arr = {3.2, 2.5, 1.5}; 
  
        Arrays.sort(arr); 
  
        System.out.printf("Modified arr[] : %s", 
                          Arrays.toString(arr)); 
            
            Scanner scanner = new Scanner(System.in);
            System.out.println("Podaj swoje imię.");
            String name = scanner.nextLine();
            postHelloRequest(name);
        } catch (Exception ex) {
            System.err.println("Wystąpił błąd: "+ex.getLocalizedMessage());
        }
    }

    private static void postHelloRequest(String name) throws Exception {

        String url = "http://localhost:8090/hello";
        URL obj = new URL(url);

        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

        conn.setRequestMethod("POST");

        String urlParameters = "name=" + name;
        String urlParameters2 = "&name2=" + name;

        conn.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.writeBytes(urlParameters);
            wr.writeBytes(urlParameters2);
            wr.flush();
        }

        int responseCode = conn.getResponseCode();
        System.out.println("\nWysyłanie żądania 'POST' pod URL: " + url);
        System.out.println("Parametry Post: " + urlParameters);
        System.out.println("Kod odpowiedzi: " + responseCode);
        System.out.println("Treść odpowiedzi: "
                + "\n====================");

        StringBuilder response;
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            String line;
            response = new StringBuilder();
            while ((line = in.readLine()) != null) {
                response.append(line).append("\r\n");
            }
        }

        //print response
        System.out.println(response.toString());

    }

}
