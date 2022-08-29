package client;

import util.ClientUtils;
import util.SetupUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private String IP_ADDRESS;
    private int PORT;

    private final Scanner scanner = new Scanner(System.in);

    private boolean isRunning = true;
    private String fileName;

    public Client() {
        try {
            // Create necessary directories for the client
            SetupUtils.setUpFileStorage("/client/data/");
            // Set address and port
            this.IP_ADDRESS = SetupUtils.readProperty("app.address");
            this.PORT = Integer.parseInt(SetupUtils.readProperty("app.port"));
        } catch (IOException e) {
            System.err.println("Server address or port is not specified in the config file!" + e.getMessage());
        }
    }

    public void start() {
        try (
                // Establish the connection
                Socket socket = new Socket(InetAddress.getByName(IP_ADDRESS), PORT);
                // Create streams for communication
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            while (isRunning) {
                System.out.println("Enter action (1 - get a file, 2 - save a file, 3 - delete a file): ");

                // Read the action from the user
                String action = scanner.nextLine();
                String command = processUserAction(action);

                // Send the command to the server
                output.writeUTF(command);
                output.flush();
                if (action.equals("2")) ClientUtils.sendFile(fileName, output);
                System.out.println("The request was sent.");

                // Read the response from the server
                try {
                    String response = input.readUTF();
                    processServerResponse(command, response, input);
                } catch (EOFException e) {
                    System.out.println("The server has been shut down.");
                }

                stop(input, output, socket);
            }

        } catch (IOException e) {
            System.out.println("The server is not available. " + e.getMessage());
        }
    }

    private String processUserAction(String action) {
        String command = "";

        String nameOrId;
        switch (action) {
            case "1" -> {
                command = "GET";
                System.out.print("Do you want to get the file by name or by id (1 - name, 2 - id): ");
                nameOrId = scanner.nextLine();
                if (nameOrId.equals("1")) {
                    nameOrId = "BY_NAME";
                    System.out.print("Enter filename: ");
                } else {
                    nameOrId = "BY_ID";
                    System.out.print("Enter id: ");
                }
                fileName = scanner.nextLine();
                command += " " + nameOrId + " " + fileName;
            }
            case "2" -> {
                command = "PUT";
                System.out.print("Enter filename: ");
                fileName = scanner.nextLine();
                System.out.print("Enter name of the file to be saved on server: ");
                String serverFileName = scanner.nextLine();
                if (serverFileName.trim().length() == 0) serverFileName = "*";
                command += " " + serverFileName;
            }
            case "3" -> {
                command = "DELETE";
                System.out.print("Do you want to delete the file by name or by id (1 - name, 2 - id): ");
                nameOrId = scanner.nextLine();
                if (nameOrId.equals("1")) {
                    nameOrId = "BY_NAME";
                    System.out.print("Enter filename: ");
                } else {
                    nameOrId = "BY_ID";
                    System.out.print("Enter id: ");
                }
                fileName = scanner.nextLine();
                command += " " + nameOrId + " " + fileName;
            }
            case "exit" -> command = "exit";
            default -> System.out.println("Invalid action.");
        }
        return command;
    }

    private void processServerResponse(String command, String response, DataInputStream input) throws IOException {

        String[] responseTokens = response.split(" ");
        String[] commandTokens = command.split(" ");
        String commandName = commandTokens[0];

        switch (responseTokens[0]) {
            case "200":
                switch (commandName) {
                    case "GET" -> ClientUtils.receiveFile(input);
                    case "PUT" -> System.out.println("Response says that file is saved! ID = " + responseTokens[1]);
                    case "DELETE" -> System.out.println("The response says that this file was deleted successfully!");
                }
                break;
            case "403":
                System.out.println("The response says that creating the file was forbidden!");
                break;
            case "404":
                System.out.println("The response says that this file is not found!");
                break;
        }
    }

    public void stop(DataInputStream input, DataOutputStream output, Socket socket) throws IOException {
        // Close streams and socket
        input.close();
        output.close();
        socket.close();
        isRunning = false;
    }
}
