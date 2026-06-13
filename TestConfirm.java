import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;

public class TestConfirm {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        // 1. Get auth token
        HttpRequest loginReq = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:3001/api/auth/login"))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString("{\"username\": \"admin\", \"password\": \"admin123\"}"))
            .build();
        HttpResponse<String> loginRes = client.send(loginReq, HttpResponse.BodyHandlers.ofString());
        String token = loginRes.body().split("token\":\"")[1].split(""")[0];
        
        System.out.println("Token: " + token);
        
        // 2. Confirm Payment for prescription_id = 1
        HttpRequest confirmReq = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:3001/api/payments/1/confirm"))
            .header("Authorization", "Bearer " + token)
            .POST(BodyPublishers.noBody())
            .build();
        HttpResponse<String> confirmRes = client.send(confirmReq, HttpResponse.BodyHandlers.ofString());
        System.out.println("Confirm Response: " + confirmRes.statusCode() + " " + confirmRes.body());
    }
}
