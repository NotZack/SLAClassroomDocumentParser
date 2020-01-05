package UserInterfaceConnection;

import CSVParsing.LuceneIndex;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A socket used to communicate with the user interface.
 */
public class UserInterfaceSocket {

    private static final int SOCKET_PORT = 5119;

    private PrintWriter out;
    private BufferedReader in;

    /**
     * Opens a new socket used to communicate with the user interface.
     */
    public void openSocket() {
        try {
            System.out.println("Opening socket on port " + SOCKET_PORT);
            ServerSocket serverSocket = new ServerSocket(SOCKET_PORT);
            System.out.println("New socket opened! Awaiting connection ...");

            acceptConnection(serverSocket);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void acceptConnection(@NotNull ServerSocket serverSocket) {
        try {
            Socket clientSocket = serverSocket.accept();
            clientSocket.setTcpNoDelay(true);
            out = new PrintWriter(new BufferedOutputStream(clientSocket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            System.out.println("Client connected");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openCommunication(LuceneIndex luceneIndex) {
        try {
            String clientQuery;
            while ((clientQuery = in.readLine()) != null) {
                if (clientQuery.startsWith("Collect_Unique_Room_Data:")) {
                    out.println(luceneIndex.collectRoomData(clientQuery.substring(clientQuery.indexOf(":") + 1)));
                }
                else {
                    clientQuery = clientQuery.replaceAll("[^0-9a-zA-Z ]+", "");
                    clientQuery = clientQuery.replaceAll(" +", " ");
                    if (clientQuery.length() > 0 && !clientQuery.equals(" ")) {
                        out.println(luceneIndex.parseInexactQuery(clientQuery));
                    }
                    else {
                        out.println("Invalid query");
                    }
                }
            }
        }
        catch (IOException e) {
            System.out.println(e + " and/or client disconnected");
            openSocket();
        }
    }
}
