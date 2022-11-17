
// Xuan Thinh Le - a1807507
// Client class
// Sends a GET request to the aggregation server
import java.net.*;
import java.util.Scanner;
import java.io.*;

public class GETClientTesting {
    private static long lamportClock = 0;

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Unable to identify port number and host name from input URL");
            System.exit(1);
        }

        // Parsing the URL
        URL url = parseURL(args[0]);
        int runningTimes = Integer.parseInt(args[1]);
        String hostName = url.getHost();
        int portNumber = url.getPort();
        String path = url.getPath();

        if (path.isEmpty()) { // Doesn't provide a path
            path = "/";
        }

        try {
            Socket socket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            persistentConnection(in, out, hostName, path, runningTimes);
        } catch (IOException e) {
            System.err.println("Unable to create a connection to server!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Get information about port and hostname from stdin
    // Format: http://servername.domain.domain:portnumber
    // http://servername:portnumber
    // servername:portnumber
    public static URL parseURL(String url) {
        URL res = null;
        try {
            // Check if http is exist at the start
            if (url.indexOf("http://") != -1) {
                res = new URL(url);
            } else {
                res = new URL("http://" + url);
            }
        } catch (MalformedURLException e) {
            System.err.println("Error GETClient.java: Unable to parse input URL!");
            e.printStackTrace();
        }
        return res;
    }

    // Send the GET request to Aggregation Server
    // Reference:
    // https://www.ibm.com/docs/en/cics-ts/5.3?topic=client-making-get-requests-atom-feeds-collections
    public static void sendGETRequest(PrintWriter out, String hostname, String path) {
        System.out.println("Sending GET request...");
        try {
            out.println("GET " + path + " HTTP/1.1\r");
            out.println("Host: " + hostname + "\r");
            out.println("Lamport-Clock: " + Long.toString(lamportClock) + "\r");
            out.println("\r");
            out.flush();
        } catch (Exception e) {
            System.err.println("Error in sending GET request!");
            e.printStackTrace();
        }
    }

    // Maintaining connection with the server and sending GET request
    public static void persistentConnection(BufferedReader in, PrintWriter out, String hostname, String path,
            int runningTimes) {
        for (int i = 1; i <= runningTimes; i++) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException et) {
                System.err.println("Error in thread stopping " + et.getMessage());
            }
            sendGETRequest(out, hostname, path);
            try {
                Thread.sleep(500);
                receiveResponse(in);
            } catch (IOException e) {
                System.err.println("Error in receiving respond: " + e.getMessage());
            } catch (InterruptedException et) {
                System.err.println("Error in thread stopping " + et.getMessage());
            }
        }
    }

    // method to received response and print out
    public static void receiveResponse(BufferedReader in) throws IOException {
        String line;
        while (in.ready() && (line = in.readLine()) != null) {
            System.out.println(line);
        }
    }
}