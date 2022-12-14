package util;

import server.Server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class ServerUtils {

    private static final String FILE_PATH = SetupUtils.setUpFileStorage("/server/data/");
    private static final String ID_MAP_PATH = SetupUtils.setUpFileStorage("/server/config/") + "map.bin";

    /**
     * The object representing the map is serialized and saved to the hard drive.
     * It represents a hashmap that stores file names and their ids.
     *
     * @param obj The id map to be saved.
     */
    public static void saveIdMap(Object obj) {
        try {
            FileOutputStream fos = new FileOutputStream(ID_MAP_PATH);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The id map is deserialized and returned.
     *
     * @return The id map as hashmap or null if it doesn't exist.
     */
    public static Object getIdMap() {
        Object obj;

        try {
            FileInputStream fis = new FileInputStream(ID_MAP_PATH);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No id map found! Creating new one...");
            obj = new HashMap<>();
            saveIdMap(obj);
        }

        return obj instanceof HashMap ? obj : new HashMap<>();
    }

    /**
     * Send a file to the client.
     *
     * @param requestTokens The tokens of the request representing the user's request.
     * @param output        The output stream to the client.
     * @param idMap         The id map.
     * @return The response to the client (200 if the file was sent, 404 if it wasn't).
     */
    public static String getFile(String[] requestTokens, DataOutputStream output, HashMap<String, String> idMap) {
        String filename, response = "";
        try {
            if (requestTokens[1].equals("BY_ID")) {
                if (idMap.containsKey(requestTokens[2])) filename = idMap.get(requestTokens[2]);
                else {
                    response = "404";
                    output.writeUTF(response);
                    return response;
                }
            } else {
                filename = requestTokens[2];
            }
            String path = FILE_PATH + filename;
            File f = new File(path);

            if (f.exists() && !f.isDirectory()) {
                response = "200";
                output.writeUTF(response);
                byte[] fileBytes = Files.readAllBytes(Paths.get(path));
                output.writeInt(fileBytes.length);
                output.write(fileBytes);
                output.flush();
            } else {
                response = "404";
                output.writeUTF(response);
            }
        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        }

        return response;
    }

    /**
     * Receive a file from the client.
     *
     * @param requestTokens The tokens of the request representing the user's request.
     * @param input         The input stream from the client.
     * @param idMap         The id map.
     * @return The response to the client (200 if the file was added, 403 if it wasn't).
     */
    public static String addFile(String[] requestTokens, DataInputStream input, HashMap<String, String> idMap) {
        String response = "";
        try {
            String filename;
            String fileId = System.currentTimeMillis() + "";

            if (requestTokens[1].equals("*")) filename = fileId + ".dat";
            else filename = requestTokens[1];

            String path = FILE_PATH + filename;
            File f = new File(path);

            // Read in file size, file data
            int size = input.readInt();
            byte[] fileBytes = new byte[size];
            input.readFully(fileBytes, 0, fileBytes.length);

            if (!f.exists() && !f.isDirectory()) {
                Files.write(Paths.get(path), fileBytes);
                response = "200 " + fileId;

                // Update idMap with new file id and filename
                idMap.put(fileId, filename);
                Server.idMap = idMap;
            } else {
                response = "403";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    /**
     * Delete a file from the server.
     *
     * @param requestTokens The tokens of the request representing the user's request.
     * @param output        The output stream to the client.
     * @param idMap         The id map.
     * @return The response to the client (200 if the file was deleted, 404 if it wasn't).
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String deleteFile(String[] requestTokens, DataOutputStream output, HashMap<String, String> idMap) {
        String response = "";
        try {
            String filename, fileId;
            if (requestTokens[1].equals("BY_ID")) {
                if (idMap.containsKey(requestTokens[2])) {
                    filename = idMap.get(requestTokens[2]);
                    fileId = requestTokens[2];
                } else {
                    response = "404";
                    output.writeUTF(response);
                    return response;
                }
            } else {
                filename = requestTokens[2];
                fileId = findById(idMap, requestTokens[2]);
            }

            String path = FILE_PATH + filename;
            File f = new File(path);

            if (f.exists() && !f.isDirectory()) {
                f.delete();
                idMap.remove(fileId);
                response = "200";
            } else {
                response = "404";
            }
            output.writeUTF(response);
        } catch (IOException e) {
            System.out.println("Error deleting the file: " + e.getMessage());
        }
        return response;
    }

    /**
     * Find the file id in the id map in a linear search.
     *
     * @param idMap The id map.
     * @param key   The key to search for.
     * @return The file id or and empty string if it wasn't found.
     */
    public static String findById(HashMap<String, String> idMap, String key) {
        String value = "";
        for (String id : idMap.keySet()) {
            if (id.equals(key)) {
                value = idMap.get(id);
                break;
            }
        }
        return value;
    }
}
