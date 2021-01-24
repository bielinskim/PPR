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
public class Zad6 {

    public static void main(String[] args) {
        try {
            double[] arr = new double[5];
  
       // Arrays.sort(arr); 
  
//        System.out.printf("Modified arr[] : %s", 
//                          Arrays.toString(arr)); 
            
            Scanner scanner = new Scanner(System.in);
            
            System.out.println("Podaj 1 liczbe");
            arr[0] = scanner.nextDouble();
            System.out.println("Podaj 2 liczbe");
            arr[1] = scanner.nextDouble();
            System.out.println("Podaj 3 liczbe");
            arr[2] = scanner.nextDouble();
            System.out.println("Podaj 4 liczbe");
            arr[3] = scanner.nextDouble();
            System.out.println("Podaj 5 liczbe");
            arr[4] = scanner.nextDouble();
            
            postHelloRequest(arr);
        } catch (Exception ex) {
            System.err.println("Wystąpił błąd: "+ex.getLocalizedMessage());
        }
    }

    private static void postHelloRequest(double[] arr) throws Exception {

        String url = "http://localhost:8090/zad6";
        URL obj = new URL(url);

        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

        conn.setRequestMethod("POST");
        
        

        String urlParameters1 = "number1=" + arr[0];
        String urlParameters2 = "&number2=" + arr[1];
        String urlParameters3 = "&number3=" + arr[2];
        String urlParameters4 = "&number4=" + arr[3];
        String urlParameters5 = "&number5=" + arr[4];

        conn.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.writeBytes(urlParameters1);
            wr.writeBytes(urlParameters2);
            wr.writeBytes(urlParameters3);
            wr.writeBytes(urlParameters4);
            wr.writeBytes(urlParameters5);
            wr.flush();
        }

        int responseCode = conn.getResponseCode();
        System.out.println("\nWysyłanie żądania 'POST' pod URL: " + url);
        System.out.println("Parametry Post: " + urlParameters1+urlParameters2+urlParameters3+urlParameters4+urlParameters5);
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
