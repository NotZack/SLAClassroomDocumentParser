package UserInterfaceConnection;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A socket used to communicate with the user interface.
 */
public class UserInterfaceSocket {

    private static final int SOCKET_PORT = 5119;

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Opens a new socket used to communicate with the user interface.
     */
    public void openSocket() {
        try {
            System.out.println("Opening socket on port " + SOCKET_PORT);
            ServerSocket serverSocket = new ServerSocket(SOCKET_PORT);
            System.out.println("Socket opened! Awaiting connection ...");

            acceptConnection(serverSocket);

            String clientSays;
            while ((clientSays = in.readLine()) != null) {
                acceptInput(clientSays);
            }

            System.out.println("Client text accepted");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void acceptConnection(@NotNull ServerSocket serverSocket) {
        try {
            clientSocket = serverSocket.accept();
            clientSocket.setTcpNoDelay(true);
            out = new PrintWriter(new BufferedOutputStream(clientSocket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            System.out.println("Client connected");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acceptInput(String clientInput) {
        System.out.println("Client said: " + clientInput);
        if ("hello server".equals(clientInput)) {
            out.println("hello client");
        }
        else {
            out.println("unrecognised greeting");
        }
        out.flush();
    }
}
