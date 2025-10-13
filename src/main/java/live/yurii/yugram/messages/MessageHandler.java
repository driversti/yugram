package live.yurii.yugram.messages;

import jakarta.transaction.Transactional;
import live.yurii.yugram.messages.config.MessageSaveProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class MessageHandler implements InitializingBean {

  private final MessageRepository messageRepository;
  private final MessageSaveProperties saveProperties;

  private static String getText(TdApi.MessageContent content) {
    return switch (content.getConstructor()) {
      case TdApi.MessageText.CONSTRUCTOR -> ((TdApi.MessageText) content).text.text;
      case TdApi.MessagePhoto.CONSTRUCTOR -> ((TdApi.MessagePhoto) content).caption.text;
      case TdApi.MessageVideo.CONSTRUCTOR -> ((TdApi.MessageVideo) content).caption.text;
      default -> "";
    };
  }

  @Transactional
  @EventListener
  public void handle(NewMessageEvent event) {
    TdApi.Message tgMessage = event.getUpdateNewMessage().message;

    // Only save messages from specific chat IDs
    if (!saveProperties.shouldSaveChatId(tgMessage.chatId)) {
      log.trace("Not saving message ID {} from chat ID {} (not in save list)", tgMessage.id, tgMessage.chatId);
      return;
    }

    messageRepository.findById(tgMessage.id).ifPresentOrElse(
        entity -> messageRepository.save(updateEntity(entity, tgMessage)),
        () -> createEntity(tgMessage).ifPresent(messageRepository::save));
  }

  @Override
  public void afterPropertiesSet() {
    log.info("MessageHandler initialized with configuration: {}", saveProperties.getConfigurationSummary());
    log.info("Configuration source: {}", saveProperties.getConfigurationSource());
    if (saveProperties.hasSaveChatIds()) {
      log.info("Selective message saving is enabled. Messages from {} chat IDs will be saved.", saveProperties.getSaveChatIdsCount());
    } else {
      log.info("Selective message saving is disabled. No messages will be saved.");
    }
  }

  private Optional<MessageEntity> createEntity(TdApi.Message tgMessage) {
    String text = getText(tgMessage.content);
    if (text == null || text.isBlank()) {
      return Optional.empty();
    }
    return Optional.of(new MessageEntity(tgMessage.id)
        .withSenderId(getSenderId(tgMessage.senderId))
        .withChatId(tgMessage.chatId)
        .withDate(tgMessage.date)
        .withContent(getText(tgMessage.content)));
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
    String text = getText(tgMessage.content);
    if (text == null || text.isBlank()) {
      return entity;
    }
    return entity.withContent(text);
  }
}
