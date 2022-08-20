package server;

public class Main {

    public static void main(String[] args) {
        new Server("127.0.0.1", 23456).start();
    }
}