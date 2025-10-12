package live.yurii.yugram.messages;

import lombok.Getter;
import org.drinkless.tdlib.TdApi;
import org.springframework.context.ApplicationEvent;

@Getter
public class NewMessageEvent extends ApplicationEvent {

  private final TdApi.UpdateNewMessage updateNewMessage;

  public NewMessageEvent(Object source, TdApi.UpdateNewMessage updateNewMessage) {
    super(source);
    this.updateNewMessage = updateNewMessage;
  }
}
