package server;

import util.ServerUtils;
import util.SetupUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {

    private final String IP_ADDRESS;
    private final int PORT;

    private boolean isRunning = true;
    public static HashMap<String, String> idMap;

    public Server() {
        this.IP_ADDRESS = SetupUtils.setUpServerAddress();
        this.PORT = SetupUtils.setUpServerPort();
        SetupUtils.setUpIdMap();
    }

    /**
     * Process the request from the client. The server receives a command string from the client and processes it
     * accordingly. The server then sends a response to the client.
     * The user can terminate the connection by typing "exit" on the client side.
     * This should be avoided in a production environment.
     */
    @SuppressWarnings("unchecked")
    public void start() {
        try (
                // Establish the connection
                ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName(IP_ADDRESS))
        ) {
            System.out.println("Server started!");

            idMap = (HashMap<String, String>) ServerUtils.getIdMap();

            while (isRunning) {
                // Wait for a client to connect
                Socket socket = serverSocket.accept();
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());

                // Read the action from the client
                String request = input.readUTF();
                if (request.equalsIgnoreCase("exit")) {
                    stop(input, output, socket, idMap);
                    break;
                }

                String[] requestTokens = request.split(" ", 3);
                String response = processRequest(requestTokens, input, output, idMap);

                output.writeUTF(response);
                output.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle the request from the client and return the response.
     *
     * @param requestTokens The request from the client as an array of strings to separate the command and the file/id.
     * @param input         The input stream from the client.
     * @param output        The output stream to the client.
     * @param idMap         The map of ids to files to avoid collisions in case of similar file names.
     * @return The response to the client as a string.
     * @throws IOException If an error occurs while reading from the input stream.
     */
    private String processRequest(String[] requestTokens, DataInputStream input,
                                  DataOutputStream output, HashMap<String, String> idMap) throws IOException {
        String response;
        String command = requestTokens[0];
        switch (command) {
            case "PUT" -> {
                response = ServerUtils.addFile(requestTokens, input, idMap);
                output.writeUTF(response);
            }
            case "GET" -> response = ServerUtils.getFile(requestTokens, output, idMap);
            case "DELETE" -> response = ServerUtils.deleteFile(requestTokens, output, idMap);
            default -> response = "Invalid command!";
        }
        return response;
    }

    /**
     * Stop the server.
     *
     * @param input  The input stream from the client.
     * @param output The output stream to the client.
     * @param socket The socket to the client.
     * @param idMap  To save the map before closing the server.
     */
    public void stop(DataInputStream input, DataOutputStream output,
                     Socket socket, HashMap<String, String> idMap) throws IOException {
        // Close streams and socket
        ServerUtils.saveIdMap(idMap);
        input.close();
        output.close();
        socket.close();
        isRunning = false;
    }
}
