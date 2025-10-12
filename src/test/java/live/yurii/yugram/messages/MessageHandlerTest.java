package live.yurii.yugram.messages;

import live.yurii.yugram.messages.config.MessageSkipProperties;
import org.drinkless.tdlib.TdApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MessageHandler}.
 * Tests are performed without Spring context for faster execution.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MessageHandler Unit Tests")
class MessageHandlerTest {

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private MessageSkipProperties skipProperties;

  @InjectMocks
  private MessageHandler messageHandler;

  private NewMessageEvent newMessageEvent;
  private TdApi.Message message;

  @BeforeEach
  void setUp() {
    TdApi.UpdateNewMessage updateNewMessage = new TdApi.UpdateNewMessage();
    message = new TdApi.Message();
    updateNewMessage.message = message;
    newMessageEvent = new NewMessageEvent(this, updateNewMessage);
  }

  @Test
  @DisplayName("Should save message when chat ID is not in skip list")
  void shouldSaveMessageWhenChatIdNotInSkipList() {
    // Given
    message.id = 123L;
    message.chatId = 456L;
    message.date = 1640995200;

    TdApi.MessageSenderUser senderUser = new TdApi.MessageSenderUser();
    senderUser.userId = 789L;
    message.senderId = senderUser;

    TdApi.MessageText textContent = new TdApi.MessageText();
    textContent.text = new TdApi.FormattedText("Hello World", null);
    message.content = textContent;

    when(messageRepository.findById(123L)).thenReturn(Optional.empty());
    when(skipProperties.shouldSkipChatId(456L)).thenReturn(false);

    // When
    messageHandler.handle(newMessageEvent);

    // Then
    verify(messageRepository).findById(123L);
    verify(messageRepository).save(any(MessageEntity.class));
    verify(skipProperties).shouldSkipChatId(456L);
  }

  @Test
  @DisplayName("Should skip message when chat ID is in skip list")
  void shouldSkipMessageWhenChatIdInSkipList() {
    // Given
    message.id = 123L;
    message.chatId = 999L;
    message.date = 1640995200;

    TdApi.MessageSenderUser senderUser = new TdApi.MessageSenderUser();
    senderUser.userId = 789L;
    message.senderId = senderUser;

    TdApi.MessageText textContent = new TdApi.MessageText();
    textContent.text = new TdApi.FormattedText("Skip this message", null);
    message.content = textContent;

    when(skipProperties.shouldSkipChatId(999L)).thenReturn(true);

    // When
    messageHandler.handle(newMessageEvent);

    // Then
    verify(messageRepository, never()).findById(any());
    verify(messageRepository, never()).save(any());
    verify(skipProperties).shouldSkipChatId(999L);
  }

  @Test
  @DisplayName("Should update existing message when chat ID is not in skip list")
  void shouldUpdateExistingMessageWhenChatIdNotInSkipList() {
    // Given
    message.id = 123L;
    message.chatId = 456L;
    message.date = 1640995200;

    TdApi.MessageSenderUser senderUser = new TdApi.MessageSenderUser();
    senderUser.userId = 789L;
    message.senderId = senderUser;

    TdApi.MessageText textContent = new TdApi.MessageText();
    textContent.text = new TdApi.FormattedText("Updated content", null);
    message.content = textContent;

    MessageEntity existingEntity = new MessageEntity(123L)
        .withSenderId(100L)
        .withChatId(200L)
        .withContent("Old content");

    when(messageRepository.findById(123L)).thenReturn(Optional.of(existingEntity));
    when(skipProperties.shouldSkipChatId(456L)).thenReturn(false);

    // When
    messageHandler.handle(newMessageEvent);

    // Then
    verify(messageRepository).findById(123L);
    verify(messageRepository).save(existingEntity);
    verify(skipProperties).shouldSkipChatId(456L);
  }

  @Test
  @DisplayName("Should not update existing message when chat ID is in skip list")
  void shouldNotUpdateExistingMessageWhenChatIdInSkipList() {
    // Given
    message.id = 123L;
    message.chatId = 999L;
    message.date = 1640995200;

    TdApi.MessageSenderUser senderUser = new TdApi.MessageSenderUser();
    senderUser.userId = 789L;
    message.senderId = senderUser;

    TdApi.MessageText textContent = new TdApi.MessageText();
    textContent.text = new TdApi.FormattedText("Updated content", null);
    message.content = textContent;

    when(skipProperties.shouldSkipChatId(999L)).thenReturn(true);

    // When
    messageHandler.handle(newMessageEvent);

    // Then
    verify(messageRepository, never()).findById(any());
    verify(messageRepository, never()).save(any());
    verify(skipProperties).shouldSkipChatId(999L);
  }

  @Test
  @DisplayName("Should save message from chat not in skip list with different message IDs")
  void shouldSaveMessageFromChatNotInSkipListWithDifferentMessageIds() {
    // Given
    message.id = 999L; // Different message ID
    message.chatId = 456L; // Chat ID not in skip list
    message.date = 1640995200;

    TdApi.MessageSenderUser senderUser = new TdApi.MessageSenderUser();
    senderUser.userId = 789L;
    message.senderId = senderUser;

    TdApi.MessageText textContent = new TdApi.MessageText();
    textContent.text = new TdApi.FormattedText("Save this message", null);
    message.content = textContent;

    when(messageRepository.findById(999L)).thenReturn(Optional.empty());
    when(skipProperties.shouldSkipChatId(456L)).thenReturn(false);

    // When
    messageHandler.handle(newMessageEvent);

    // Then
    verify(messageRepository).findById(999L);
    verify(messageRepository).save(any(MessageEntity.class));
    verify(skipProperties).shouldSkipChatId(456L);
  }

  @Test
  @DisplayName("Should skip message from chat in skip list with different message IDs")
  void shouldSkipMessageFromChatInSkipListWithDifferentMessageIds() {
    // Given
    message.id = 999L; // Different message ID
    message.chatId = 456L; // Chat ID in skip list
    message.date = 1640995200;

    TdApi.MessageSenderUser senderUser = new TdApi.MessageSenderUser();
    senderUser.userId = 789L;
    message.senderId = senderUser;

    TdApi.MessageText textContent = new TdApi.MessageText();
    textContent.text = new TdApi.FormattedText("Skip this message", null);
    message.content = textContent;

    when(skipProperties.shouldSkipChatId(456L)).thenReturn(true);

    // When
    messageHandler.handle(newMessageEvent);

    // Then
    verify(messageRepository, never()).findById(any());
    verify(messageRepository, never()).save(any());
    verify(skipProperties).shouldSkipChatId(456L);
  }

  @Test
  @DisplayName("Should save message from private chat when not skipped")
  void shouldSaveMessageFromPrivateChatWhenNotSkipped() {
    // Given
    message.id = 123L;
    message.chatId = 123456789L; // Private chat ID
    message.date = 1640995200;

    TdApi.MessageSenderUser senderUser = new TdApi.MessageSenderUser();
    senderUser.userId = 789L;
    message.senderId = senderUser;

    TdApi.MessageText textContent = new TdApi.MessageText();
    textContent.text = new TdApi.FormattedText("Private message", null);
    message.content = textContent;

    when(messageRepository.findById(123L)).thenReturn(Optional.empty());
    when(skipProperties.shouldSkipChatId(123456789L)).thenReturn(false);

    // When
    messageHandler.handle(newMessageEvent);

    // Then
    verify(messageRepository).findById(123L);
    verify(messageRepository).save(any(MessageEntity.class));
    verify(skipProperties).shouldSkipChatId(123456789L);
  }

  @Test
  @DisplayName("Should skip message from private chat when in skip list")
  void shouldSkipMessageFromPrivateChatWhenInSkipList() {
    // Given
    message.id = 123L;
    message.chatId = 123456789L; // Private chat ID
    message.date = 1640995200;

    TdApi.MessageSenderUser senderUser = new TdApi.MessageSenderUser();
    senderUser.userId = 789L;
    message.senderId = senderUser;

    TdApi.MessageText textContent = new TdApi.MessageText();
    textContent.text = new TdApi.FormattedText("Private message", null);
    message.content = textContent;

    when(skipProperties.shouldSkipChatId(123456789L)).thenReturn(true);

    // When
    messageHandler.handle(newMessageEvent);

    // Then
    verify(messageRepository, never()).findById(any());
    verify(messageRepository, never()).save(any());
    verify(skipProperties).shouldSkipChatId(123456789L);
  }

  @Test
  @DisplayName("Should save message from group chat when not skipped")
  void shouldSaveMessageFromGroupChatWhenNotSkipped() {
    // Given
    message.id = 123L;
    message.chatId = -1001125352796L; // Group/Supergroup chat ID (negative)
    message.date = 1640995200;

    TdApi.MessageSenderUser senderUser = new TdApi.MessageSenderUser();
    senderUser.userId = 789L;
    message.senderId = senderUser;

    TdApi.MessageText textContent = new TdApi.MessageText();
    textContent.text = new TdApi.FormattedText("Group message", null);
    message.content = textContent;

    when(messageRepository.findById(123L)).thenReturn(Optional.empty());
    when(skipProperties.shouldSkipChatId(-1001125352796L)).thenReturn(false);

    // When
    messageHandler.handle(newMessageEvent);

    // Then
    verify(messageRepository).findById(123L);
    verify(messageRepository).save(any(MessageEntity.class));
    verify(skipProperties).shouldSkipChatId(-1001125352796L);
  }

  @Test
  @DisplayName("Should skip message from group chat when in skip list")
  void shouldSkipMessageFromGroupChatWhenInSkipList() {
    // Given
    message.id = 123L;
    message.chatId = -1001125352796L; // Group/Supergroup chat ID (negative)
    message.date = 1640995200;

    TdApi.MessageSenderUser senderUser = new TdApi.MessageSenderUser();
    senderUser.userId = 789L;
    message.senderId = senderUser;

    TdApi.MessageText textContent = new TdApi.MessageText();
    textContent.text = new TdApi.FormattedText("Group message", null);
    message.content = textContent;

    when(skipProperties.shouldSkipChatId(-1001125352796L)).thenReturn(true);

    // When
    messageHandler.handle(newMessageEvent);

    // Then
    verify(messageRepository, never()).findById(any());
    verify(messageRepository, never()).save(any());
    verify(skipProperties).shouldSkipChatId(-1001125352796L);
  }

  @Test
  @DisplayName("Should save message from channel when not skipped")
  void shouldSaveMessageFromChannelWhenNotSkipped() {
    // Given
    message.id = 123L;
    message.chatId = -1001125352796L; // Channel ID (negative)
    message.date = 1640995200;

    TdApi.MessageSenderChat senderChat = new TdApi.MessageSenderChat();
    senderChat.chatId = -1001125352796L; // Channel is the sender
    message.senderId = senderChat;

    TdApi.MessageText textContent = new TdApi.MessageText();
    textContent.text = new TdApi.FormattedText("Channel message", null);
    message.content = textContent;

    when(messageRepository.findById(123L)).thenReturn(Optional.empty());
    when(skipProperties.shouldSkipChatId(-1001125352796L)).thenReturn(false);

    // When
    messageHandler.handle(newMessageEvent);

    // Then
    verify(messageRepository).findById(123L);
    verify(messageRepository).save(any(MessageEntity.class));
    verify(skipProperties).shouldSkipChatId(-1001125352796L);
  }

  @Test
  @DisplayName("Should skip message from channel when in skip list")
  void shouldSkipMessageFromChannelWhenInSkipList() {
    // Given
    message.id = 123L;
    message.chatId = -1001125352796L; // Channel ID (negative)
    message.date = 1640995200;

    TdApi.MessageSenderChat senderChat = new TdApi.MessageSenderChat();
    senderChat.chatId = -1001125352796L; // Channel is the sender
    message.senderId = senderChat;

    TdApi.MessageText textContent = new TdApi.MessageText();
    textContent.text = new TdApi.FormattedText("Channel message", null);
    message.content = textContent;

    when(skipProperties.shouldSkipChatId(-1001125352796L)).thenReturn(true);

    // When
    messageHandler.handle(newMessageEvent);

    // Then
    verify(messageRepository, never()).findById(any());
    verify(messageRepository, never()).save(any());
    verify(skipProperties).shouldSkipChatId(-1001125352796L);
  }

  @Test
  @DisplayName("Should handle message with photo content when chat not skipped")
  void shouldHandleMessageWithPhotoContentWhenChatNotSkipped() {
    // Given
    message.id = 123L;
    message.chatId = 456L;
    message.date = 1640995200;

    TdApi.MessageSenderUser senderUser = new TdApi.MessageSenderUser();
    senderUser.userId = 789L;
    message.senderId = senderUser;

    TdApi.MessagePhoto photoContent = new TdApi.MessagePhoto();
    photoContent.caption = new TdApi.FormattedText("Photo caption", null);
    message.content = photoContent;

    when(messageRepository.findById(123L)).thenReturn(Optional.empty());
    when(skipProperties.shouldSkipChatId(456L)).thenReturn(false);

    // When
    messageHandler.handle(newMessageEvent);

    // Then
    verify(messageRepository).findById(123L);
    verify(messageRepository).save(argThat(entity ->
        entity.getContent().equals("Photo caption")
    ));
    verify(skipProperties).shouldSkipChatId(456L);
  }

  @Test
  @DisplayName("Should skip message with photo content when chat is skipped")
  void shouldSkipMessageWithPhotoContentWhenChatIsSkipped() {
    // Given
    message.id = 123L;
    message.chatId = 999L;
    message.date = 1640995200;

    TdApi.MessageSenderUser senderUser = new TdApi.MessageSenderUser();
    senderUser.userId = 789L;
    message.senderId = senderUser;

    TdApi.MessagePhoto photoContent = new TdApi.MessagePhoto();
    photoContent.caption = new TdApi.FormattedText("Photo caption", null);
    message.content = photoContent;

    when(skipProperties.shouldSkipChatId(999L)).thenReturn(true);

    // When
    messageHandler.handle(newMessageEvent);

    // Then
    verify(messageRepository, never()).findById(any());
    verify(messageRepository, never()).save(any());
    verify(skipProperties).shouldSkipChatId(999L);
  }

  @Test
  @DisplayName("Should handle zero chat ID when not skipped")
  void shouldHandleZeroChatIdWhenNotSkipped() {
    // Given
    message.id = 123L;
    message.chatId = 0L;
    message.date = 1640995200;

    TdApi.MessageSenderUser senderUser = new TdApi.MessageSenderUser();
    senderUser.userId = 789L;
    message.senderId = senderUser;

    TdApi.MessageText textContent = new TdApi.MessageText();
    textContent.text = new TdApi.FormattedText("Zero chat ID message", null);
    message.content = textContent;

    when(messageRepository.findById(123L)).thenReturn(Optional.empty());
    when(skipProperties.shouldSkipChatId(0L)).thenReturn(false);

    // When
    messageHandler.handle(newMessageEvent);

    // Then
    verify(messageRepository).findById(123L);
    verify(messageRepository).save(any(MessageEntity.class));
    verify(skipProperties).shouldSkipChatId(0L);
  }

  @Test
  @DisplayName("Should skip message with zero chat ID when in skip list")
  void shouldSkipMessageWithZeroChatIdWhenInSkipList() {
    // Given
    message.id = 123L;
    message.chatId = 0L;
    message.date = 1640995200;

    TdApi.MessageSenderUser senderUser = new TdApi.MessageSenderUser();
    senderUser.userId = 789L;
    message.senderId = senderUser;

    TdApi.MessageText textContent = new TdApi.MessageText();
    textContent.text = new TdApi.FormattedText("Zero chat ID message", null);
    message.content = textContent;

    when(skipProperties.shouldSkipChatId(0L)).thenReturn(true);

    // When
    messageHandler.handle(newMessageEvent);

    // Then
    verify(messageRepository, never()).findById(any());
    verify(messageRepository, never()).save(any());
    verify(skipProperties).shouldSkipChatId(0L);
  }

  @Test
  @DisplayName("Should handle multiple messages with different chat ID skip outcomes")
  void shouldHandleMultipleMessagesWithDifferentChatIdSkipOutcomes() {
    // First message from chat that should be saved
    message.id = 123L;
    message.chatId = 456L;
    message.date = 1640995200;

    TdApi.MessageSenderUser senderUser = new TdApi.MessageSenderUser();
    senderUser.userId = 789L;
    message.senderId = senderUser;

    TdApi.MessageText textContent = new TdApi.MessageText();
    textContent.text = new TdApi.FormattedText("Save me", null);
    message.content = textContent;

    when(messageRepository.findById(123L)).thenReturn(Optional.empty());
    when(skipProperties.shouldSkipChatId(456L)).thenReturn(false);

    // When - First message (should be saved)
    messageHandler.handle(newMessageEvent);

    // Prepare second message from chat that should be skipped
    message.id = 456L;
    message.chatId = 999L;
    textContent.text = new TdApi.FormattedText("Skip me", null);
    when(skipProperties.shouldSkipChatId(999L)).thenReturn(true);

    // When - Second message (should be skipped)
    messageHandler.handle(newMessageEvent);

    // Then
    verify(messageRepository).findById(123L);
    verify(messageRepository).save(any(MessageEntity.class));
    verify(skipProperties).shouldSkipChatId(456L);
    verify(skipProperties).shouldSkipChatId(999L);
    verify(messageRepository, never()).findById(456L);
    verify(messageRepository, never()).save(argThat(entity -> entity.getId() == 456L));
  }
}