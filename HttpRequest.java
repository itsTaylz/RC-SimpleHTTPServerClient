import java.util.Date;
import java.util.HashMap;

/**
 * @author Rodrigo Correia - 58180
 * @author Martim Pereira - 58223
 * @author Daniela Camarinha - 58199
 * 
 * Classe usada para representar um pedido HTTP
 */
public class HttpRequest {

    private static final String LINE_FEED = "\r\n";

    private String method, url, version, body;
    private HashMap<String, String> headers;
    
    /**
     * Cria um novo objeto HttpResquest com os parametros dados
     * @param method O método HTTP do pedido
     * @param url O url do pedido
     * @param version A versão de HTTP a ser usada
     * @param body O corpo do pedido
     * @param headers Os cabeçalhos do pedido
     * @requires {@code method != null && url != null && version != null && body != null && headers != null}
     */
    public HttpRequest(String method, String url, String version, String body, HashMap<String, String> headers) {
        this.method = method;
        this.url = url;
        this.version = version;
        this.body = body;
        this.headers = headers;
    }

    /**
     * Cria um objeto HttpRequest com os parametros dados e os cabeçalhos standard usados
     * @param method O método HTTP do pedido
     * @param url O url do pedido
     * @param version Versão do HTTP usada no pedido
     * @param body Corpo do pedido
     * @param host O host a ser colocado no cabeçalho
     * @return HttpRequest com os parametros dados
     * @requires {@code method != null && url != null && version != null && body != null && host != null}
     * @ensures {@code \result != null}
     */
    public static HttpRequest createDefaultHeaders(String method, String url, String version, String body, String host) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Length", body.length() + "");
        headers.put("Date", new Date().toString());
        headers.put("Connection", "keep-alive");
        headers.put("Host", host);
        HttpRequest request = new HttpRequest(method, url, version, body, headers);
        return request;
    }

    /**
     * Define/adiciona um valor de cabeçalho do pedido
     * @param key O nome do campo de cabeçalho
     * @param value O valor do campo
     * @return O próprio HttpRequest com o cabeçalho alterado
     * @requires {@code key != null && value != null}
     */
    public HttpRequest setHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }
    /**
     * Transforma uma string num objeto HttpRequest
     * @param text A string a ser transformada
     * @return HttpRequest caso a string seja um pedido bem formatada, null caso contrário
     * @requires {@code text != null}
     */
    public static HttpRequest parseRequest(String text) {
        String[] lines = text.split("\n");
        String[] requestLine = lines[0].split(" ");
        if (requestLine.length == 3) {
            String method = requestLine[0];
            String url = requestLine[1];
            String version = requestLine[2];
            if (version.equals("HTTP/1.1\r") || version.equals("HTTP/1.0\r")) {
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
                if (lines[index+1].equals("\r")) {
                    StringBuilder bodyBuilder = new StringBuilder();
                    for (int i = index + 2; i < lines.length; i++) {
                        bodyBuilder.append(lines[i]);
                        if (i != lines.length - 1 || text.endsWith("\n"))
                            bodyBuilder.append('\n');
                    }
                    return new HttpRequest(method, url, version, bodyBuilder.toString(), headers);
                }
            }
        }
        return null;
    }

    /**
     * Cria uma string que representa um pedido Http mal formatado
     * @param type O tipo de http mal formatado
     * @param host O host a ser usado no cabeçalho
     * @return Representação textual de um pedido HTTP mal formatado
     * @ensures {@code \result != null}
     * @requires {@code type >= 1 && type <= 3 && host != null}
     */
    public static String createBadRequest(int type, String host) {
        String request = "";
        switch (type) {
            case 1:
                request = HttpRequest.createDefaultHeaders("GET", "/index.html", "HTTP/1.1", "", host).toString().replaceFirst("\r\n", "");
                break;
            case 2:
                request = HttpRequest.createDefaultHeaders("GET ", "/index.html ", "HTTP/1.1", "", host).toString();
                break;
            case 3:
                request = HttpRequest.createDefaultHeaders("GET", "/index.html", "", "", host).toString();
                break;
        }
        return request;
    }

    /**
     * Converte o objeto HttpRequest para uma representação textual de um pedido HTTP
     * @ensures {@code \result != null}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(method + " " + url + " " + version + LINE_FEED);
        for (String key : this.headers.keySet()) {
            sb.append(key + ": " + this.headers.get(key) + LINE_FEED);
        }
        sb.append(LINE_FEED);
        sb.append(body);
        return sb.toString();
    }

    /**
     * Retorna o método do pedido HTTP
     * @return O método do pedido HTTP
     */
    public String getMethod() {
        return method;
    }

    /**
     * Retorna o url do pedido HTTP
     * @return O url do pedido HTTP
     */
    public String getUrl() {
        return url;
    }

    /**
     * Retorna um hashmap contendo os cabeçalhos e valores do pedido HTTP
     * @return O hashmap com os cabeçalhos http
     */
    public HashMap<String, String> getHeaders() {
        return headers;
    }

    /**
     * Retorna a versão do pedido HTTP
     * @return A versão do pedido HTTP
     */
    public String getVersion() {
        return version;
    }

    /**
     * Retorna o corpo do pedido HTTP
     * @return O corpo do pedido HTTP
     */
    public String getBody() {
        return body;
    }

}
