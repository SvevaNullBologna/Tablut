package Custom;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class writeLogs {
    public  static  final String file_path = "D:\\Tablut\\TablutCompetition-master\\Tablut\\src\\Custom\\PersonalLogs.txt";
    public static void  write(String stringa){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file_path, true));
            writer.append(stringa);
            writer.close();
        }
        catch (IOException e){
            System.exit(-1);
        }
    }

    public static void handleCriticalError(String message, Exception e){
        write("\n" + message + "" +  Exception.class + "\n");
        System.exit(1);
    }
}
