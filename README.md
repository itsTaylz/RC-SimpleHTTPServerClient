# RC-SimpleHTTPServerClient

This repository hosts the code for the project of my computer networks class, the goal of the project was to make a simple server that receives, parses and responds to http requests coming from the created client. The server allows up to 5 clients connected at a time, both sending and receiving messages, though multithreading.

# Compilation

The project contains both the server and the client

To compile the server simply run:
```bash
$ javac MyHttpServer.java
```

To complite the client run:
```bash
$ javac TestMP1.java
```

The client wasn´t designed to be run by itself, only through the `TestMP1.java` file provided.

# Running

To run the server you only need to specify the port number for the socket
```bash
$ java MyHttpServer <port>
```

To run the client use:
```bash
$ java TestMP1 <hostname> <port>
``` 
