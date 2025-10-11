package live.yurii.yugram.authorization;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OtpCodeReceivedEvent extends ApplicationEvent {

  private final String code;

  public OtpCodeReceivedEvent(AuthorizationController source, String code) {
    super(source);
    this.code = code;
  }
}
