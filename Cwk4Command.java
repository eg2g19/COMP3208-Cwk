import java.sql.*;

public class Cwk4Command {
    static Connection database;

    public static void main(String[] args) throws SQLException {
        String fn = "MyDatabase.db";
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + fn);
        if (connection != null) {
            database = connection;
        }

        String s = "SELECT Rating FROM trainingTable WHERE UserId=1 AND ItemID=1";
        Statement st = database.createStatement();
        ResultSet rs = st.executeQuery(s);
        while(rs.next()) System.out.println(rs.getInt(1));


    }

    //returns resultSet of list of userIDs
    private static ResultSet getUsers() throws SQLException {
        String getUsers = "SELECT DISTINCT UserID FROM trainingTable";

        Statement selectStatement = database.createStatement();
        ResultSet rs = selectStatement.executeQuery(getUsers);

        return rs;
    }

    //returns resultSet of list of itemIDs
    private static ResultSet getItems() throws SQLException {
        String getItems = "SELECT DISTINCT ItemID FROM trainingTable";

        Statement selectStatement = database.createStatement();
        ResultSet rs = selectStatement.executeQuery(getItems);

        return rs;
    }


    public static void trial() {

    }

    //    private void initBias() throws SQLException {
//        PreparedStatement userPS = database.prepareStatement("SELECT Rating FROM trainingTable WHERE UserID=?");
//        PreparedStatement itemPS = database.prepareStatement("SELECT Rating FROM trainingTable WHERE ItemID=?");
//
//        String selectUsers = "SELECT DISTINCT UserID FROM trainingTable";
//        Statement selectUsersSt = database.createStatement();
//        ResultSet users = selectUsersSt.executeQuery(selectUsers);
//
//        String selectItems = "SELECT DISTINCT ItemID FROM trainingTable";
//        Statement selectItemsSt = database.createStatement();
//        ResultSet items = selectItemsSt.executeQuery(selectItems);
//
//        while(users.next()) {
//            int user = users.getInt(1);
//            userPS.setInt(1, user);
//            ResultSet userRatings =
//        }
//
//    }
}
