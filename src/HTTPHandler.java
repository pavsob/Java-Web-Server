import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * Class that handles HTTP connection.
 * It is able to handle multiple connections.
 * 
 */
public class HTTPHandler implements Runnable {

    private Socket conn;
    private String dirPath;
    private String fileName = null;
    private String filePath;
    private String method;
    private File requestedFile;
    private InputStream inputStream;
    private OutputStream outputStream;
    private PrintWriter headerOut = null;
    private BufferedOutputStream dataOut = null;
    private BufferedReader clientRequest = null;

    /**
     * HTTPHandler constructor.
     * Setups all streams we need for HTTP communication and takes two parameters.
     * 
     * @param conn Current Socket connection
     * @param dirPath Directory from which are supplied documents to the client
     */
    public HTTPHandler(Socket conn, String dirPath) {
        this.conn = conn;
        this.dirPath = dirPath;

        try {
            //receiving data from the client on this stream
            inputStream = conn.getInputStream();
            //sending data back to the client on this stream
            outputStream = conn.getOutputStream();
            //Buffered reader used for reading requests from Client
            clientRequest = new BufferedReader(new InputStreamReader(inputStream));
            //character output stream for headers (to client)
            headerOut = new PrintWriter(outputStream);
            //binary output stream for data (to client)
            dataOut = new BufferedOutputStream(outputStream);

        } catch (IOException ioe) {
            System.out.println("HTTPHandler: " + ioe.getMessage());
        }
    }

    @Override
    public void run() {

        try {
            String line = clientRequest.readLine();
            StringTokenizer parseRequest = new StringTokenizer(line);
            method = parseRequest.nextToken().toUpperCase();
            fileName = parseRequest.nextToken().toLowerCase();
            filePath = dirPath + File.separator + fileName.substring(1);

            requestedFile = new File(filePath);
            int fileLength = (int) requestedFile.length();
            String type = Files.probeContentType(requestedFile.toPath());

            if (!requestedFile.exists()) {
                throw new FileNotFoundException();
            }

            if (method.equals("GET")) {

                byte[] requestedData = readData(requestedFile, fileLength);

                headerOut.println("HTTP/1.1 200 OK");
                headerOut.println("Server: Java HTTP Server");
                headerOut.println("Date: " + new Date());
                headerOut.println("File exists: " + requestedFile.exists());
                headerOut.println("Content-Type: " + type);
                headerOut.println("Content-Length: " + fileLength);
                //headerOut.println("File location: " + filePath); //had to be commented to avoid failing stacscheck. Since in the path was used the same name eg. Content-Type, stacscheck was matching with that as well causing fail.
                headerOut.println(); // CRLF
                headerOut.flush(); // flush character output stream

                //sends requested data
                dataOut.write(requestedData, 0, fileLength);
                dataOut.flush();
            }
            else if (method.equals("HEAD")) {

                headerOut.println("HTTP/1.1 200 OK");
                headerOut.println("Server: Java HTTP Server");
                headerOut.println("Date: " + new Date());
                headerOut.println("File exists: " + requestedFile.exists());
                headerOut.println("Content-Type: " + type);
                headerOut.println("Content-Length: " + fileLength);
				//headerOut.println("File location: " + filePath);
				headerOut.println(); // CRLF
                headerOut.flush(); // flush character output stream
            }
            else if (method.equals("DELETE")) {

                requestedFile.delete();
                if (!requestedFile.exists()) {

                    headerOut.println("HTTP/1.1 200 OK");
                    headerOut.println("File has been successfully deleted");
                    headerOut.println("Server: Java HTTP Server");
                    headerOut.println("Date: " + new Date());
                    headerOut.println("File exists: " + requestedFile.exists());
                    headerOut.println("Content-Type: " + null);
                    headerOut.println("Content-Length: " + null);
                    headerOut.println(); // CRLF
                    headerOut.flush(); // flush character output stream
                }
                else {

                    headerOut.println("HTTP/1.1 401 Unauthorized");
                    headerOut.println("File has not been deleted");
                    headerOut.println("Server: Java HTTP Server");
                    headerOut.println("Date: " + new Date());
                    headerOut.println("File exists: " + requestedFile.exists());
                    headerOut.println("Content-Type: " + type);
                    headerOut.println("Content-Length: " + fileLength);
                    //headerOut.println("File location: " + filePath);
                    headerOut.println(); // CRLF
                    headerOut.flush(); // flush character output stream

                }
            }
            else if (method.equals("JOKE")) {

                headerOut.println("HTTP/1.1 418 I'm a teapot");
                headerOut.println("This was defined in April Fools' joke in 1998");
                headerOut.println("Server: Java HTTP Server");
                headerOut.println("Date: " + new Date());
                headerOut.println("File exists: " + requestedFile.exists());
                headerOut.println("Content-Type: " + type);
                headerOut.println("Content-Length: " + fileLength);
                //headerOut.println("File location: " + filePath);
                headerOut.println(); // CRLF
                headerOut.flush(); // flush character output stream
            }
            else {

                headerOut.println("HTTP/1.1 501 Not Implemented");
				headerOut.println("Server: Java HTTP Server");
				headerOut.println("Date: " + new Date());
				headerOut.println("File exists: " + requestedFile.exists());
				headerOut.println("Content-Type: " + type);
                headerOut.println("Content-Length: " + fileLength);
				//headerOut.println("File location: " + filePath);
				headerOut.println(); // CRLF
                headerOut.flush(); // flush character output stream buffer
            }
        } catch (FileNotFoundException fnfe) {
            try {
                resourceNotFound(headerOut, dataOut, requestedFile);
            } catch (IOException ioe) { }
        } catch (Exception e) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) { }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) { }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (IOException e) { }
            }
            if (headerOut != null) {
                try {
                    headerOut.close();
                } catch (Exception e) { }
            }
            if (dataOut != null) {
                try {
                    dataOut.close();
                } catch (IOException e) { }
            }
            if (clientRequest != null) {
                try {
                    clientRequest.close();
                } catch (IOException e) { }
            }
        }
    }

    /**
     * Method that handles when the file is not found.
     * 
     * @param headerOut Text-output stream that sends a header to the client
     * @param dataOut Byte-output stream that sends files to the client
     * @param requestedFile File that is requested by the client
     * @throws IOException This exception might be thrown
     */
    public void resourceNotFound(PrintWriter headerOut, OutputStream dataOut, File requestedFile) throws IOException {
        File notFoundFile = new File("NotFound.html");
        int fileLength = (int) notFoundFile.length();
        String type = Files.probeContentType(notFoundFile.toPath());
        byte[] notFoundData = readData(notFoundFile, fileLength);

        headerOut.println("HTTP/1.1 404 Not Found");
        headerOut.println("Server: Java HTTP Server");
        headerOut.println("Date: " + new Date());
        headerOut.println("File exists: " + requestedFile.exists());
        headerOut.println("Content-Type: " + type);
        headerOut.println("Content-Length: " + fileLength);
        headerOut.println(); // CRLF
        headerOut.flush(); // flush character output stream buffer

        //sends not found page
        dataOut.write(notFoundData, 0, fileLength);
        dataOut.flush();
    }

    /**
     * Method that reads the data from the file required by the client.
     * 
     * @param reqFile File that is requested by the client
     * @param fileLegth The length of the requested file
     * @return Byte array of the data of the requested file
     * @throws IOException This exception might be thrown
     */
    public byte[] readData(File reqFile, int fileLegth) throws IOException {
        FileInputStream in = null;
        byte[] requestedData = new byte[fileLegth];
        try {
            in = new FileInputStream(reqFile);
            in.read(requestedData);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return requestedData;
    }
}
