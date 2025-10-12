package live.yurii.yugram.messages;

import live.yurii.yugram.MainUpdateHandler;
import lombok.Getter;
import org.drinkless.tdlib.TdApi;
import org.springframework.context.ApplicationEvent;

@Getter
public class NewMessageEvent extends ApplicationEvent {

  private final TdApi.UpdateNewMessage updateNewMessage;

  public NewMessageEvent(MainUpdateHandler source, TdApi.UpdateNewMessage updateNewMessage) {
    super(source);
    this.updateNewMessage = updateNewMessage;
  }
}
