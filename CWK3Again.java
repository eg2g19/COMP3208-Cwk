//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//public class CWK3Again {
//
//    private static double[][] pMatrix;
//    private static double[][] qMatrix;
//
//    private static HashMap<Integer, Integer> userToIndex;
//    private static HashMap<Integer, Integer> itemToIndex;
//
//    private static int[] users;
//    private static int[] items;
//
//
//    private static String trainPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard" +
//            "\\Social Computing\\comp3208_example_package\\comp3208_100k_train_withratings.csv";
//    private static String predPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard\\Social Computing" +
//            "\\comp3208_example_package\\comp3208_100k_test_withoutratings.csv";
//
//
//
//
//    private static void initPQ() {
//        pMatrix = new double[][]
//    }
//
//    // load training data into arrays
//    private static void loadData() throws IOException {
//
//        ArrayList<String[]> training = new ArrayList();
//        trainingData = new HashMap<>();
//        items = new ArrayList<>();
//        users = new ArrayList<>();
//        predictions = new ArrayList();
//
//        BufferedReader csvReader = new BufferedReader(new FileReader(trainPath));
//        String trainRow;
//        while((trainRow = csvReader.readLine()) != null) {
//            String[] data = trainRow.split(",");
//            training.add(data);
//            if(!users.contains(Float.valueOf(data[0]).intValue())) users.add(Float.valueOf(data[0]).intValue());
//            if(!items.contains(Float.valueOf(data[1]).intValue())) items.add(Float.valueOf(data[1]).intValue());
//        }
//        csvReader.close();
//        for(int user : users) {
//            HashMap<Integer, Integer> itemsToRatings = new HashMap<>();
//            for (String[] entry : training) {
//                if(Float.valueOf(entry[0]).intValue() == user)
//                    itemsToRatings.put(Float.valueOf(entry[1]).intValue(), Float.valueOf(entry[2]).intValue());
//            }
//            trainingData.putIfAbsent(user, itemsToRatings);
//
//        }
//
//
//        BufferedReader csvReader1 = new BufferedReader(new FileReader(predPath));
//        String predRow;
//        while((predRow = csvReader1.readLine()) != null) {
//            String[] data = predRow.split(",");
//            predictions.add(data);
//        }
//        csvReader1.close();
//    }
//}
