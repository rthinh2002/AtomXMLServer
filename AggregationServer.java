
// Xuan Thinh Le - a1807507
// Main communicating server between clients and content server
// AS will send a response for GET request and PUT request
// as well as perform the pre-determine operation to these requests
// AS will have to handle error and fault tolerance cases
import java.net.*;
import java.io.*;

public class AggregationServer {

    private static LamportClock lamportClock = new LamportClock((long) 0); // Every process have timestamp start at 0

    public static void main(String[] args) throws IOException {

        // Initialize portnumber as 4567
        int portNumber = 4567;

        // If port number is given in the stdin, use that port number
        if (args.length == 1) {
            portNumber = Integer.parseInt(args[0]);
        } else if (args.length > 1) { // Port number error handling
            System.err.println("Error: Too many parameters.");
            System.err.println("Error: Enter a port number for AggregationServer! 4567 is the default port number.");
            System.exit(1);
        }

        System.out.println("Aggregation Server connecting to port: " + portNumber);

        // Establish database connection
        Database db = new Database();

        // Start the delete thread to delete from database every 12 seconds
        new DatabaseDeleteThread(db).start();

        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            // Start a thread for responding for each client
            while (true) {
                new ServerThread(serverSocket.accept(), db, portNumber, lamportClock).start();
            }
        } catch (IOException e) {
            System.err.println("SERVER ERROR! UNABLE TO PROCESS");
            e.printStackTrace();
        }
    }
}
