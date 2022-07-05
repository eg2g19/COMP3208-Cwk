import java.io.IOException;

        import java.util.*;
        import java.io.*;
        import java.lang.Math;
        import java.util.Random;

class Reccommender
{

    private static ArrayList<Integer> users;
    private static ArrayList<Integer> items;

    private static String trainPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard" +
            "\\Social Computing\\comp3208_example_package\\comp3208_100k_train_withratings.csv";
    private static String predPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard\\Social Computing" +
            "\\comp3208_example_package\\comp3208_100k_test_withoutratings.csv";
    public static class Pair<A,B>
    {
        public final A a;
        public final B b;

        public Pair(A a, B b)
        {
            this.a = a;
            this.b = b;
        }
    }

    public static float[][] CreateMatrix(String inputFile)throws FileNotFoundException, IOException
    {
        users = new ArrayList<>();
        items = new ArrayList<>();
        BufferedReader csvReader = new BufferedReader(new FileReader(trainPath));
        String trainRow;
        while((trainRow = csvReader.readLine()) != null) {
            String[] data = trainRow.split(",");
            if(!users.contains(Integer.parseInt(data[0]))) users.add(Integer.parseInt(data[0]));
            if(!items.contains(Integer.parseInt(data[1])))items.add(Integer.parseInt(data[1]));
        }
        // Initialize the matrix with -1 for all elements
        float[][] matrix = new float[users.size()][items.size()];
        for (int i = 0; i<matrix.length; ++i)
        {
            for (int j = 0; j<matrix[0].length; ++j)
            {
                matrix[i][j] = -1f;
            }
        }

        while((trainRow = csvReader.readLine()) != null) {
            String[] data = trainRow.split(",");
            int user = Integer.parseInt(data[0]);
            int item = Integer.parseInt(data[1]);
            float rating = Float.parseFloat(data[2]);
            matrix[user-1][item-1] = rating;
        }

        // Read the input values and form the full matrix
        return matrix;
    }

    public static int[][] testData(String testFile)throws FileNotFoundException, IOException
    {
        BufferedReader csvReader = new BufferedReader(new FileReader(predPath));
        String testRow;

        int[][] data = new int[9430][2];
        int i = 0;
        while ((testRow = csvReader.readLine()) != null)
        {
            String[] data1 = testRow.split(",");
            data[i][0] = Integer.parseInt(data1[0]);
            data[i][1] = Integer.parseInt(data1[1]);
            i += 1;
        }
        return data;
    }

    public static Pair<float[][], float[][]> myRecommender(float[][] matrix, int r, float rate, float lambda)
    {
        int maxIter = 100;
        int n1 = matrix.length;
        int n2 = matrix[0].length;

        float[][] U = new float[n1][r];
        float[][] V = new float[n2][r];

        // Initialize U and V matrix
        Random rand = new Random();
        for (int i = 0; i < U.length; ++i)
        {
            for (int j = 0; j < U[0].length; ++j)
            {
                U[i][j] = rand.nextFloat()/(float)r;
            }
        }

        for (int i = 0; i < V.length; ++i)
        {
            for (int j = 0; j < V[0].length; ++j)
            {
                V[i][j] = rand.nextFloat()/(float)r;
            }
        }


        // Gradient Descent
        for (int iter = 0; iter < maxIter; ++iter)
        {
//			System.out.println("Iteration no. " + iter + " / " + maxIter);

            float[][] prodMatrix = new float[n1][n2];
            for (int i = 0; i < n1; ++i)
            {
                for (int j = 0; j < n2; ++j)
                {
                    for (int k = 0; k < r; ++k)
                    {
                        prodMatrix[i][j] += U[i][k]*V[j][k];
                    }
                }
            }

            float[][] errorMatrix = new float[n1][n2];
            for (int i = 0; i < n1; ++i)
            {
                for (int j = 0; j < n2; ++j)
                {
                    if (matrix[i][j] == -1f) // MATRIX IS THE [USER][ITEM] = RATING : IS -1 IF USER HAS NOT RATED ITEM
                    {
                        errorMatrix[i][j] = 0f;
                    }
                    else
                    {
                        errorMatrix[i][j] = matrix[i][j] - prodMatrix[i][j];
                    }
                }
            }

            float[][] UGrad = new float[n1][r];
            for (int i = 0; i < n1; ++i)
            {
                for (int j = 0; j < r; ++j)
                {
                    for (int k = 0; k < n2; ++k)
                    {
                        UGrad[i][j] += errorMatrix[i][k]*V[k][j];
                    }
                }
            }

            float[][] VGrad = new float[n2][r];
            for (int i = 0; i < n2; ++i)
            {
                for (int j = 0; j < r; ++j)
                {
                    for (int k = 0; k < n1; ++k)
                    {
                        VGrad[i][j] += errorMatrix[k][i]*U[k][j];
                    }
                }
            }

            float[][] Un = new float[n1][r];
            for (int i = 0; i < n1; ++i)
            {
                for (int j = 0; j < r; ++j)
                {
                    Un[i][j] = (1f - rate*lambda)*U[i][j] + rate*UGrad[i][j];
                }
            }

            float[][] Vn = new float[n2][r];
            for (int i = 0; i < n2; ++i)
            {
                for (int j = 0; j < r; ++j)
                {
                    Vn[i][j] = (1f - rate*lambda)*V[i][j] + rate*VGrad[i][j];
                }
            }

            U = Un;
            V = Vn;
        }

        Pair<float[][], float[][]> p = new Pair<float[][], float[][]>(U,V);
        return p;
    }

    public static void PredictRating(float[][] U, float[][] V, int [][] test)throws FileNotFoundException, IOException
    {
        int n1 = U.length;
        int n2 = V.length;
        int r = V[0].length;

        float[][] prodMatrix = new float[n1][n2];
        for (int i = 0; i < n1; ++i)
        {
            for (int j = 0; j < n2; ++j)
            {
                for (int k = 0; k < r; ++k)
                {
                    prodMatrix[i][j] += U[i][k]*V[j][k];
                }
            }
        }

        PrintWriter writer = new PrintWriter("result.csv", "UTF-8");
        for (int i = 0; i < test.length; ++i)
        {
            int user = users.indexOf(test[i][0]);
            int movie = items.indexOf(test[i][1]);
            if(!(user == -1) && !(movie ==-1)) writer.println(prodMatrix[user][movie]);
            else writer.println(2.5);
        }
        writer.close();
    }

    public static void main(String args[])throws IOException
    {
        System.out.println("Recommendation System Ratings!!!");

        int iterations = 45;
        String inputFile = trainPath;
        String testFile = predPath;
        int r = 15;
        float rate = 1f / iterations;
        float lambda = rate / iterations * 10;

        float[][] matrix = CreateMatrix(inputFile);
        int[][] test = testData(testFile);

        Pair<float[][], float[][]> p = myRecommender(matrix,r,rate,lambda);
        PredictRating(p.a,p.b,test);
    }
}