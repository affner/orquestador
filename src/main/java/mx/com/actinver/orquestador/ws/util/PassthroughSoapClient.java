package mx.com.actinver.orquestador.ws.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class PassthroughSoapClient {

    private final String endpointUrl;
    private final int    connectTimeoutMs;
    private final int    readTimeoutMs;
    private final Map<String,String> extraHeaders;

    public PassthroughSoapClient(String endpointUrl) {
        this(endpointUrl, 15000, 30000, Collections.<String,String>emptyMap());
    }

    public PassthroughSoapClient(String endpointUrl,
                                 int connectTimeoutMs,
                                 int readTimeoutMs,
                                 Map<String,String> extraHeaders) {
        this.endpointUrl     = endpointUrl;
        this.connectTimeoutMs= connectTimeoutMs;
        this.readTimeoutMs   = readTimeoutMs;
        this.extraHeaders    = extraHeaders;
    }

    /** Envía XML y devuelve la respuesta (sin parsear). */
    public String invokeRaw(String xml) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(endpointUrl).openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setConnectTimeout(connectTimeoutMs);
        con.setReadTimeout(readTimeoutMs);
        con.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
        con.setRequestProperty("Accept",       "text/xml");

        for (Map.Entry<String,String> e : extraHeaders.entrySet()) {
            con.setRequestProperty(e.getKey(), e.getValue());
        }

        try (OutputStream os = con.getOutputStream()) {
            os.write(xml.getBytes(StandardCharsets.UTF_8));
        }

        int code = con.getResponseCode();
        InputStream is = (code >= 200 && code < 300)
                ? con.getInputStream()
                : con.getErrorStream();

        String resp = readFully(is);

        if (code >= 400) {
            // opcional: propagar como Fault genérico
            throw new IOException("HTTP "+code+": "+resp);
        }
        return resp;
    }

    private static String readFully(InputStream in) throws IOException {
        if (in == null) return "";
        StringBuilder sb = new StringBuilder(4096);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            char[] buf = new char[4096];
            int n;
            while ((n = br.read(buf)) != -1) sb.append(buf, 0, n);
        }
        return sb.toString();
    }
}
