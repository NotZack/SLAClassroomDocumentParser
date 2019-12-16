import CSVParsing.LuceneIndex;
import UserInterfaceConnection.UserInterfaceSocket;

/**
 * The entry point for starting the application.
 */
public class Main {

    /**
     * Starts the application by loading the csv file(s) and opening a UserInterfaceConnection.UserInterfaceSocket.
     */
    private void startApp() {
        UserInterfaceSocket interfaceSocket = new UserInterfaceSocket();
        interfaceSocket.openSocket();
        interfaceSocket.openCommunication(new LuceneIndex());
    }

    /** Entry point into the application. Starts the application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Main app = new Main();
        app.startApp();
    }
}

