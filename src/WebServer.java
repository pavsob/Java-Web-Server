import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class that setups the server and listens to connections.
 * 
 */
public class WebServer {

    private ExecutorService executor = Executors.newFixedThreadPool(2);
    private ServerSocket serverSocket; // listen for client connection requests on this server socket

    /**
     * Method that creates a new server and listens to connections.
     * 
     * @param directoryPath Directory path from which are supplied documents to the client
     * @param port Port number on which the server listens
     */
    public WebServer(String directoryPath, int port) {
        try {
            serverSocket = new ServerSocket(port);
            while (serverSocket.isBound() && !serverSocket.isClosed()) {

                Socket conn = serverSocket.accept();
                HTTPHandler connHandler = new HTTPHandler(conn, directoryPath);
                executor.submit(connHandler);
            }
        } catch (IOException ioe) {
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) { }
            }
        }
    }
}
