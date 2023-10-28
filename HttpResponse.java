import java.util.Date;
import java.util.HashMap;

/**
 * @author Rodrigo Correia - 58180
 * @author Martim Pereira - 58223
 * @author Daniela Camarinha - 58199
 * 
 * Classe usada para representar uma resposta HTTP
 */
public class HttpResponse {

    private static final String LINE_FEED = "\r\n";

    private String message, version, body;
    private int statusCode;
    private HashMap<String, String> headers;
    
    /**
     * Cria um novo objeto HttpResponse com os parametros dados
     * @param version A versão de HTTP a ser usada
     * @param statusCode O código de estado da resposta
     * @param message A mensagem associada ao código da resposta
     * @param body O corpo da resposta
     * @param headers Os cabeçalhos da resposta
     * @requires {@code version != null && message != null && body != null && headers != null}
     */
    public HttpResponse(String version, int statusCode, String message, String body, HashMap<String, String> headers) {
        this.message = message;
        this.statusCode = statusCode;
        this.version = version;
        this.body = body;
        this.headers = headers;
        this.headers.put("Content-Length", body.length() + "");
    }

    /**
     * Cria um objeto HttpResponse com os parametros dados e os cabeçalhos standard usados
     * @param version Versão do HTTP usada na resposta
     * @param statusCode Código de estado da resposta
     * @param message Mensagem do resposta
     * @param body Corpo da resposta
     * @return HttpResponse com os parametros dados
     * @requires {@code version != null && message != null & body != null}
     * @ensures {@code \result != null}
     */
    public static HttpResponse createDefaultHeaders(String version, int statusCode, String message, String body) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Length", body.length() + "");
        headers.put("Date", new Date().toString());
        headers.put("Connection", "keep-alive");
        headers.put("Server", "MyHttpServer");
        HttpResponse response = new HttpResponse(version, statusCode, message, body, headers);
        return response;
    }

    /**
     * Define/adiciona um valor de cabeçalho ao resposta
     * @param key O nome do campo de cabeçalho
     * @param value O valor do campo
     * @return O próprio HttpResponse com o cabeçalho alterado
     * @requires {@code key != null && value != null}
     */
    public HttpResponse setHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    /**
     * Transforma uma string num objeto de HttpResponse
     * @param text A string a ser transformada
     * @return HttpResponse com os atributos lidos da string
     * @requires {@code text != null}
     * @ensures {@code \result != null}
     */
    public static HttpResponse parseResponse(String text) {
        String[] lines = text.split("\n");
        String[] requestLine = lines[0].split(" ", 3);
        String version = requestLine[0];
        int statusCode = Integer.parseInt(requestLine[1]);
        String message = requestLine[2];
        HashMap<String, String> headers = new HashMap<>();
        int index = 0;
        for (int i = 1; i < lines.length && !lines[i].equals("\r"); i++) {
            String[] headerLine = lines[i].split(": ", 2);
            if (!headerLine[1].endsWith("\r"))
                return null;
            String header = headerLine[0].substring(0, headerLine[0].length());
            String value = headerLine[1].substring(0, headerLine[1].length() - 1);
            headers.put(header, value);
            index = i;
        }
        StringBuilder bodyBuilder = new StringBuilder();
        for (int i = index + 2; i < lines.length; i++) {
            bodyBuilder.append(lines[i]);
            if (i != lines.length - 1 || text.endsWith("\n"))
                bodyBuilder.append('\n');
        }
        return new HttpResponse(version, statusCode, message, bodyBuilder.toString(), headers);
    }

    /**
     * Converte o objeto HttpResponse para uma representação textual de uma resposta HTTP
     * @ensures {@code \result != null}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(version + " " + statusCode + " " + message + LINE_FEED);
        for (String key : this.headers.keySet()) {
            sb.append(key + ": " + this.headers.get(key) + LINE_FEED);
        }
        sb.append(LINE_FEED);
        sb.append(body);
        return sb.toString();
    }

}
