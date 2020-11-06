import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.ArrayList;
import java.util.Scanner; // Import the Scanner class to read text files

public class ReadData {
    public static ArrayList readData() {
        ArrayList donnees = new ArrayList();
        try {
            File myObj = new File("/home/falloudiakhate/IdeaProjects/TravailFinal/src/main/java/Data.txt");
            Scanner myReader = new Scanner(myObj);

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine().split("=")[1];
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