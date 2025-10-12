package live.yurii.yugram.chats;

import lombok.*;
import lombok.extern.slf4j.*;
import org.drinkless.tdlib.*;
import org.springframework.context.event.*;
import org.springframework.stereotype.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatHandler {

  private final ChatRepository chatRepository;

  @EventListener
  public void handle(NewChatEvent event) {
    TdApi.Chat tgChat = event.getUpdateNewChat().chat;
    chatRepository.findById(tgChat.id).ifPresentOrElse(
        entity -> chatRepository.save(updateEntity(entity, tgChat)),
        () -> chatRepository.save(createEntity(tgChat)));
  }

  private ChatEntity createEntity(TdApi.Chat tgChat) {
    return new ChatEntity(tgChat.id)
        .withType(ChatEntity.ChatType.fromConstructor(tgChat.type.getConstructor()))
        .withTitle(tgChat.title);
  }

  private ChatEntity updateEntity(ChatEntity entity, TdApi.Chat tgChat) {
    if (tgChat.title != null && !tgChat.title.equals(entity.getTitle())) {
      entity.setTitle(tgChat.title);
    }
    if (tgChat.type != null) {
      ChatEntity.ChatType newType = ChatEntity.ChatType.fromConstructor(tgChat.type.getConstructor());
      if (!newType.equals(entity.getType())) {
        entity.setType(newType);
      }
    }
    return entity;
  }
}
