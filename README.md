# Java File Server

A simple file server that allows sending, reading and deleting files. The original idea was based on a project
on a [Jetbrains Academy](https://hyperskill.org/projects/52) track. I took this further and added the ability to specify the local database path and added
more possibilities to customize the software on your system.

## Requirements:

Make sure that you have a [Java Runtime Environment](https://openjdk.org/install/) installed on your system.
Other than that, just clone the repository and run the `gradle build` command.

## Installation:

```bash
git clone https://github.com/dan-koller/Java-File-Server.git
```

```bash
cd Java-File-Server/
```

```bash
gradle build
```

Now you can run the server*, complete the initial setup and after that run the client.

On the first start you will be asked to specify the default ip and port (or leave them empty to use the default values).

_*) You can use the provided run configuration in `.idea/runConfigurations/` or create your own._

## How to use:

- Start the server
- Start the client
- Select between sending*, reading and deleting files

Note that the server only terminates if a client sends the `exit` command. I only use it for testing purposes. You
should avoid this in a production environment.

_*) Make sure that the files you try to send are located in the `client/data/` folder._

## Technologies:

- Java 17
- Gradle 7.5.1
