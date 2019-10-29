import java.io.File;

/**
 * The entry point for starting the application.
 */
public class Main {

    /**
     * Starts the application by loading the csv file(s) and opening a UserInterfaceSocket.
     */
    private void startApp() {
        File csvToParse = new File("src/main/resources/SLA_Classroom_Schedules_Fall_2019.csv");
        CSVLoader loader = new CSVLoader(csvToParse);
        loader.loadCSV();

        UserInterfaceSocket interfaceSocket = new UserInterfaceSocket(new CSVDataInteraction(loader.getCSVData()));
        interfaceSocket.openSocket();
    }

    /** Entry point into the application. Starts the application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Main app = new Main();
        app.startApp();
    }
}

