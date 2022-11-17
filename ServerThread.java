import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.xml.sax.SAXException;

public class ServerThread extends Thread {
    private Socket socket;
    private String[] requestLine;
    private Database database;
    private int portNumber;
    private LamportClock lamportClock;

    // Constructor
    public ServerThread(Socket socket, Database database, int portNumber, LamportClock lamportClock) {
        try {
            socket.getKeepAlive(); // Check Keep Alive or throw SOCKET error
            socket.setSoTimeout(12000); // Set the timeout for the socket at 12 seconds
            System.out.println("Aggregation Server receive connection at: " + socket.getRemoteSocketAddress());
        } catch (SocketException e) {
            System.err.println("SERVER ERROR! SOCKET TIMES OUT - CAN'T KEEP CONNECTION AT ADDRESS "
                    + socket.getRemoteSocketAddress().toString());
            e.printStackTrace();
        }

        this.socket = socket;
        this.database = database;
        this.portNumber = portNumber;
        this.lamportClock = lamportClock;
    }

    // Start the thread
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream());) {
            boolean terminate = false;
            while (!terminate) {
                try {
                    boolean isAlive = parseRequest(in); // Read in the request line and analyze it

                    if (!isAlive) {
                        System.out.println("SERVER LOST CONNECTION WITH CLIENT/CONTENT SERVER AT ADDRESS "
                                + this.socket.getRemoteSocketAddress().toString());
                        try {
                            this.database.deleteFeeds(socket.getRemoteSocketAddress().toString());
                            System.out.println("Removing information from database");
                        } catch (SQLException eq) {
                            System.err.println("Unable to remove feeds " + eq.getMessage());
                        }
                        break;
                    }

                    if (this.requestLine.length != 3) {
                        continue;
                    }
                    analyzeRespond(in, out);

                } catch (SocketTimeoutException se) {
                    System.err.println("SERVER ERROR! TIMEOUT - NO REQUEST FROM CONTENT SERVER AT ADDRESS "
                            + this.socket.getRemoteSocketAddress().toString() + " FOR THE PASS 12 SECONDS ");
                    System.err.println("CLOSING CONNECTION.");
                    try {
                        this.database.deleteFeeds(socket.getRemoteSocketAddress().toString());
                        System.out.println("Removing feed from database");
                    } catch (SQLException eq) {
                        System.err.println("Unable to remove feeds " + eq.getMessage());
                    }
                    terminate = true;
                } catch (IOException | SAXException e) {
                    System.err.println("SERVER ERROR! MESSAGES: " + e.getMessage());
                    terminate = true;
                    this.lamportClock.increment();
                }
            }
            socket.close();
        } catch (IOException e) {
            System.err.println("SERVER ERROR! UNABLE TO READ AND RESPONSE TO REQUEST - MESSAGE: " + e.getMessage());
        }

    }

    // Get the request type by reading the first line of InputStream
    // Return false if does not receive any request type, else split the request
    // line into 3 pieces
    private boolean parseRequest(BufferedReader in) throws IOException {
        String request = in.readLine();
        if (request == null) {
            return false;
        }
        this.requestLine = request.split(" ");
        return true;
    }

    // Turn the headers field into a HashMap for further process by reading the
    // InputStream
    private HashMap<String, String> parseHeader(BufferedReader in) throws IOException {
        HashMap<String, String> mp = new HashMap<String, String>();
        String inputLine;
        while ((inputLine = in.readLine()) != null && !inputLine.isEmpty()) {
            int delim = inputLine.indexOf(": ");
            if (delim != -1) { // If delimeter exist at the first catch, then put it into the map
                mp.put(inputLine.substring(0, delim), inputLine.substring(delim + 2));
            }
        }
        return mp;
    }

    // Return a string of the body (the file content) with the given body length
    private String parseBody(BufferedReader in, int bodyLength) throws IOException {
        // Skip the two feed headers line
        in.readLine();
        in.readLine();

        // Reading the body characters by characters
        char[] bodyChars = new char[bodyLength];
        in.read(bodyChars, 0, bodyLength);

        String body = new String(bodyChars);
        return body;
    }

    // Process to handling PUT and GET request and print the response to
    // corresponding connection
    private void analyzeRespond(BufferedReader in, PrintWriter out) throws IOException, SAXException {
        // CODE 400 for invalid request
        if (!this.requestLine[0].equals("GET") && !this.requestLine[0].equals("PUT")) {
            System.err.println("Unrecognize request " + this.requestLine[0] + "- terminate connection");
            printResponse(400, "", out);
            this.lamportClock.increment();
            return;
        }

        if (this.requestLine[0].equals("PUT")) {
            handlePUT(in, out);
        } else if (this.requestLine[0].equals("GET")) { // If come from client, set timeout to 30 seconds
            this.socket.setSoTimeout(30000);
            handleGET(in, out);
        }
    }

    // Method to extract body for PUT request - return an array consist of entries
    private String[] extractFeeds(String body) {
        String[] feeds = body.split("\nentry\n");
        return feeds;
    }

    // Return a HashMap from feed, mapping atom elements to their content
    private HashMap<String, String> toHashMap(String feed) {
        HashMap<String, String> map = new HashMap<String, String>();
        String[] feedLine = feed.split("\n");

        for (int i = 0; i < feedLine.length; i++) {
            if (isAtomElement(feedLine[i])) {
                int delim = feedLine[i].indexOf(":");
                if (delim == -1)
                    continue;
                String atomElement = feedLine[i].substring(0, delim);

                if (atomElement.contains("summary")) { // Summary is at the end of feed
                    String summary = feedLine[i].substring(delim + 1);
                    for (int j = i + 1; j < feedLine.length; j++) { // Add all the line
                        summary += feedLine[j];
                    }
                    map.put(atomElement, summary);
                    break;
                } else if (map.containsKey(atomElement)) { // Found duplicated atom field
                    map.put("duplicated", feedLine[i].substring(delim + 1));
                } else { // Other atoms element
                    map.put(atomElement, feedLine[i].substring(delim + 1));
                }
            } else { // Error: Unrecognized atom element
                System.err.println("FEED ERROR! UNRECOGNIZED ATOM ELEMENT " + feedLine[i]);
                break;
            }
        }
        return map;
    }

    // Check if either the element is an atom element or not
    private boolean isAtomElement(String element) {
        if (element.contains("title") || element.contains("subtitle") || element.contains("link")
                || element.contains("updated")
                || element.contains("author") || element.contains("name") || element.contains("id")
                || element.contains("summary") || element.contains("entry"))
            return true;
        return false;
    }

    // Receive the status code, the feed body, and output stream, send this to the
    // connected content server/client
    private void printResponse(int statusCode, String body, PrintWriter out) throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        out.print("\nHTTP/1.1 " + String.valueOf(statusCode) + " " + getStatusMessage(statusCode) + "\r\n");
        out.print("Date: " + formatter.format(date) + "\r\n");
        out.print("Content-Length: " + String.valueOf(body.length()) + "\r\n");
        out.print("Content-Type: application/atom+xml\r\n");
        out.print("Connection: keep-alive\r\n");
        out.print("\r\n");
        out.flush();
    }

    // Return message for status code
    private String getStatusMessage(int statusCode) {
        switch (statusCode) {
            case 201:
                return "HTTP_CREATED";
            case 200:
                return "OK";
            case 400:
                return "BAD REQUEST";
            case 204:
                return "NO CONTENT";
            case 500:
                return "INTERNAL SERVER ERROR";
            case 404: // Only for GET client, when the database have no content
                return "NOT FOUND";
            default:
                return "UNRECOGNIZED STATUS CODE";
        }
    }

    // Print the response for the Content Server and process the feed in database
    private void handlePUT(BufferedReader in, PrintWriter out) throws IOException {
        int feedNumber = 0;
        int isInsert = 0;
        // Get the headers and the body
        HashMap<String, String> headers = parseHeader(in);
        String body = parseBody(in, Integer.parseInt(headers.get("Content-Length")));

        // The file has no content - CODE 204
        if (body.isEmpty()) {
            System.out.println("REJECTED FEED! NO CONTENT");
            printResponse(204, "", out);
            this.lamportClock.increment();
            return;
        }

        // Extract the body into feeds by "entry"
        String[] feeds = extractFeeds(body);

        // Check if the server exist 20 feeds - if yes, remove a feed for insertion
        try {
            feedNumber = this.database.getFeedNumber(this.socket.getRemoteSocketAddress().toString());
            while (feedNumber >= 20) {
                this.database.deleteOldestFeed();
            }
        } catch (SQLException e) {
            System.err.println("SQL ERROR WHEN GETTING FEED NUMBER: " + e.getMessage());
        }

        // Insert or update feeds into database - Critical section, starvation may start
        // -> have to synchronize the threads
        synchronized (this.lamportClock) { // Lock the thread until it get it's chance to execute - making sure only 1
                                           // thread will execute at the given time
            String lpClock = headers.get("Lamport-Clock");
            lamportClock.maxIncrease(Long.parseLong(lpClock));

            for (int i = 0; i < feeds.length; i++) {
                HashMap<String, String> feedItem = toHashMap(feeds[i]);
                // Invalid feed cases
                if (feedItem.get("title").isEmpty() || feedItem.get("link").isEmpty() || feedItem.get("id").isEmpty()
                        || !feedItem.containsKey("title") || !feedItem.containsKey("link")
                        || !feedItem.containsKey("id")) {
                    System.err.println("FEED ERROR! REJECTED FEED WITHOUT TITLE/LINK/ID");
                    printResponse(500, "", out);
                    this.lamportClock.increment();
                    return;
                }

                // Duplicated cases
                if (feedItem.containsKey("duplicated")) {
                    System.err.println("FEED ERROR! REJECTED FEED WITH DUPLICATED ATOM ELEMENTS");
                    printResponse(500, "", out);
                    this.lamportClock.increment();
                    return;
                }
            }

            // Insert each entry into database
            for (int i = 0; i < feeds.length; i++) {
                HashMap<String, String> feedItem = toHashMap(feeds[i]);
                String feedMaxDate = feedItem.get("updated");

                try {
                    isInsert = database.saveFeed(feedMaxDate, feeds[i] + "\n\n",
                            this.socket.getRemoteSocketAddress().toString(),
                            feedItem.get("link"), this.portNumber);
                } catch (SQLException e) {
                    System.err.println("DATABASE ERROR! UNABLE TO COMPLETE PUT REQUEST - MESSAGE: " + e.getMessage());
                    this.lamportClock.increment();
                }
            }

            // Feed did not exist in database - Content Server first connection CODE 201
            if (isInsert == 1) {
                System.out.println("Connected to database - insert feed.");
                printResponse(201, body, out);
                this.lamportClock.increment();
            } else { // Later upload - CODE 200
                System.out.println("Database connected to port " + this.portNumber + " was updated.");
                printResponse(200, body, out);
                this.lamportClock.increment();
            }

        }
    }

    // Print the response for Client by retrieving the feeds and convert to xml
    private void handleGET(BufferedReader in, PrintWriter out) throws IOException, SAXException {
        HashMap<String, String> headers = parseHeader(in);
        synchronized (this.lamportClock) { // Lock the thread for critical section
            String lpClock = headers.get("Lamport-Clock");
            lamportClock.maxIncrease(Long.parseLong(lpClock));
            try {
                String[] xmlArray = this.database.getFeed();
                if (xmlArray.length == 0) {
                    printResponse(404, "", out);
                    out.println("\r");
                    this.lamportClock.increment();
                } else {
                    TextParser parser = new TextParser(xmlArray);
                    parser.parsingXML();

                    XMLParser xmlParser = new XMLParser(parser.getXML());
                    xmlParser.parseAtom();

                    out.println(xmlParser.getFeed());
                    out.println("\r");
                    this.lamportClock.increment();
                }
                out.flush();
            } catch (SQLException es) {
                System.err.println("ERROR IN GETTING FEED FOR GET REQUEST: " + es.getMessage());
                this.lamportClock.increment();
            }
        }
    }
}