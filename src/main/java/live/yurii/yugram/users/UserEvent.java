package live.yurii.yugram.users;

import live.yurii.yugram.MainUpdateHandler;
import lombok.Getter;
import org.drinkless.tdlib.TdApi;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserEvent extends ApplicationEvent {

  private final TdApi.UpdateUser updateUser;

  public UserEvent(MainUpdateHandler source, TdApi.UpdateUser updateUser) {
    super(source);
    this.updateUser = updateUser;
  }
}
