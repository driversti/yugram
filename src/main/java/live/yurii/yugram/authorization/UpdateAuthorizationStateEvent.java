package live.yurii.yugram.authorization;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;
import org.springframework.context.ApplicationEvent;

@Slf4j
@Getter
public class UpdateAuthorizationStateEvent extends ApplicationEvent {
  private final TdApi.UpdateAuthorizationState state;

  public UpdateAuthorizationStateEvent(Object source, TdApi.UpdateAuthorizationState update) {
    super(source);
    this.state = update;
  }
}
