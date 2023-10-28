import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer {

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(80);
        while (true) {
            Socket client = ss.accept();
            System.out.println("Client con!");
            new Handler(client).start();
        }
    }

    private static class Handler extends Thread {

        private Socket client;
        private BufferedReader reader;
        private PrintWriter writer;

        public Handler(Socket client) throws IOException {
            this.client = client;
            this.reader = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
            this.writer = new PrintWriter(this.client.getOutputStream());
        }

        @Override
        public void run() {
            while (true) {
                try {
                    int charInt;
                    while ((charInt = this.reader.read()) != -1) {
                        System.out.println(charInt);
                    }
                } catch (IOException e) {
                    System.out.println("Client closed!");
                }
            }
        }
    }
    
}
