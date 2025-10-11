package live.yurii.yugram.authorization;

import org.springframework.context.ApplicationEvent;

public class LoginRequestEvent extends ApplicationEvent {
  public LoginRequestEvent(AuthorizationController source) {
    super(source);
  }
}
