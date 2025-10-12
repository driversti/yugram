package live.yurii.yugram.messages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.*;
import org.springframework.context.event.*;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MessageHandler {

  private final MessageRepository messageRepository;

  @EventListener
  public void handle(NewMessageEvent event) {
    TdApi.Message tgMessage = event.getUpdateNewMessage().message;
    messageRepository.findById(tgMessage.id).ifPresentOrElse(
        entity -> messageRepository.save(updateEntity(entity, tgMessage)),
        () -> messageRepository.save(createEntity(tgMessage)));
  }

  private MessageEntity createEntity(TdApi.Message tgMessage) {
    return new MessageEntity(tgMessage.id)
        .withSenderId(getSenderId(tgMessage.senderId))
        .withChatId(tgMessage.chatId)
        .withDate(tgMessage.date)
        .withContent(getText(tgMessage.content));
  }

  private long getSenderId(TdApi.MessageSender sender) {
    if (sender instanceof TdApi.MessageSenderUser) {
      return ((TdApi.MessageSenderUser) sender).userId;
    }

    if (sender instanceof TdApi.MessageSenderChat) {
      return ((TdApi.MessageSenderChat) sender).chatId;
    }
    throw new IllegalArgumentException("Unknown sender type: " + sender.getClass());
  }

  private MessageEntity updateEntity(MessageEntity entity, TdApi.Message tgMessage) {
    return entity.withContent(getText(tgMessage.content));
  }

  private static String getText(TdApi.MessageContent content) {
    return switch (content.getConstructor()) {
      case TdApi.MessageText.CONSTRUCTOR -> ((TdApi.MessageText) content).text.text;
      case TdApi.MessagePhoto.CONSTRUCTOR -> ((TdApi.MessagePhoto) content).caption.text;
      default -> "No content";
    };
  }
}
