package live.yurii.yugram.chats;

import live.yurii.yugram.*;
import lombok.*;
import org.drinkless.tdlib.*;
import org.springframework.context.*;

@Getter
public class NewChatEvent extends ApplicationEvent {

  private final TdApi.UpdateNewChat updateNewChat;

  public NewChatEvent(MainUpdateHandler source, TdApi.UpdateNewChat updateNewChat) {
    super(source);
    this.updateNewChat = updateNewChat;
  }
}
