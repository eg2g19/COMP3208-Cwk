import java.io.*;
import java.util.ArrayList;

import static java.lang.Math.abs;

public class Main {

    private static String predPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard" +
            "\\Social Computing\\comp3208_example_package\\comp3208_micro_pred.csv";
    private static String goldPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard" +
            "\\Social Computing\\comp3208_example_package\\comp3208_micro_gold.csv";
    private static ArrayList<Float> predictions;
    private static ArrayList<Float> goldRatings;

    public static void main(String[] args) throws IOException {

        loadData();
        float MAE = computeMAE();
        float[] MSEs = computeMSE();
        float MSE = MSEs[0];
        float RMSE = MSEs[1];

        writeResults(MAE, RMSE, MSE);
//        System.out.println(MAE + " " + MSE + " " + RMSE);

    }

    private static void loadData() throws IOException {
        predictions = new ArrayList<Float>();
        goldRatings = new ArrayList<Float>();


        BufferedReader csvReader = new BufferedReader(new FileReader(predPath));
        String predRow;
        while((predRow = csvReader.readLine()) != null) {
            String[] data = predRow.split(",");
            predictions.add(Float.parseFloat(data[2]));
        }
        csvReader.close();


        BufferedReader csvReader1 = new BufferedReader(new FileReader(goldPath));
        String goldRow;
        while((goldRow = csvReader1.readLine()) != null) {
            String[] data = goldRow.split(",");
            goldRatings.add(Float.parseFloat(data[2]));
        }
        csvReader1.close();
    }

    private static float computeMAE() {
        int i = 0;
        float rec = 0;
        for(Float f : predictions) {
            float temp = abs(f - goldRatings.get(i));
            rec = rec + temp;
            i++;
        }
        return rec / (i+1);
    }

    private static float[] computeMSE() {
        int i = 0;
        float rec = 0;
        for(Float f : predictions) {
            float subtraction = f - goldRatings.get(i);
            float temp = subtraction * subtraction;
            rec = rec + temp;
            i++;
        }
        rec = rec / (i+1);
        float[] ans = {rec, (float) Math.sqrt(rec)};
        return ans;
    }

    private static void writeResults(float MAE, float RMSE, float MSE) throws IOException {

        FileWriter csvWriter = new FileWriter("results.csv");
        csvWriter.append(Float.toString(MSE));
        csvWriter.append(", ");
        csvWriter.append(Float.toString(RMSE));
        csvWriter.append(", ");
        csvWriter.append(Float.toString(MAE));

        csvWriter.flush();
        csvWriter.close();


    }
}
