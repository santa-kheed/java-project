import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleCDN {

    // Cache to store content (URL -> content)
    private static final Map<String, byte[]> cache = new HashMap<>();

    // Thread pool for handling concurrent requests
    private static final ExecutorService executor = Executors.newFixedThreadPool(10); // Adjust pool size as needed

    public static void main(String[] args) throws IOException {
        int port = 8080; // Port to listen on
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("CDN Node started on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            executor.submit(() -> handleClient(clientSocket)); // Handle each client in a separate thread
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream()
        ) {
            String requestLine = in.readLine();
            if (requestLine == null) return; // Handle empty requests

            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) return; // Basic request validation

            String method = requestParts[0];
            String url = requestParts[1];

            if (method.equals("GET")) {
                byte[] cachedContent = cache.get(url);
                if (cachedContent != null) {
                    // Serve from cache
                    System.out.println("Serving from cache: " + url);
                    sendResponse(out, "HTTP/1.1 200 OK\r\nContent-Length: " + cachedContent.length + "\r\n\r\n", cachedContent);
                } else {
                    // Fetch from origin (simplified)
                    System.out.println("Fetching from origin: " + url);
                    byte[] originContent = fetchFromOrigin(url);
                    if (originContent != null) {
                        cache.put(url, originContent); // Cache the fetched content
                        sendResponse(out, "HTTP/1.1 200 OK\r\nContent-Length: " + originContent.length + "\r\n\r\n", originContent);
                    } else {
                        sendResponse(out, "HTTP/1.1 404 Not Found\r\n\r\n", "404 Not Found".getBytes());
                    }
                }
            } else {
                sendResponse(out, "HTTP/1.1 400 Bad Request\r\n\r\n", "400 Bad Request".getBytes());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static byte[] fetchFromOrigin(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == 200) {
                try (InputStream inputStream = connection.getInputStream();
                     ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    return outputStream.toByteArray();
                }
            } else {
                return null; // Origin returned non-200
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void sendResponse(OutputStream out, String headers, byte[] content) throws IOException {
        out.write(headers.getBytes());
        out.write(content);
        out.flush();
    }
}