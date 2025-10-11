package live.yurii.yugram.authorization;

import org.springframework.context.ApplicationEvent;

public class LogoutRequestEvent extends ApplicationEvent {
  public LogoutRequestEvent(AuthorizationController source) {
    super(source);
  }
}
