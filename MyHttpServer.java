import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Rodrigo Correia - 58180
 * @author Martim Pereira - 58223
 * @author Daniela Camarinha - 58199
 * 
 * Classe servidor que irá receber pedidos HTTP de clientes e enviar as devidas respostas
 */
public class MyHttpServer {

    private final ServerSocket serverSocket;
    private final List<ClientHandler> ACTIVE_CLIENTS = new CopyOnWriteArrayList<>();

    /**
     * Cria e inicia um servidor socket
     * @param port A porta a ser usada pelo servidor
     * @throws IOException Caso não seja possivel criar o servidor
     */
    public MyHttpServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        int clientId = 1;
        while (!this.serverSocket.isClosed()) {
            Socket client = this.serverSocket.accept();
            ClientHandler handler = new ClientHandler(this.serverSocket, client, clientId);
            Thread thread = new Thread(handler);
            thread.setName("ClientThread-" + clientId);
            thread.start();
            clientId++;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Invalid arguments. Please use only the port of the server has the startup argument.");
        } else {
            try {
                int port = Integer.parseInt(args[0]);
                new MyHttpServer(port);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port!");
            }
        }
    }

    /**
     * Classe privada usada para executar o codigo de fornecer um cliente num thread separado
     */
    private class ClientHandler implements Runnable {

        private final ServerSocket serverSocket;
        private final Socket client;
        private final int id;
        private final BufferedReader reader;
        private final PrintWriter writer;
        private boolean shouldClose = false;

        /**
         * Inicializa a classe 
         * @param serverSocket O socket do servidor
         * @param client O socket que vai ser o cliente nesta classe
         * @param id O id do thread
         * @throws IOException Caso não seja possivel criar BufferedReader ou PrintWriter para o cliente
         * @requires {@code serverSocket != null && client != null}
         */
        public ClientHandler(ServerSocket serverSocket, Socket client, int id) throws IOException {
            this.id = id;
            this.serverSocket = serverSocket;
            this.client = client;
            this.reader = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
            this.writer = new PrintWriter(this.client.getOutputStream());
            ACTIVE_CLIENTS.add(this);
            System.out.println("\nClient #" + this.id + " connected!\n");
        }

        /**
         * O código neste método será executado num thread separado do principal e vai ler todos os pedidos efetuados pelo
         * cliente e fornecer as respetivas respostas
         */
        @Override
        public void run() {
            while (!this.serverSocket.isClosed() && !this.shouldClose) {
                try {
                    StringBuilder sb = new StringBuilder();
                    int readCode;
                    while ((readCode = this.reader.read()) != -1 && this.reader.ready()) {
                        sb.append((char) readCode);
                    }
                    if (readCode != -1)
                        sb.append((char) readCode);
                    if (readCode == -1)
                        this.shouldClose = true;
                    else if (sb.length() != 0) {
                        String requestText = sb.toString();
                        System.out.println("Request from client #" + this.id + ":\n\n" + requestText);
                        HttpRequest request = HttpRequest.parseRequest(requestText);
                        HttpResponse response = HttpResponse.createDefaultHeaders("HTTP/1.1", 400, "Bad Request", "");
                        if (ACTIVE_CLIENTS.indexOf(this) > 4) {
                            response = HttpResponse.createDefaultHeaders("HTTP/1.1", 503, "Service unavailable", "");
                        } else if (request != null) {
                            if (request.getMethod().equals("GET")) {
                                if (request.getUrl().equals("/index.html") || request.getUrl().equals("/"))
                                    response = handleGetRequest(request);
                                else
                                    response = HttpResponse.createDefaultHeaders("HTTP/1.1", 404, "Not Found", "");
                            } else if (request.getMethod().equals("POST") && request.getUrl().equals("/simpleForm.html")) {
                                System.out.println("POST");
                                HttpResponse postResponse = handlePostRequest(request);
                                if (postResponse != null)
                                    response = postResponse;
                            } else if (!request.getMethod().equals("POST")) {
                                response = HttpResponse.createDefaultHeaders("HTTP/1.1", 501, "Not Implemented", "");
                            }
                        }
                        sendResponse(response);
                    }
                } catch (IOException e) {
                    this.shouldClose = true;
                }
            }
            System.out.println("\nClient #" + this.id + " disconnected!\n");
            ACTIVE_CLIENTS.remove(this);
            this.shouldClose = true;
            try {
                this.writer.close();
                this.reader.close();
                this.client.close();
            } catch (IOException e) {
                // ignored
            }
        }

        /**
         * Esta função irá ler o ficheiro index.html e criar uma resposta http para enviá-lo para o cliente
         * @param request O pedido HTTP do cliente
         * @return HttpResponse contendo os cabeçalhos standard e no corpo o código html presente no ficheiro lido
         * @ensures {@code \result != null}
         * @requires {@code request != null}
         * @throws IOException Caso não seja possivel ler o ficheiro
         */
        private HttpResponse handleGetRequest(HttpRequest request) throws IOException {
            File htmlFile = new File("index.html");
            Date lastModified = new Date(htmlFile.lastModified());
            String fileContent = readAllTextFile(htmlFile);
            return HttpResponse.createDefaultHeaders("HTTP/1.1", 200, "OK", fileContent)
                .setHeader("Content-Type", "text/html")
                .setHeader("Last-Modified", lastModified.toString());
        }

        /**
         * Esta função irá ler todo o texto num ficheiro
         * @param file O ficheiro a ser lido
         * @return String contendo todo o texto lido do ficheiro
         * @throws IOException Caso não seja possivel ler o ficheiro
         * @requires {@code file != null}
         * @ensures {@code \result != null}
         */
        private String readAllTextFile(File file) throws IOException {
            StringBuilder sb = new StringBuilder();
            try (Scanner sc = new Scanner(file)) {
                while (sc.hasNextLine())
                    sb.append(sc.nextLine()).append("\r\n");
            }
            return sb.toString();
        }

        /**
         * Esta função irá receber o pedido POST do cliente e verificar se o cabeçalho Content-Length é valido, 
         * retornado uma HttpResponse caso isso se verifique
         * @param request O pedido HTTP do cliente
         * @return HttpResponse de uma resposta de sucesso caso o pedido esteja bem formatado, null caso contrário
         * @requires {@code request != null}
         */
        private HttpResponse handlePostRequest(HttpRequest request) {
            String contentLengthStr = request.getHeaders().get("Content-Length");
            if (contentLengthStr != null) {
                System.out.println("Has length");
                try {
                    int contentLength = Integer.parseInt(contentLengthStr);
                    System.out.println("CC: " + contentLength + " | " + request.getBody().length());
                    if (request.getBody().length() == contentLength) {
                        return HttpResponse.createDefaultHeaders("HTTP/1.1", 200, "OK", "");
                    }
                } catch (NumberFormatException e) { }
            }
            return null;
        }

        /**
         * Este método vai enviar a resposta HTTP para o cliente usando o PrintWriter 
         * @param response A resposta HTTP a ser enviada
         * @requires {@code response != null}
         */
        private void sendResponse(HttpResponse response) {
            this.writer.print(response.toString());
            this.writer.flush();
        }
    }
}
