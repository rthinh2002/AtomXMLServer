
// This is the class for establish the database to manage feed information from the content server
import java.io.File;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Database {
    // Establish connection to database
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection("jdbc:sqlite:" +
                new File("database.db")
                        .getAbsolutePath()
                        .toString());
    }

    // Insert feed into the database
    // The content server may try to send more than 1
    // PUT request of the same content
    // so if we keep inserting, it's will not be efficient, instead, I update the
    // database rather than insert
    public int saveFeed(String updated, String body, String address, String link, int port) throws SQLException {
        // Process to check if feed from the specific content server exist in database
        Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM feeds WHERE cs_address=? AND link=?");

        ps.setString(1, address);
        ps.setString(2, link);
        ResultSet rs = ps.executeQuery();

        // Get the saved time
        long currentTime = new Date().getTime();

        if (rs.next()) { // If exist, then it is not the first connection
            updateDatabase(body, updated, address, link, port, currentTime, con);
            return 0;
        } else { // Feed not exist for this content server => Fresh connection
            insertDatabase(body, address, updated, link, port, currentTime, con);
            return 1;
        }
    }

    // Method to insert into database
    private synchronized void insertDatabase(String body, String address, String updated, String link, int port,
            long time, Connection con)
            throws SQLException {
        PreparedStatement ps = con
                .prepareStatement(
                        "INSERT INTO feeds (body, cs_address, updated, link, port_number, save_time) VALUES (?,?,?,?,?,?)");
        ps.setString(1, body);
        ps.setString(2, address);
        ps.setString(3, updated);
        ps.setString(4, link);
        ps.setInt(5, port);
        ps.setString(6, Long.toString(time));
        ps.executeUpdate();
        ps.close();
        con.close();
    }

    // Method to update into database
    private synchronized void updateDatabase(String body, String updated, String address, String link, int port,
            long time, Connection con)
            throws SQLException {
        PreparedStatement ps = con
                .prepareStatement(
                        "UPDATE feeds SET body=?, updated=?, save_time=? WHERE cs_address=? AND link=? AND port_number=?");
        ps.setString(1, body);
        ps.setString(2, updated);
        ps.setString(3, Long.toString(time));
        ps.setString(4, address);
        ps.setString(5, link);
        ps.setInt(6, port);
        ps.executeUpdate();
        ps.close();
        con.close();
    }

    // Return array of entries for parsing GET request
    public synchronized String[] getFeed() throws SQLException {
        Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT body FROM feeds");
        ResultSet rs = ps.executeQuery();

        // Get the body response
        List<String> entryList = new ArrayList<String>();
        while (rs.next()) {
            entryList.add(rs.getString("body"));
        }
        String[] entryArray = new String[entryList.size()];
        entryArray = entryList.toArray(entryArray);
        con.close();
        return entryArray;
    }

    // Delete based on content server address
    public synchronized void deleteFeeds(String address) throws SQLException {
        Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement("DELETE FROM feeds WHERE cs_address=?");
        ps.setString(1, address);
        ps.executeUpdate();
        con.close();
    }

    // Delete all feeds
    public synchronized void deleteAll() throws SQLException {
        Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement("DELETE FROM feeds");
        ps.executeUpdate();
        con.close();
    }

    // Getting number of feeds for a content server
    public synchronized int getFeedNumber(String address) throws SQLException {
        Connection con = getConnection();
        int count = 0;
        PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) AS rowcount FROM feeds WHERE cs_address=?");
        ps.setString(1, address);
        ResultSet rs = ps.executeQuery();
        while (rs.next())
            count = rs.getInt("rowcount");
        rs.close();
        con.close();
        return count;
    }

    // Delete the oldest feeds
    public synchronized void deleteOldestFeed() throws SQLException {
        // Get the oldest date
        String oldestDate = "";
        int id = 0;
        Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM feeds");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            if (oldestDate.isEmpty()) {
                oldestDate = rs.getString("updated");
                id = rs.getInt("id");
            } else {
                String tmpDate = rs.getString("updated");
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                try {
                    Date parsedDateTmp = inputFormat.parse(tmpDate);
                    Date parsedDateOldest = inputFormat.parse(oldestDate);
                    Long t1 = parsedDateTmp.getTime();
                    Long t2 = parsedDateOldest.getTime();

                    // Comparison
                    if (t1.compareTo(t2) < 0) {
                        oldestDate = tmpDate;
                        id = rs.getInt("id");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        // Delete
        ps = con.prepareStatement("DELETE FROM feeds WHERE updated=? AND id=?");
        ps.setString(1, oldestDate);
        ps.setInt(2, id);
        ps.executeUpdate();
        rs.close();
        con.close();
    }

    // Deletes 12 second old feeds, for the purpose of server crashing backup
    public synchronized void delete12SecondFeed() throws SQLException {
        Connection con = getConnection();
        Statement statement = con.createStatement();
        statement.setQueryTimeout(60);

        long time = (new Date()).getTime() - 12000;
        statement.executeUpdate("DELETE FROM feeds WHERE save_time <= " + time);
        con.close();
    }
}
