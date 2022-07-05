import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Float.NaN;

public class cwk2Maps {

    //Maps users to items to ratings
    private static HashMap<Integer, HashMap<Integer, Integer>> trainingData;
    //users average ratings
    private static HashMap<Integer, Float> averages;
    //List of items
    private static ArrayList<Integer> items;
    // item mapped to compared items and their similarity
    private static HashMap<Integer, HashMap<Integer, Float>> cosineMap;
    // testing dataset loaded into arraylist
    private static ArrayList<String[]> predictions;

    //filepaths for training and testing datasets
    private static String trainPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard" +
            "\\Social Computing\\comp3208_example_package\\comp3208_100k_train_withratings.csv";
    private static String predPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard\\Social Computing" +
            "\\comp3208_example_package\\comp3208_100k_test_withoutratings.csv";

    public static void main(String[] args) throws IOException {
        System.out.println("loading data");
        loadData();
        System.out.println("data loaded");
        calcAverages();
        System.out.println("calculating matrix");
        calcCosineMatrix();
        System.out.println("matrix built");
        System.out.println("Making predictions...");
        makePredictions();
        System.out.println("Finished predictions");

    }

    private static void makePredictions() throws IOException {
        FileWriter csvWriter = new FileWriter("results.csv");
        int line = 1;
        // Iterates through all testing data and calls methods to predict ratings
        for(String[] entry : predictions) {
            Float result = makePrediction(Float.valueOf(entry[0]).intValue(), Float.valueOf(entry[1]).intValue());
            System.out.println(result);
            csvWriter.append(entry[0]);
            csvWriter.append(", ");
            csvWriter.append(entry[1]);
            csvWriter.append(", ");
            if (!result.isNaN()) csvWriter.append(Float.valueOf(result).toString());
            else csvWriter.append("2.5");
            csvWriter.append(", ");
            csvWriter.append(entry[2]);
            csvWriter.append(System.getProperty("line.separator"));
        }
    }

    private static Float makePrediction(int user, int item) {
        Float top = predTop(user, item);
        Float bottom = predBottom(item, itemsToCompare(user, item));
        return top / bottom;
    }

    //Returns ArrayList listing all items that should be included in calculation of the denominator of the prediction equation
    private static ArrayList<Integer> itemsToCompare(int user, int item) {
        HashMap<Integer, Float> similarities = cosineMap.get(item);
        ArrayList<Integer> toCompare = new ArrayList<>();
        if(trainingData.containsKey(item)) {
            for (Map.Entry<Integer, Float> entry : similarities.entrySet()) {
                if (!(entry.getValue() == NaN) && trainingData.get(user).containsKey(entry.getKey())) {
                    toCompare.add(entry.getKey());
                }
            }
        }
        return toCompare;
    }

    //Calculates denominator of prediction equation
    private static Float predBottom(int item, ArrayList<Integer> itemsToCompare) {
        Float acc = 0f;
        if(trainingData.containsKey(item)) {
            for (Map.Entry<Integer, Float> entry : cosineMap.get(item).entrySet()) {
                //            System.out.println(entry.getValue());
                if (!entry.getValue().isNaN() && itemsToCompare.contains(entry.getKey())) acc = acc + entry.getValue();
            }
        }
        return acc;
    }


    //Calculates numerator of prediction equation
    private static Float predTop(int user, int item){
        HashMap<Integer, Float> similarities = cosineMap.get(item);
        Float acc = 0f;
        if(trainingData.containsKey(item)) {
            for (Map.Entry<Integer, Float> entry : similarities.entrySet()) {
                if (!(entry.getValue() == NaN) && trainingData.get(user).containsKey(entry.getKey())) {
                    int item2Rating = trainingData.get(user).get(entry.getKey());
                    Float sim = entry.getValue();
                    sim = sim * item2Rating;
                    acc = acc + sim;
                }
            }
        }
        if (acc == 0f || acc == NaN) return NaN;
        else return acc;
    }

    //Creates and fills map of users to their average rating
    private static void calcAverages() {
        averages = new HashMap<>();
        for(Map.Entry<Integer, HashMap<Integer, Integer>> entry : trainingData.entrySet()) {
            int count = 0;
            Float average = 0f;
            for(Map.Entry<Integer, Integer> itemRatingMap : entry.getValue().entrySet()) {
                average = average + itemRatingMap.getValue();
                count++;
            }
            average = average / count;
            averages.put(entry.getKey(), average);
        }
    }

    //Iterates through all items to complete a cosine matrix
    private static void calcCosineMatrix() {
        cosineMap = new HashMap<>();
        for(int item1 : items) {
            HashMap<Integer, Float> tempMap = new HashMap<>();
            for(int item2 : items) {
                Float cosine = calcCosine(item1, item2);
                if(cosine > 0) tempMap.putIfAbsent(item2, cosine);
            }
            cosineMap.putIfAbsent(item1, tempMap);
        }
    }

    //calculates adjusted cosine similarity of two given items
    private static Float calcCosine(int item1, int item2) {
        Float top = calcTop(item1, item2);
        Float bottom = calcBottom(item1, item2);

        return top / bottom;
    }

    //calculates numerator of similarity equation
    private static Float calcTop(int item1, int item2) {
        Float tot = 0f;
        for(int i : getCommonUsers(item1, item2)) {
            Float average = averages.get(i);
            int item1rating = trainingData.get(i).get(item1);
            int item2rating = trainingData.get(i).get(item2);

            Float item1temp = item1rating - average;
            Float item2temp = item2rating - average;

            tot = tot + item1temp * item2temp;
        }
        return tot;
    }

    //calculates denominator of similarity equation
    private static Float calcBottom(int item1, int item2) {
        Float item1tot = 0f;
        Float item2tot = 0f;
        for(int i : getCommonUsers(item1, item2)) {
            Float average = averages.get(i);
            int item1rating = trainingData.get(i).get(item1);
            int item2rating = trainingData.get(i).get(item2);
            Float item1temp = (item1rating - average) * (item1rating - average);
            Float item2temp = (item2rating - average) * (item2rating - average);
            item1tot = item1tot + item1temp;
            item2tot = item2tot + item2temp;
            }
        item1tot = (float) Math.sqrt(item1tot);
        item2tot = (float) Math.sqrt(item2tot);

        return item1tot * item2tot;
    }

    //retturns all users that have rated both given items
    private static ArrayList<Integer> getCommonUsers(int item1, int item2) {
        ArrayList<Integer> commonUsers = new ArrayList<>();
        for(Map.Entry<Integer, HashMap<Integer, Integer>> entry : trainingData.entrySet()) {
            if(entry.getValue().containsKey(item1) && entry.getValue().containsKey(item2))
                commonUsers.add(entry.getKey());
        }
        return commonUsers;
    }

    //Loads data into respective data types
    //HashMaps were used on larger datasets rather than ArrayLists to speed up computation
    private static void loadData() throws IOException {

        ArrayList<String[]> training = new ArrayList();
        trainingData = new HashMap<>();
        items = new ArrayList<>();
        ArrayList<Integer> users = new ArrayList<>();
        predictions = new ArrayList();

        BufferedReader csvReader = new BufferedReader(new FileReader(trainPath));
        String trainRow;
        while((trainRow = csvReader.readLine()) != null) {
            String[] data = trainRow.split(",");
            training.add(data);
            if(!users.contains(Float.valueOf(data[0]).intValue())) users.add(Float.valueOf(data[0]).intValue());
            if(!items.contains(Float.valueOf(data[1]).intValue())) items.add(Float.valueOf(data[1]).intValue());
        }
        csvReader.close();
        for(int user : users) {
            HashMap<Integer, Integer> itemsToRatings = new HashMap<>();
            for (String[] entry : training) {
                if(Float.valueOf(entry[0]).intValue() == user)
                    itemsToRatings.put(Float.valueOf(entry[1]).intValue(), Float.valueOf(entry[2]).intValue());
            }
            trainingData.putIfAbsent(user, itemsToRatings);

        }


        BufferedReader csvReader1 = new BufferedReader(new FileReader(predPath));
        String predRow;
        while((predRow = csvReader1.readLine()) != null) {
            String[] data = predRow.split(",");
            predictions.add(data);
        }
        csvReader1.close();
    }
}
