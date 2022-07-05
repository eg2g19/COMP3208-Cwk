import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static java.lang.Math.abs;

public class SmallMFRecommender {

    //Maps users to items to ratings
    private static HashMap<Integer, HashMap<Integer, Integer>> trainingData;
    private static ArrayList<Integer> items;
    private static ArrayList<Integer> users;
    // testing dataset loaded into arraylist
    private static ArrayList<String[]> predictions;
    private static float[] finalPredctions;
    //maps item to factor to value
    private static HashMap<Integer, float[]> qMatrix;
    //maps user to factor to value
    private static HashMap<Integer, float[]> pMatrix;

    private static int nFactor = 10;
    private static float learningRate = 1f;
    private static float regularization = 1f;
    private static int iterations = 45;

    //filepaths for training and testing datasets
    private static String trainPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard" +
            "\\Social Computing\\comp3208_example_package\\comp3208_100k_train_withratings.csv";
//    private static String trainPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard\\Social Computing\\comp3208_example_package\\comp3208_micro_gold.csv";
//    private static String predPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard\\Social Computing\\comp3208_example_package\\comp3208_micro_pred.csv";
        private static String predPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard\\Social Computing" +
            "\\comp3208_example_package\\comp3208_100k_test_withoutratings.csv";

    //need to randomly shuffle set of (user, item) pairs

    public static void main(String[] args) throws IOException {
        learningRate = 1f / 40f;
        regularization = learningRate / 40f * 10;
        loadData();
        initialisePQ();
        myReccomender1();
//        makePredictions();
//        printPredictions();
        predictOnTraining();
    }

    private static void printPredictions() throws IOException {
        FileWriter csvWriter = new FileWriter("results.csv");
        int index = 0;
        for(String[] entry : predictions){
            Float pred = finalPredctions[index];
            if(pred < 0) pred = 0f;
            if(pred > 5) pred  = 5f;
            csvWriter.append(entry[0]);
            csvWriter.append(", ");
            csvWriter.append(entry[1]);
            csvWriter.append(", ");
            if (!(pred.isNaN())) csvWriter.append(Float.valueOf(pred).toString());
            else csvWriter.append("2.5");
            csvWriter.append(", ");
            csvWriter.append(entry[2]);

            index++;
        }
        csvWriter.close();
    }

    private static void predictOnTraining() {
        float error = 0;
        float divisor = 0;
//        float maxPrediction = 0;
        for(Map.Entry<Integer, HashMap<Integer, Integer>> entry : trainingData.entrySet()) {
            for(Map.Entry<Integer, Integer> entry1 : entry.getValue().entrySet()) {
                float prediction = dotProduct(pMatrix.get(entry.getKey()), qMatrix.get(entry1.getKey()));
                prediction = (prediction * 5) / 23;
//                if(prediction > maxPrediction) maxPrediction = prediction;
                if(prediction < 0 ) prediction = 0;
                float result = entry1.getValue();
                error += abs(prediction - result);
                divisor++;
            }
        }
//        System.out.println(maxPrediction + " = max prediciton");
        System.out.println("MAE = " + error / divisor);
    }

    private static void makePredictions() {
        finalPredctions = new float[predictions.size()];
        int index = 0;
        for(String[] entry : predictions) {
//            System.out.println(index);
            int user = Integer.parseInt(entry[0]);
            int item = Integer.parseInt(entry[1]);

            if(qMatrix.get(item) == null) {
                qMatrix.put(item, setRandomRow());
            }
            if(pMatrix.get(user) == null) {
                pMatrix.put(user, setRandomRow());
            }

            float prediction = dotProduct(pMatrix.get(user), qMatrix.get(item));
            finalPredctions[index] = prediction;
//            System.out.println(prediction);
            index++;
        }
        for(int i = 0; i < 100; i++) System.out.println(finalPredctions[i]);
    }

    private static int[] createItemRandomOrder() {
        Random r = new Random();

        //Froms random order for users
        int[] itemOrder = new int[items.size()];
        ArrayList<Integer> toBeChosen = new ArrayList<>();
        for(int i = 0; i < items.size(); i++) {
            toBeChosen.add(i);
        }
        int size = items.size()-1;
        for(int i = 0; i < items.size(); i++) {
            if(size == 0) itemOrder[i] = toBeChosen.get(0);
            else {
                int index = r.nextInt(size);
                itemOrder[i] = toBeChosen.get(index);
                size--;
                toBeChosen.remove(index);
            }
        }
        return itemOrder;
    }

    private static int[] createUserRandomOrder() {
        Random r = new Random();

        //Froms random order for users
        int[] userOrder = new int[users.size()];
        ArrayList<Integer> toBeChosen = new ArrayList<>();
        for(int i = 0; i < users.size(); i++) {
            toBeChosen.add(i);
        }
        int size = users.size()-1;
        for(int i = 0; i < users.size(); i++) {
            if(size == 0) userOrder[i] = toBeChosen.get(0);
            else {
                int index = r.nextInt(size);
                userOrder[i] = toBeChosen.get(index);
                size--;
                toBeChosen.remove(index);
            }
        }
        return userOrder;
    }

    private static void myReccomender1() {
//        float[][] matrix = new float[users.size()][items.size()];

        for(int i = 0; i < iterations; i++) {

            //Product Matrix of (user,item) pairs
            float[][] productMatrix = new float[users.size()][items.size()];
            for(int userIndex = 0; userIndex < users.size(); userIndex++) {
                for(int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
                    float[] userFactors = pMatrix.get(users.get(userIndex));
                    float[] itemFactors = qMatrix.get(items.get(itemIndex));
                    for(int k = 0; k < nFactor; k++) {
                        productMatrix[userIndex][itemIndex] += userFactors[k] * itemFactors[k];
//                        System.out.println(productMatrix[userIndex][itemIndex]);
                    }
                }
            }

            // generates error matrix for (user,item) pairs
            float[][] errorMatrix = new float[users.size()][items.size()];
            for(int userIndex = 0; userIndex < users.size(); userIndex++) {
                for(int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
                    if(trainingData.get(users.get(userIndex)).containsKey(items.get(itemIndex))) {
                        float error = trainingData.get(users.get(userIndex))
                                .get(items.get(itemIndex))
                                - productMatrix[userIndex][itemIndex];
                        if(error < 0.0001) errorMatrix[userIndex][itemIndex] = 0f;
                        else errorMatrix[userIndex][itemIndex] = error;
//                        System.out.println(errorMatrix[userIndex][itemIndex]);
                    }
                    else errorMatrix[userIndex][itemIndex] = 0f;
                }
            }

            for(int userIndex : createUserRandomOrder()) {
                for(int itemIndex : createItemRandomOrder()) {
                    for(int factor = 0; factor < nFactor; factor++) {
                        float x = errorMatrix[userIndex][itemIndex] * qMatrix.get(items.get(itemIndex))[factor];
                        float inside = x - (regularization * pMatrix.get(users.get(userIndex))[factor]);
                        pMatrix.get(users.get(userIndex))[factor] -= learningRate * inside;
                    }
                }
            }

            for(int itemIndex :createItemRandomOrder()) {
                for(int userIndex : createUserRandomOrder()) {
                    for(int factor = 0; factor < nFactor; factor++) {
                        float x = errorMatrix[userIndex][itemIndex] * pMatrix.get(users.get(userIndex))[factor];
                        float inside = x - (regularization * qMatrix.get(items.get(itemIndex))[factor]);
                        qMatrix.get(items.get(itemIndex))[factor] -= learningRate * inside;
//                        System.out.println( qMatrix.get(items.get(itemIndex))[factor]);
                    }
                }
            }


        }
    }



    private static void myReccomender() {
        for(int i = 0; i < iterations; i++){
            //TODO: shuffle useritem pairs
            int userIndex = 0;
            for(int user : users) {
                for(Map.Entry<Integer, Integer> entry1 : trainingData.get(user).entrySet()) {
//                    for(double d : pMatrix.get(user)) System.out.println("pMatrix Values = " + d);
                    if(qMatrix.get(entry1.getKey()) == null) {
                        qMatrix.put(entry1.getKey(), setRandomRow());
                    }
                    if(pMatrix.get(user) == null) {
                        pMatrix.put(user, setRandomRow());
                    }
                    float prediction = dotProduct(pMatrix.get(user), qMatrix.get(entry1.getKey()));
//                    if(Double.isNaN(prediction)) System.out.println("user = " + user + ". item = " + entry1.getKey());
                    int trueRating = entry1.getValue();
                    float error = trueRating - prediction;
//                    System.out.println("error = " +error);
                    //update user row
                    float[] newRowP = new float[nFactor];
                    for(int j = 0; j < nFactor; j++) {
                        pMatrix.get(user)[j] += learningRate * ((error * qMatrix.get(entry1.getKey())[j]) - (regularization * pMatrix.get(user)[j]));
//                        System.out.println(pMatrix.get(user)[j]);
                    }
                    //update item row
                    float[] newRowQ = new float[nFactor];
                    for(int j = 0; j < nFactor; j++) {
                        qMatrix.get(entry1.getKey())[j] += learningRate * ((error * pMatrix.get(user)[j]) - (regularization * qMatrix.get(entry1.getKey())[j]));
                    }
                }
            }

        }
    }

    private static float[] setRandomRow() {
        float[] row = new float[nFactor];
        Random rand = new Random();
        for(int i = 0;i < nFactor; i++) {
            row[i] = rand.nextFloat() / iterations;
        }
        return row;
    }

    static float dotProduct(float vect_A[], float vect_B[])
    {
        float product = 0;
        // Loop for calculate dot product
//        for(float d : vect_B) {
////            System.out.println(d);
//        }
        for (int i = 0; i < nFactor; i++)
            product = product + vect_A[i] * vect_B[i];
//        System.out.println("product = " + product);
        return product;
    }


    //initialses matrices randomly
    private static void initialisePQ() {
        pMatrix = new HashMap<>();
        qMatrix = new HashMap<>();
        Random r = new Random();
        for(int user : users) {
            float[] row = new float[nFactor];
            for(int i = 0; i < nFactor; i++) {
                row[i] = 1f / nFactor;
            }
            pMatrix.put(user, row);
//            for(double d : pMatrix.get(user)) System.out.println("pMatrix Values = " + d);

        }

        for(int item : items) {
            float[] row = new float[nFactor];
            for(int i = 0; i < nFactor; i++) {
                row[i] = 1f / nFactor;
            }
            qMatrix.put(item, row);
        }
    }


    private static void loadData() throws IOException {

        ArrayList<String[]> training = new ArrayList();
        trainingData = new HashMap<>();
        items = new ArrayList<>();
        users = new ArrayList<>();
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
