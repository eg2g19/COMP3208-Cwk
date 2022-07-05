import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

/*
Class responsible for populating databases with training and testing data.
 */


public class Cwk4 {

    /*
    TOTAL DISTINCT USERS 138493
    TOTAL ITEMS 26697
     */
    static String trainPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard\\Social Computing" +
            "\\comp3208_example_package\\comp3208_20m_train_withratings.csv";
    static String testPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard\\Social Computing" +
            "\\comp3208_example_package\\comp3208_20m_test_withoutratings.csv";
    //10K
//    static String trainPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard" +
//        "\\Social Computing\\comp3208_example_package\\comp3208_micro_gold.csv";

    static Connection database;

    public static void main(String[] args) throws SQLException, IOException {
        createDB();
        System.out.println("Database connected");
        createTables();
        System.out.println("Database Tables Created");
        loadTrainingIntoTable();
        System.out.println("Training Data Loaded Into Training Table");
        loadTestIntoTable();
        System.out.println("Testing Data Loaded Into Testing Table");
        Cwk4MF factorizor = new Cwk4MF(database);
        factorizor.train();
        System.out.println("Training Complete");
        System.out.println("Final MAE = ");
        factorizor.writeResults();

        try {
            finish();
            System.out.println("Clean Disconnect");
        } catch(SQLException se) {
            System.out.println("Unclean Disconnect");
        }

    }


    //forms connection with database using JDBC
    private static void createDB() throws SQLException {

        String fn = "MyDatabase.db";
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + fn);
        if (connection != null) {
            database = connection;
        }
        database.setAutoCommit(false);
    }

    //Loads test .csv into respective table in database
    private static void loadTestIntoTable() throws IOException, SQLException {
        int i = 0;
        BufferedReader csvReader = new BufferedReader(new FileReader(testPath));
        String trainRow;
        PreparedStatement statement = database.prepareStatement("INSERT INTO testingData VALUES (?, ?, ?)");
        while((trainRow = csvReader.readLine()) != null) {
            String[] data = trainRow.split(",");
            statement.setInt(1, Float.valueOf(data[0]).intValue());
            statement.setInt(2, (Float.valueOf(data[1]).intValue()));
            statement.setString(3, data[2]);
            statement.addBatch();

            if (i % 15000 == 0) {
                statement.executeBatch();
                database.commit();
            }
            i++;
        }
        statement.executeBatch();
        database.commit();
    }

    // Loads training .csv into respective table within database
    private static void loadTrainingIntoTable() throws IOException, SQLException {
        int i = 0;
        BufferedReader csvReader = new BufferedReader(new FileReader(trainPath));
        String trainRow;
        PreparedStatement statement = database.prepareStatement("INSERT INTO trainingTable VALUES (?, ?, ?)");
        int index = 0;
        while((trainRow = csvReader.readLine()) != null) {
            String[] data = trainRow.split(",");
//            statement.setInt(1, index);
            statement.setInt(1, Float.valueOf(data[0]).intValue());
            statement.setInt(2, (Float.valueOf(data[1]).intValue()));
            statement.setFloat(3, (Float.valueOf(data[2])));
            statement.addBatch();
            index++;

            if (i % 15000 == 0) {
                statement.executeBatch();
                database.commit();
            }
            i++;
        }
        statement.executeBatch();
        database.commit();
    }

    //Initially creates tables
    private static void createTables() throws SQLException {

        String sqlCreateTrainingTable = "CREATE TABLE IF NOT EXISTS trainingTable (UserID INT, ItemID INT, Rating FLOAT)";

        String sqlCreateTestingTable = "CREATE TABLE IF NOT EXISTS testingData (UserID INT, ItemID INT, Time Text)";


        Statement createTrainingTable = database.createStatement();
        createTrainingTable.execute(sqlCreateTrainingTable);
        createTrainingTable.close();

        Statement createTestingTable = database.createStatement();
        createTestingTable.execute(sqlCreateTestingTable);
        createTestingTable.close();

    }

    //Safely closes connection with database
    private static void finish() throws SQLException {
        database.close();
    }

}




