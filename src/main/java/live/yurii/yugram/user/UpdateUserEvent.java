package live.yurii.yugram.user;

import live.yurii.yugram.MainUpdateHandler;
import lombok.Getter;
import org.drinkless.tdlib.TdApi;
import org.springframework.context.ApplicationEvent;

@Getter
public class UpdateUserEvent extends ApplicationEvent {

  private final TdApi.UpdateUser updateUser;

  public UpdateUserEvent(MainUpdateHandler source, TdApi.UpdateUser updateUser) {
    super(source);
    this.updateUser = updateUser;
  }
}
