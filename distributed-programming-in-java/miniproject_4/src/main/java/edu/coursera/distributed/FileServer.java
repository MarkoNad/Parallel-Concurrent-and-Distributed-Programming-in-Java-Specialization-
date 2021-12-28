package edu.coursera.distributed;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
     * @param ncores The number of cores that are available to your
     *               multi-threaded file server. Using this argument is entirely
     *               optional. You are free to use this information to change
     *               how you create your threads, or ignore it.
     * @throws IOException If an I/O error is detected on the server. This
     *                     should be a fatal error, your file server
     *                     implementation is not expected to ever throw
     *                     IOExceptions during normal operation.
     */
    public void run(
            final ServerSocket serverSocket,
            final PCDPFilesystem filesystem,
            final int ncores) throws IOException {

        ExecutorService executorService = Executors.newFixedThreadPool(ncores);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            Runnable task = createRequestProcessingTask(clientSocket, filesystem);
            executorService.submit(task);
        }
    }

    private Runnable createRequestProcessingTask(Socket clientSocket, PCDPFilesystem filesystem) {
        return () -> {
            try (
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
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
