import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;
import static java.lang.Float.isNaN;

/*
Class responsible for performing matrix factorization and training the model
 */

public class Cwk4MF {

    Connection database;
    HashMap<Integer, float[]> pMatrix;  //users
    HashMap<Integer, float[]> qMatrix;   // items
    HashMap<Integer, Float> userBias;   // items
    HashMap<Integer, Float> itemBias;   // items

    final int nFactor = 8;
    final int iterations = 8;
    final float learningRate = 0.002f;
    final float regulization = 0.01f;


    // Constructor sets connection to database initialised in "Cwk4.java"
    public Cwk4MF(Connection connection) throws SQLException {
        database = connection;

        try {
            initPQ();
            System.out.println("Initialised PQ matrix");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Could not initialise PQ");
        }
    }

    public void train() throws SQLException {
        for(int i = 0; i < iterations; i++) {
            trainReccomender();
        }
    }


    //One Iteration of training using stochastic gradient descent
    public void trainReccomender() throws SQLException {
        String userItemRatings = "SELECT UserId, ItemID, Rating FROM trainingTable ORDER BY RANDOM()";
        Statement st = database.createStatement();
        ResultSet rs = st.executeQuery(userItemRatings);
        while(rs.next()) {
            int user = rs.getInt(1);
            int item = rs.getInt(2);
            float rating = rs.getFloat(3);

            float prediction = getPrediction(user, item);
            float[] rowP = pMatrix.get(user);
            float[] rowQ = qMatrix.get(item);
            float error = prediction - rating;

            for(int k = 0; k < nFactor; k++) {
                float toAdd = learningRate * (error * qMatrix.get(item)[k] - (regulization * pMatrix.get(user)[k]));
                pMatrix.get(user)[k] = rowP[k] - toAdd;
            }
            for(int k = 0; k < nFactor; k++) {
                float toAdd = learningRate * (error * pMatrix.get(user)[k] - (regulization * qMatrix.get(item)[k]));
                qMatrix.get(item)[k] = rowQ[k] - toAdd;
            }
        }
    }


    //Initialises P and Q Matrices with values between 0 and 1
    private void initPQ() throws SQLException {
        pMatrix = new HashMap<>();
        qMatrix = new HashMap<>();
        Random r = new Random();

        ResultSet users = getUsers();

        while(users.next()) {
            int user = users.getInt(1);
            float[] row = new float[nFactor];
            for(int k = 0; k < nFactor; k++) {
                Float f = r.nextFloat();
                while(f.isNaN()) f = r.nextFloat();
                row[k] = f;
            }
            pMatrix.put(user, row);
        }

        ResultSet items = getItems();
        while(items.next()) {
            int item = items.getInt(1);
            float[] row = new float[nFactor];
            for(int k = 0; k < nFactor; k++) {
                Float f = r.nextFloat();
                while(f.isNaN()) f = r.nextFloat();
                row[k] = f;
            }
            qMatrix.put(item, row);
        }
    }

    public void writeResults() throws IOException, SQLException {
        FileWriter csvWriter = new FileWriter("Cwk4Results.csv");
        String testUserItem = "SELECT * FROM testingData";
        Statement st = database.createStatement();
        ResultSet rs = st.executeQuery(testUserItem);

        while(rs.next()) {
            int user = rs.getInt(1);
            int item = rs.getInt(2);
            String timeStamp = rs.getString(3);
            float prediction = 0f;
            for(int n = 0; n < nFactor; n++) {
                if(pMatrix.containsKey(user) && qMatrix.containsKey(item)) prediction += pMatrix.get(user)[n] * qMatrix.get(item)[n];
                else prediction = 2.5f;
            }
            csvWriter.append(Float.valueOf(user).toString());
            csvWriter.append(", ");
            csvWriter.append(Float.valueOf(item).toString());
            csvWriter.append(", ");
            csvWriter.append(Float.toString(prediction));
            csvWriter.append(", ");
            csvWriter.append(timeStamp);
            csvWriter.append(System.getProperty("line.separator"));
        }
        csvWriter.close();
    }

    // prints MAE of predictions on testing set to terminal
    public void testOnTraining() throws SQLException {
        String userItemRatings = "SELECT UserId, ItemID, Rating FROM trainingTable ORDER BY RANDOM()";
        Statement st = database.createStatement();
        ResultSet rs = st.executeQuery(userItemRatings);
        int count = 0;
        float errorCount = 0f;

        while(rs.next()) {
            count++;
            int user = rs.getInt(1);
            int item = rs.getInt(2);
            float rating = rs.getFloat(3);
            float prediction = 0f;
            for(int n = 0; n < nFactor; n++) {
                prediction += pMatrix.get(user)[n] * qMatrix.get(item)[n];
            }
            errorCount += Math.abs(prediction - rating);
        }

        System.out.println(errorCount / count);
    }

    //Returns prediciton
    private float getPrediction(int user, int item) {
        float prediction = 0;
        for(int n = 0; n < nFactor; n++) {
            prediction += (pMatrix.get(user)[n]) * (qMatrix.get(item)[n]);
        }
        return prediction;
    }

    //returns resultSet of list of userIDs
    private ResultSet getUsers() throws SQLException {
        String getUsers = "SELECT DISTINCT UserID FROM trainingTable";

        Statement selectStatement = database.createStatement();
        ResultSet rs = selectStatement.executeQuery(getUsers);

        return rs;
    }

    //returns resultSet of list of itemIDs
    private ResultSet getItems() throws SQLException {
        String getItems = "SELECT DISTINCT ItemID FROM trainingTable";

        Statement selectStatement = database.createStatement();
        ResultSet rs = selectStatement.executeQuery(getItems);

        return rs;
    }

//    private void initBias() throws SQLException {
//        PreparedStatement userPS = database.prepareStatement("SELECT UserID, Rating FROM trainingTable WHERE UserID=?");
//        PreparedStatement itemPS = database.prepareStatement("SELECT ItemID, Rating FROM trainingTable WHERE ItemID=?");
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
