package live.yurii.yugram.authorization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthorizationController {

  private final ApplicationEventPublisher publisher;

  @PostMapping("/login")
  public ResponseEntity<String> login() {
    log.debug("Login request received");
    publisher.publishEvent(new LoginRequestEvent(this));
    return ResponseEntity.accepted().body("Login request accepted. POST OTP code to '/auth/otp'");
  }

  @PostMapping(value = "/otp", consumes = "text/plain", produces = "text/plain")
  public ResponseEntity<String> verifyOtp(@RequestBody String code) {
    log.debug("OTP code received: {}", code);
    publisher.publishEvent(new OtpCodeReceivedEvent(this, code));
    return ResponseEntity.accepted().body("OTP code accepted");
  }

  @PostMapping("/logout")
  public ResponseEntity<String> logout() {
    log.debug("Logout request received");
    publisher.publishEvent(new LogoutRequestEvent(this));
    return ResponseEntity.accepted().body("Logout request accepted");
  }
}
