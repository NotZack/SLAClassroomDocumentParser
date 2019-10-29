import org.jetbrains.annotations.Contract;

import java.io.*;
import java.util.ArrayList;

class CSVLoader {

    private File csvFile;
    private String[] csvHeaders;
    private ArrayList<Room> dataStore = new ArrayList<>();

    @Contract(pure = true)
    CSVLoader(File csvFile) {
        this.csvFile = csvFile;
    }

    void loadCSV() {
        if (!csvFile.exists()) {
            return;
        }

        try {
            parseDataFile(new FileReader(csvFile));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parses the given data file into its own PropellerDataOrganizer
     * @param fileToParse The given propeller data file to parse
     */
    private void parseDataFile(FileReader fileToParse) {
        BufferedReader br = new BufferedReader(fileToParse);
        try {
            String currentLine = br.readLine();
            csvHeaders = currentLine.split("[,]");

            //Parses each line of file into usable array elements
            while ((currentLine = br.readLine()) != null) {
                String[] parsedLine = currentLine.split("[,]");
                dataStore.add(new Room(parsedLine));
            }
        }
        catch (IOException e) {
            System.err.println(e);
        }
    }

    String[] getCSVHeaders() {
        return csvHeaders;
    }

    ArrayList<Room> getCSVData() {
        return dataStore;
    }
}