
/*  Xuan Thinh Le - a1807507
Content Server will send a PUT request that include a feed in a specific format to AS
Note that PUT request is not overwriting feed but saving the feed
*/
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

// Content server whoes will put the file to the aggregation server
public class ContentServerTestingPUTResponse {
    private static long lamportClock = 0;

    public static void main(String[] args) {

        // Checking for stdin input
        if (args.length > 3) {
            System.err
                    .println("Error: ContentServer input via commandline - hostname:portnumber filename running_times");
            System.exit(1);
        }

        // Getting the hostname, port number, and file name
        String fileName = args[1];
        int runningTimes = 1;
        if (args.length == 3) {
            runningTimes = Integer.parseInt(args[2]);
        }
        // Parsing the URL
        URL url = parseURL(args[0]);
        String hostName = url.getHost();
        int portNumber = url.getPort();

        if (portNumber == -1)
            portNumber = 4567; // Default port number

        // Establish socket connection
        try (
                Socket socket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));) {
            persistentConnection(in, out, hostName, fileName, runningTimes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // This method use OutputStream, open the file via InputStream from the folder
    // manualTestingTxtFile and send the PUT request
    public static void sendPutRequest(PrintWriter out, String filename) {
        BufferedReader file = null;
        try {
            // Read the file
            InputStream fstream = ClassLoader.getSystemClassLoader()
                    .getResourceAsStream("automatedTesting/" + filename);

            if (fstream == null)
                throw new FileNotFoundException();

            file = new BufferedReader(new InputStreamReader(fstream));
            String body = file.lines().collect(Collectors.joining("\n"));

            // Send the request with the pre-defined format
            // New update 27/9/2022 - Sending lamport clock
            System.out.println("Sending PUT request");
            out.println("PUT /atom.xml HTTP/1.1\r");
            out.println("User-Agent: ATOMClient/1/0\r");
            out.println("Content-Type: application/atom+xml\r");
            out.println("Content-Length: " + Integer.toString(body.length()) + "\r");
            out.println("Lamport-Clock: " + Long.toString(lamportClock) + "\r\n");
            out.println("<?xml version='1.0' encoding='iso-8859-1' ?>\r");
            out.println("<feed xml:lang=\"en-US\" xmlns=\"http://www.w3.org/2005/Atom\">\r");
            out.println(body);
            out.println("</feed>\r");
            out.flush();
        } catch (FileNotFoundException e) {
            System.err.println("Error while sending PUT request! Can't find file: " + filename);
            e.printStackTrace();
            System.exit(1);
        } finally {
            try {
                if (file != null)
                    file.close();
            } catch (IOException ex) {
                System.err.println("Error: " + ex.getMessage());
            }
        }
    }

    // Method to receive response from server
    // This method appear as a way to maintain persistence connection with the
    // server, for manual testing, user input act as heartbeat
    public static void persistentConnection(BufferedReader in, PrintWriter out, String hostName, String fileName,
            int runningTimes) {

        for (int i = 1; i <= runningTimes; i++) {

            if (out.checkError()) {
                System.out.println("Lost the connection with Aggregation Server! Terminate Content Server.");
                return;
            }

            sendPutRequest(out, fileName);
            try {
                Thread.sleep(800);
                receiveResponse(in);
            } catch (IOException e) {
                System.err.println("Error in receiving respond: " + e.getMessage());
            } catch (InterruptedException et) {
                System.err.println("Error in thread stopping " + et.getMessage());
            }
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

    // Receive the response
    // Print relevant information to stdout
    // Returns false if server wants to end the connection
    public static void receiveResponse(BufferedReader in) throws IOException {
        String line;
        while (in.ready() && (line = in.readLine()) != null) {
            System.out.println(line);
        }
    }
}