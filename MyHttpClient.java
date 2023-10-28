import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author Rodrigo Correia - 58180
 * @author Martim Pereira - 58223
 * @author Daniela Camarinha - 58199
 * 
 * Classe cliente usada para comunicar com o servidor
 */
public class MyHttpClient {

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private String hostName;

    /**
     * Construtor da classe que aceita o nome do servidor e o número da porto destino TCP
     * @param hostName nome servidor
     * @param portNumber número porto destino TCP
     * @throws IOException
     */
    public MyHttpClient(String hostName, int portNumber) throws IOException {
        this.socket = new Socket(hostName, portNumber);
        this.writer = new PrintWriter(this.socket.getOutputStream());
        this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.hostName = hostName;
    }

    /**
     * Envia um pedido GET HTTP para obter o objeto indicado pelo parâmetro
     * @param ObjectName nome do objeto a obter
     * @throws IOException
     * @requires {@code ObjectName != null}
     */
    public void getResource(String ObjectName) throws IOException {
        sendRequest(HttpRequest.createDefaultHeaders("GET", "/" + ObjectName, "HTTP/1.1", "", this.hostName).toString());
        readResponse();
    }

    /**
     * Envia um pedido POST HTTP para a página hipotética "/simpleForm.html" hospedada pelo servidor
     * que contém formulário web com dois campos, StudentName e StudentID a serem preenchidos
     * @param data lista que contêm os dados do formulário
     * @throws IOException
     * @requires {@code data != null}
     */ 
    public void postData(String[] data) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            String[] dataArray = data[i].split(": ", 2);
            String key = dataArray[0];
            String value = dataArray[1];
            sb.append(key).append('=').append(value);
            if (i != data.length - 1)
                sb.append('&');
        }
        HttpRequest request = HttpRequest.createDefaultHeaders("POST", "/simpleForm.html", "HTTP/1.1", sb.toString(), this.hostName);
        sendRequest(request.toString());
        readResponse();
    }

    /**
     * Envia um pedido HTTP com um nome de método não suportado pelo servidor
     * @param wrongMethodName nome do método não suportado
     * @throws IOException
     * @requires {@code wrongMethodName != null && wrongMethodName != GET && wrongMethodName != POST}
     */
    public void sendUnimplementedMethod(String wrongMethodName) throws IOException {
        sendRequest(HttpRequest.createDefaultHeaders(wrongMethodName, "/index.html", "HTTP/1.1", "", this.hostName).toString());
        readResponse();
    }

    /**
     * Envia pedidos GET HTTP mal formatados de três tipos diferentes:
     * 1 - caractere ‘\r\n’ ausente depois da linha de pedido;
     * 2 - presença de caracteres de espaço adicionais entre os campos da linha de pedido;
     * 3 - campo versão HTTP ausente na linha de pedido.
     * @param type inteiro usado para identificar os diferentes problemas de formataçãp
     * @throws IOException
     * @requires {@code type == 1 || type == 2 || type == 3}
     */
    public void malformedRequest(int type) throws IOException {
        sendRequest(HttpRequest.createBadRequest(type, this.hostName));
        readResponse();
    }

    /**
     * Fecha o canal de comunicação socket com o servidor
     */
    public void close() {
        try {
            this.writer.close();
            this.reader.close();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Envia um pedido HTTP dada uma string que o representa
     * @param request string que representa o pedido
     * @throws IOException
     * @requires {@code request != null}
     */
    private void sendRequest(String request) throws IOException {
        this.writer.write(request);
        this.writer.flush();
    }

    /**
     * Lê a resposta ao pedido HTTP imprimindo-a na consola (stdout)
     * @throws IOException
     */
    private void readResponse() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (sb.length() == 0 || reader.ready()) {
            char c = (char) reader.read();
            sb.append(c);
        }
        HttpResponse response = HttpResponse.parseResponse(sb.toString());
        if (response != null)
            System.out.println("\n" + response.toString());
    }
    
}