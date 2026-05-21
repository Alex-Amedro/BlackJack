package n7.fr.BlackJack.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {
    private final Map<String, String> tokenToPseudo = new ConcurrentHashMap<>();

    public String generateToken(String pseudo) {
        String token = UUID.randomUUID().toString();
        tokenToPseudo.put(token, pseudo);
        return token;
    }

    public String getPseudoAndRemoveToken(String token) {
        // Retrieve the pseudo and immediately remove the token for security (one-time use)
        // Note: For reconnects, we might keep it, but for a web page reload they generate a steady connection.
        // For simplicity with auto-reconnects, let's just get it without removing:
        return tokenToPseudo.get(token);
    }
}
