package edu.coursera.distributed;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A basic and very limited implementation of a file server that responds to GET
 * requests from HTTP clients.
 */
public final class FileServer {
    /**
     * Main entrypoint for the basic file server.
     *
     * @param serverSocket Provided socket to accept connections on.
     * @param filesystem A proxy filesystem to serve files from. See the PCDPFilesystem
     *           class for more detailed documentation of its usage.
     * @throws IOException If an I/O error is detected on the server. This
     *                     should be a fatal error, your file server
     *                     implementation is not expected to ever throw
     *                     IOExceptions during normal operation.
     */
    public void run(final ServerSocket serverSocket, final PCDPFilesystem filesystem)
            throws IOException {
        /*
         * Enter a spin loop for handling client requests to the provided
         * ServerSocket object.
         */
        while (true) {
            try (
                    Socket clientSocket = serverSocket.accept();
                    InputStream inStream = clientSocket.getInputStream();
                    InputStreamReader streamReader = new InputStreamReader(inStream);
                    BufferedReader bufferedReader = new BufferedReader(streamReader);
                    OutputStream outStream = clientSocket.getOutputStream();
                    PrintWriter printWriter = new PrintWriter(outStream)
            ) {
                String firstLine = bufferedReader.readLine();

                String[] lineParts = firstLine.split(" ");
                String method = lineParts[0];
                String pathString = lineParts[1];

                PCDPPath path = new PCDPPath(pathString);
                String contents = filesystem.readFile(path);
                writeResponse(method, contents, printWriter);
            }
        }
    }

    private void writeResponse(String method, String contents, PrintWriter printWriter) {
        if (method.equals("GET") && contents != null) {
            writeFile(contents, printWriter);
        } else {
            writeError(printWriter);
        }
    }

    private void writeFile(String contents, PrintWriter printWriter) {
        printWriter.write("HTTP/1.0 200 OK");
        printWriter.write("\r\n");
        printWriter.write("Server: FileServer");
        printWriter.write("\r\n");
        printWriter.write("\r\n");
        printWriter.write(contents);
    }

    private void writeError(PrintWriter printWriter) {
        printWriter.write("HTTP/1.0 404 Not Found");
        printWriter.write("\r\n");
        printWriter.write("Server: FileServer");
        printWriter.write("\r\n");
        printWriter.write("\r\n");
    }

}
