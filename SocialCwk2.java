import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SocialCwk2 {

    private static String trainPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard" +
            "\\Social Computing\\comp3208_example_package\\comp3208_100k_train_withratings.csv";
    private static String predPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard\\Social Computing" +
            "\\comp3208_example_package\\comp3208_100k_test_withoutratings.csv";

    private static ArrayList<String[]> training;
    private static ArrayList<String[]> predictions;

    private static ArrayList<Integer> users;

    private static HashMap<Integer, HashMap<Integer, Float>> cosineMatrix;

    static HashMap<Integer, ArrayList<Integer>> userItemRatings = new HashMap<>();

    private static HashMap<Integer, Integer[]> userToTop10;

    public static void main(String[] args) throws IOException {

        loadData();
//        createUserList();
//        createUserItemLinks();
//        calcCosineMatrix();
//        makePredictions();
        ArrayList<String> items = new ArrayList<>();
        ArrayList<String> users = new ArrayList<>();

        for(String[] entry : training){
            if(!items.contains(entry[1])) items.add(entry[1]);
        }
        System.out.println(items.size());
        for(String[] entry : training){
            if(!users.contains(entry[0])) users.add(entry[0]);
        }
        System.out.println(users.size());
    }

    //TODO: instead of calculating whole matrix, map user i to map of 50 random users to respective cosine similarity
    // ^ DONE
    //TODO: From here we can select top 10 and compute average and use as prediction
    // ^ DONE
    //Takes 1 minute as per x+1 ....
    public static void calcCosineMatrix() {

        Random ran = new Random();
        cosineMatrix = new HashMap<>();
        for(int i : users) {
//            for(int x : users) {
//                userICosine.add(calcCosine(i, x));
//            }
//            cosineMatrix.putIfAbsent(i, userICosine);

            HashMap<Integer, Float> userIMatrix = new HashMap<>();
            int nxtUser = ran.nextInt(users.size());
            for(int x = 0; x < 1; x++) {
                if((nxtUser = ran.nextInt(users.size())) == 0) nxtUser++ ;
                userIMatrix.putIfAbsent(nxtUser, calcCosine(i, nxtUser));
            }
            cosineMatrix.putIfAbsent(i, userIMatrix);
        }


        //Creates hashmap pf user to top 10 other users
        userToTop10 = new HashMap<>();
        for(int i : cosineMatrix.keySet()) {
//            HashMap<Integer, HashMap<Integer, Float>> top10 = new HashMap<>();
            Integer[] top10 = new Integer[10];
            int acc = 0;

            for(int x : cosineMatrix.get(i).keySet()){
                if(acc < 10) top10[acc] = x;
                else {
                    for(int y = 0; y < 10; y++) {
                        if(cosineMatrix.get(i).get(x) > top10[y]) {
                            top10[y] = x;
                            break;
                        }
                    }
                }
                acc++;
//                System.out.println(top10[4]);
            }
            userToTop10.putIfAbsent(i, top10);
        }
        System.out.println(userToTop10.get(546)[0]);
    }

    public static void makePredictions() throws IOException {
        ArrayList<Integer> madePredictions = new ArrayList<>();
        for(String[] entry : predictions) {
            int user = Integer.parseInt(entry[0]);
            int item = Integer.parseInt(entry[1]);
            ArrayList<Integer> othersRated = new ArrayList<>();
            System.out.println("here, " + userToTop10.get(item)[0]);
            if(userToTop10.containsKey(item)) {
                // TODO: change back to commented out for loop
//                for (int i : userToTop10.get(item)) {
                for (int i = 0; i < 1; i++) {
                    if (userItemRatings.get(user).contains(item));
//                        othersRated.add();
                }
            } else {
                othersRated.add(3);
            }
            int acc = 0;
            for(int i : othersRated) {
                acc = acc + i;
            }
            acc = acc / othersRated.size();
            madePredictions.add(acc);
        }
        writeResults(madePredictions);

    }

    private static void writeResults(ArrayList<Integer> madePredictions) throws IOException {

        FileWriter csvWriter = new FileWriter("results.csv");
//        csvWriter.append(Float.toString(MSE));
//        csvWriter.append(", ");
//        csvWriter.append(Float.toString(RMSE));
//        csvWriter.append(", ");
//        csvWriter.append(Float.toString(MAE));
//
//        csvWriter.flush();
//        csvWriter.close();
        int i = 0;
        for(String[] entry : predictions) {
            csvWriter.append(entry[0]);
            csvWriter.append(", ");
            csvWriter.append(entry[1]);
            csvWriter.append(", ");
            csvWriter.append(Integer.toString(madePredictions.get(i)));
            csvWriter.append(entry[2]);
            csvWriter.append("/n");
            i++;
        }
        csvWriter.flush();
        csvWriter.close();

    }

    public static float calcCosine(int user1, int user2) {

        if(user1 == user2) return 1f;

        ArrayList<Integer> commonRatings = calcCommonRatings(user1, user2);
        if(commonRatings.isEmpty()) return 0f;
        ArrayList<Integer>[] respectiveRatings = new ArrayList[2];
        ArrayList<Integer> user1Ratings = new ArrayList<>();
        ArrayList<Integer> user2Ratings = new ArrayList<>();

        for(int i : commonRatings){
            for(String[] entry : training) {
                if ((Integer.parseInt(entry[0]) == user1)
                        && commonRatings.contains(Integer.parseInt(entry[1]))) user1Ratings.add(Float.valueOf(entry[2]).intValue());
                else if ((Integer.parseInt(entry[0]) == user2)
                        && commonRatings.contains(Integer.parseInt(entry[1]))) user2Ratings.add(Float.valueOf(entry[2]).intValue());
            }
        }
        respectiveRatings[0] = user1Ratings;
        respectiveRatings[1] = user2Ratings;

        Float top = calcTop(respectiveRatings);
        Float bottom = calcBottom(respectiveRatings);
        return top / bottom;

    }

    public static Float calcBottom(ArrayList<Integer>[] respectiveRatings) {
        Float accX = 0f;
        Float accY = 0f;
        for(int i = 0; i < respectiveRatings[0].size(); i++) {
            accX = accX + Float.valueOf(respectiveRatings[0].get(i) * respectiveRatings[0].get(i));
        }
        for(int i = 0; i < respectiveRatings[1].size(); i++) {
            accY = accY + Float.valueOf(respectiveRatings[1].get(i) * respectiveRatings[1].get(i));
        }
        double xRoot = Math.sqrt((double) accX);
        double yRoot = Math.sqrt((double) accY);

        return ((float) (xRoot * yRoot));
    }

    public static Float calcTop(ArrayList<Integer>[] respectiveRatings){
        Float acc = 0f;
        for(int i = 0; i < respectiveRatings[1].size(); i++) {
            acc = acc + Float.valueOf(respectiveRatings[0].get(i) * respectiveRatings[1].get(i));
        }
        return acc;
    }

    public static ArrayList<Integer> calcCommonRatings(int user1, int user2) {

        ArrayList<Integer> commonRatings = new ArrayList<>();
        for(int i : userItemRatings.get(user1)) {
            for(int x : userItemRatings.get(user2)){
                if(i == x) {
                    commonRatings.add(i);
                    break;
                }
            }
        }
        return commonRatings;
    }

    public static void createUserList() {
        users = new ArrayList<>();
        for(String[] entry : training) {
            int user = Integer.parseInt(entry[0]);
            if(!users.contains(user)) users.add(user);
        }
    }

    //Method to create hashmap of items each user has rated
    public static void createUserItemLinks() {
        for(int i : users) {
            ArrayList<Integer> itemsRated = new ArrayList<>();
            for(String[] entry : training) {
                if(Integer.parseInt(entry[0]) == i) itemsRated.add(Integer.parseInt(entry[1]));
            }
            userItemRatings.putIfAbsent(i, itemsRated);
        }
    }


    // Loads data into Arraylists of each line entry.
    private static void loadData() throws IOException {

        training = new ArrayList<String[]>();
        predictions = new ArrayList<String[]>();


        BufferedReader csvReader = new BufferedReader(new FileReader(trainPath));
        String trainRow;
        while((trainRow = csvReader.readLine()) != null) {
            String[] data = trainRow.split(",");
            training.add(data);
        }
        csvReader.close();


        BufferedReader csvReader1 = new BufferedReader(new FileReader(predPath));
        String predRow;
        while((predRow = csvReader1.readLine()) != null) {
            String[] data = predRow.split(",");
            predictions.add(data);
        }
        csvReader1.close();
    }
}
