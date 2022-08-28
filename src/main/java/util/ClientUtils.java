package util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class ClientUtils {

    private static final String FILE_PATH = SetupUtils.setUpFileStorage("/client/data/");

    public static void sendFile(String fileName, DataOutputStream output) throws IOException {
        String path = FILE_PATH + fileName;
        byte[] fileBytes = Files.readAllBytes(Paths.get(path));

        output.writeInt(fileBytes.length);
        output.write(fileBytes);
        output.flush();
    }

    public static void receiveFile(DataInputStream input) throws IOException {
        int size = input.readInt();
        byte[] fileBytes = new byte[size];
        input.readFully(fileBytes, 0, fileBytes.length);

        System.out.print("The file was downloaded! Specify a name for it: ");
        Scanner scanner = new Scanner(System.in);
        String saveName = scanner.nextLine();

        String path = FILE_PATH + saveName;
        Files.write(Paths.get(path), fileBytes);
        System.out.println("File saved on the hard drive!");
    }
}
