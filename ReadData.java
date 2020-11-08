import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.ArrayList;
import java.util.Scanner; // Import the Scanner class to read text files

public class ReadData {
    public static ArrayList<Double> readData() {
        ArrayList<Double> donnees = new ArrayList();
        try {
            String filePath = new File("src/main/java/Data.txt").getAbsolutePath();
            File myObj = new File( filePath);
            Scanner myReader = new Scanner(myObj);

            while (myReader.hasNextLine()) {
                double data = Double.parseDouble(myReader.nextLine().split("=")[1]);
                donnees.add(data);
            }

            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return donnees;
    }
}