# Java File Server

A simple file server that allows sending, reading and deleting files. The original idea was based on a project
on a [Jetbrains Academy](https://hyperskill.org/projects/52) track. I took this further and added the ability to specify
the local database path and added more possibilities to customize the software on your system.

## Requirements:

Make sure that you have a [Java Runtime Environment](https://openjdk.org/install/) installed on your system.
If you want to build the app yourself, clone the repository and run the `gradle build` command (
see [build section](#build)). If you downloaded the pre-built jar file, you can run it with the `java -jar`
(see [setup section](#setup)).

### Build:

```bash
git clone https://github.com/dan-koller/Java-File-Server.git
```

```bash
cd Java-File-Server/
```

```bash
gradle build
```

### Setup:

I assume that you know how to use the command line.

1. Start the client to initialize and create the folder structure (only on first start)
    ```bash
    java -jar client.jar
    ```

2. Start the server
    ```bash
    java -jar server.jar
    ```
   _On the first start you will be asked to specify the default ip and port (or leave them empty to use the default
   values)._

3. Copy the `app.config` file from the server to the client root directory
    ```bash
    cp server/app.config client/
    ```

4. Run the client (see [usage section](#usage))

## Usage:

- Start the server (see above)
- Start the client (see above)
- Select between sending, reading and deleting files

_Make sure that the files you try to send are located in the `client/data/` folder. You can find the received files
there as well. The server will store and send files that are located in the `server/data/` directory. The file id map is
located in `server/config/`._

Note that the server only terminates if a client sends the `exit` command. I only use it for testing purposes. You
should avoid this in a production environment.

## Technologies:

- Java 17
- Gradle 7.5.1
