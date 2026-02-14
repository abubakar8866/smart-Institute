package util;

import java.io.*;

public final class FileUtil {

    private FileUtil() { }

    public static void writeToFile(String filePath, String data) {

        try {
            File file = new File(filePath);

            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (BufferedWriter writer =
                         new BufferedWriter(new FileWriter(file, true))) {

                writer.write(data);
                writer.newLine();
            }

        } catch (IOException e) {
            throw new RuntimeException("Error writing to file: " + filePath, e);
        }
    }

    public static String readFile(String filePath) {

        StringBuilder content = new StringBuilder();

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(filePath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line)
                       .append(System.lineSeparator());
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filePath, e);
        }

        return content.toString();
    }
}
