import java.sql.SQLException;

public class DatabaseDeleteThread extends Thread {
    public Database db;

    public DatabaseDeleteThread(Database db) {
        this.db = db;
    }

    // This thread will run independently to invoke the database and delete the
    // thread that last 12 seconds
    public void run() {
        while (true) {
            try { // Pause the thread for other thread to invoke in the database
                Thread.sleep(800);
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted " + e.getMessage());
            }

            // Call the database method to delete feeds every 12 seconds
            try {
                db.delete12SecondFeed();
            } catch (SQLException e) {
                System.err.println("ERROR IN DELETE THREAD");
                e.printStackTrace();
            }
        }
    }
}
