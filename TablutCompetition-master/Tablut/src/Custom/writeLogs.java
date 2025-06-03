package Custom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class writeLogs {
    // Path relativo alla directory del progetto
    private static final String file_path = "logs/melanzanina_log.txt";

    public static void write(String stringa) {
        try {
            File logFile = new File(file_path);
            File parentDir = logFile.getParentFile();

            // Crea la directory "logs" se non esiste
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Scrive (o crea) il file in append
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
            writer.append(stringa);
            writer.close();
        } catch (IOException e) {
            System.err.println("Errore nella scrittura del file di log: " + e.getMessage());
            System.exit(-1);
        }
    }

    public static void handleCriticalError(String message, Exception e) {
        write("\n" + message + ": " + e.toString() + "\n");
        System.exit(1);
    }
}
