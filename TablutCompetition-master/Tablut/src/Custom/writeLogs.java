package Custom;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class writeLogs {
    public  static  final String file_path = "D:\\Tablut-test\\TablutCompetition-master\\Tablut\\src\\Custom\\PersonalLogs.txt";
    public static void  write(String stringa){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file_path, true));
            writer.append(stringa);
            writer.close();
        }
        catch (IOException e){

        }
    }
}
