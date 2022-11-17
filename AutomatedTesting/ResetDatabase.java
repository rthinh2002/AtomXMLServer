import java.sql.SQLException;

public class ResetDatabase {
    public static void main(String[] args) {
        Database db = new Database();
        try {
            db.deleteAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
