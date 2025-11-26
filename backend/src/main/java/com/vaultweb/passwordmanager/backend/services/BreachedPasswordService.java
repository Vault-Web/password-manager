package com.vaultweb.passwordmanager.backend.services;

import com.vaultweb.passwordmanager.backend.exceptions.PasswordBreachCheckException;
import java.security.MessageDigest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author rashmi.soni
 */
@Service
public class BreachedPasswordService {

  private final WebClient webClient = WebClient.create("https://api.pwnedpasswords.com/range/");

  public int checkIfBreached(String password) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-1");
      byte[] hashBytes = digest.digest(password.getBytes());
      StringBuilder sb = new StringBuilder();
      for (byte b : hashBytes) sb.append(String.format("%02X", b));
      String hash = sb.toString();

      String prefix = hash.substring(0, 5);
      String suffix = hash.substring(5);

      String response = webClient.get().uri(prefix).retrieve().bodyToMono(String.class).block();

      if (response != null) {
        for (String line : response.split("\r\n")) {
          String[] parts = line.split(":");
          if (parts[0].equalsIgnoreCase(suffix)) {
            return Integer.parseInt(parts[1]);
          }
        }
      }
    } catch (Exception ignored) {
      throw new PasswordBreachCheckException("Failed to check password breach. Try again later.");
    }
    return 0;
  }
}
