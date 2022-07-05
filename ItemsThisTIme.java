import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ItemsThisTIme {

    private static ArrayList<String[]> training;
    private static ArrayList<String[]> predictions;
    private static ArrayList<Integer> items;
    private static ArrayList<Integer> users;
    private static HashMap<Integer, ArrayList<Integer>> itemRatingsMap;
    private static HashMap<Integer, ArrayList<Integer>> userItemsMap;
    private static HashMap<Integer, Float> userAverages;
    private static HashMap<Integer, HashMap<Integer, Float>> similarityMatrix;
    private static String trainPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard" +
            "\\Social Computing\\comp3208_example_package\\comp3208_100k_train_withratings.csv";
    private static String predPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard\\Social Computing" +
            "\\comp3208_example_package\\comp3208_100k_test_withoutratings.csv";


    public static void main(String[] args) {

        try {
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("loaded");

        createItemAndUserList();
        System.out.println("itemUserList done");
        createItemRatingsMap();
        System.out.println("ItemRatingsMap done");
        createUserItemMap();
        System.out.println("UserItemMap done");
        setUserAverages();
        System.out.println("Averages done");
        //approx 15 seconds to run to here.

//        calcCosineMatrix();
        similarityMatrix = new HashMap<>();
        for(int i = 0; i < 10; i++) {
            System.out.println(i);
            for(int item2 : items) {
                HashMap<Integer, Float> itemToSim = new HashMap<>();
                Float cosine = calcCosine(i, item2);
                if(cosine > 0.75f) itemToSim.putIfAbsent(item2, cosine);
                similarityMatrix.putIfAbsent(i, itemToSim);
            }
        }
    }

    private static void calcCosineMatrix() {
        similarityMatrix = new HashMap<>();
        for(int item : items) {
            for(int item2 : items) {
                HashMap<Integer, Float> itemToSim = new HashMap<>();
                Float cosine = calcCosine(item, item2);
                if(cosine > 0.75f) itemToSim.putIfAbsent(item2, cosine);
                similarityMatrix.putIfAbsent(item, itemToSim);
            }
        }
    }
    private static void setUserAverages() {
        userAverages = new HashMap<Integer, Float>();
        for(int i : itemRatingsMap.keySet()) {
            Float tot = 0f;
            for(int x : itemRatingsMap.get(i)) {
                tot = tot + x;
            }
            Float ave = tot / itemRatingsMap.get(i).size();
            userAverages.putIfAbsent(i, ave);
        }
    }

    private static void createUserItemMap() {
        userItemsMap = new HashMap<>();
        for(int i : users) {
            ArrayList<Integer> itemsRated = new ArrayList<>();
            for(String[] entry : training) {
                if(Float.valueOf(entry[0]).intValue() == i) itemsRated.add(Float.valueOf(entry[1]).intValue());
            }
            userItemsMap.putIfAbsent(i, itemsRated);
        }
    }

    //TODO: need to get users a that have rated both and put them in order
    private static ArrayList<Integer> getCommonUsers(int item1, int item2) {
        ArrayList<Integer> commonUsers = new ArrayList<>();
        for(int i : users) {
            if(userItemsMap.get(i).contains(item1) && userItemsMap.get(i).contains(item2)) commonUsers.add(i);
        }
        return commonUsers;
    }


    private static Float calcCosine(int item1, int item2) {

        Float top = calcTop(item1, item2);
        Float bottom = calcBottom(item1, item2);

        return top / bottom;

    }

    private static Float calcBottom(int item1, int item2) {
        Float item1tot = 0f;
        Float item2tot = 0f;
        for(int i : getCommonUsers(item1, item2)) {
            for(String[] entry : training) {
                if (Float.valueOf(entry[0]) == i && Float.valueOf(entry[1]) == item1) {
                    Float item1temp = Float.valueOf(entry[2]) - userAverages.get(i);
                    item1temp = item1temp * item1temp;
                    item1tot = item1tot + item1temp;
                }
                if (Float.valueOf(entry[0]) == i && Float.valueOf(entry[1]) == item2) {
                    Float item2temp = Float.valueOf(entry[2]) - userAverages.get(i);
                    item2temp = item2temp * item2temp;
                    item2tot = item2tot + item2temp;
                }
            }
        }
        item1tot = (float) Math.sqrt(item1tot);
        item2tot = (float) Math.sqrt(item2tot);
        return item1tot * item2tot;

    }

    private static Float calcTop(int item1, int item2) {

        Float tot = 0f;
        for(int i : getCommonUsers(item1, item2)){
            Float item1tot = 0f;
            Float item2tot = 0f;
            for(String[] entry : training) {
                if (Integer.parseInt(entry[0]) == i && Integer.parseInt(entry[1]) == item1) {
                    item1tot = item1tot + (Float.valueOf(entry[2]).intValue()) - (userAverages.get(i));
                }
                if (Integer.parseInt(entry[0]) == i && Integer.parseInt(entry[1]) == item2) {
                    item2tot = item2tot + (Float.valueOf(entry[2]).intValue()) - (userAverages.get(i));
                }
            }
            tot = item1tot * item2tot;
        }

        return tot;
    }

    private static void createItemAndUserList() {
        items = new ArrayList<>();
        users = new ArrayList<>();
        for(String[] entry : training) {
            if(!items.contains(Float.valueOf(entry[1]).intValue())) items.add(Float.valueOf(entry[1]).intValue());
            if(!users.contains(Float.valueOf(entry[0]).intValue())) users.add(Float.valueOf(entry[0]).intValue());
        }

    }


    public static void createItemRatingsMap() {
        itemRatingsMap = new HashMap<>();
        for(int i : items) {
            ArrayList<Integer> itemRatings = new ArrayList<>();
            for(String[] entry : training) {
                if(Float.valueOf(entry[1]).intValue() == i) itemRatings.add(Float.valueOf(entry[2]).intValue());
            }
            itemRatingsMap.putIfAbsent(i, itemRatings);
        }
    }




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
