package live.yurii.yugram.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import live.yurii.yugram.messages.dto.MessageDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link MessageController}.
 */
@WebMvcTest(MessageController.class)
@DisplayName("MessageController Tests")
class MessageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private MessageRepository messageRepository;

  @Test
  @DisplayName("GET /messages/all should return paginated messages with default values")
  void whenGetAllMessages_thenReturnPaginatedMessagesWithDefaults() throws Exception {
    // Given
    MessageEntity message1 = new MessageEntity(1L)
        .withSenderId(100L)
        .withChatId(200L)
        .withDate(1640995200)
        .withContent("Hello World");
    MessageEntity message2 = new MessageEntity(2L)
        .withSenderId(101L)
        .withChatId(200L)
        .withDate(1640995300)
        .withContent("How are you?");

    Page<MessageEntity> messagePage = new PageImpl<>(List.of(message1, message2), PageRequest.of(0, 10), 2);
    when(messageRepository.findAll(any(PageRequest.class))).thenReturn(messagePage);

    // When & Then
    mockMvc.perform(get("/messages/all"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].senderId").value(100))
        .andExpect(jsonPath("$.content[0].chatId").value(200))
        .andExpect(jsonPath("$.content[0].date").value(1640995200))
        .andExpect(jsonPath("$.content[0].content").value("Hello World"))
        .andExpect(jsonPath("$.content[1].id").value(2))
        .andExpect(jsonPath("$.content[1].senderId").value(101))
        .andExpect(jsonPath("$.content[1].chatId").value(200))
        .andExpect(jsonPath("$.content[1].date").value(1640995300))
        .andExpect(jsonPath("$.content[1].content").value("How are you?"))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(10))
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.first").value(true))
        .andExpect(jsonPath("$.last").value(true));
  }

  @Test
  @DisplayName("GET /messages/all with custom pagination should return paginated messages")
  void whenGetAllMessagesWithCustomPagination_thenReturnPaginatedMessages() throws Exception {
    // Given
    MessageEntity message = new MessageEntity(1L)
        .withSenderId(100L)
        .withChatId(200L)
        .withDate(1640995200)
        .withContent("Test message");

    Page<MessageEntity> messagePage = new PageImpl<>(List.of(message), PageRequest.of(2, 5), 25);
    when(messageRepository.findAll(any(PageRequest.class))).thenReturn(messagePage);

    // When & Then
    mockMvc.perform(get("/messages/all")
            .param("page", "2")
            .param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.page").value(2))
        .andExpect(jsonPath("$.size").value(5))
        .andExpect(jsonPath("$.totalElements").value(25))
        .andExpect(jsonPath("$.totalPages").value(5))
        .andExpect(jsonPath("$.first").value(false))
        .andExpect(jsonPath("$.last").value(false));
  }

  @Test
  @DisplayName("GET /messages/all with large size should limit to default size")
  void whenGetAllMessagesWithLargeSize_thenLimitToDefaultSize() throws Exception {
    // Given
    Page<MessageEntity> messagePage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    when(messageRepository.findAll(any(PageRequest.class))).thenReturn(messagePage);

    // When & Then
    mockMvc.perform(get("/messages/all")
            .param("size", "100"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(10));
  }

  @Test
  @DisplayName("GET /messages/byChatId/{chatId} should return messages for specific chat")
  void whenGetMessagesByChatId_thenReturnMessagesForChat() throws Exception {
    // Given
    Long chatId = 123L;
    MessageEntity message1 = new MessageEntity(1L)
        .withSenderId(100L)
        .withChatId(chatId)
        .withDate(1640995200)
        .withContent("Message 1");
    MessageEntity message2 = new MessageEntity(2L)
        .withSenderId(101L)
        .withChatId(chatId)
        .withDate(1640995300)
        .withContent("Message 2");

    Page<MessageEntity> messagePage = new PageImpl<>(List.of(message1, message2), PageRequest.of(0, 10), 2);
    when(messageRepository.findByChatId(eq(chatId), any(PageRequest.class))).thenReturn(messagePage);

    // When & Then
    mockMvc.perform(get("/messages/byChatId/{chatId}", chatId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].chatId").value(chatId))
        .andExpect(jsonPath("$.content[1].chatId").value(chatId))
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  @Test
  @DisplayName("GET /messages/byChatId/{chatId} with pagination should return paginated messages")
  void whenGetMessagesByChatIdWithPagination_thenReturnPaginatedMessages() throws Exception {
    // Given
    Long chatId = 123L;
    MessageEntity message = new MessageEntity(1L)
        .withSenderId(100L)
        .withChatId(chatId)
        .withDate(1640995200)
        .withContent("Test message");

    Page<MessageEntity> messagePage = new PageImpl<>(List.of(message), PageRequest.of(1, 5), 12);
    when(messageRepository.findByChatId(eq(chatId), any(PageRequest.class))).thenReturn(messagePage);

    // When & Then
    mockMvc.perform(get("/messages/byChatId/{chatId}", chatId)
            .param("page", "1")
            .param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.page").value(1))
        .andExpect(jsonPath("$.size").value(5))
        .andExpect(jsonPath("$.totalElements").value(12))
        .andExpect(jsonPath("$.totalPages").value(3));
  }

  @Test
  @DisplayName("GET /messages/byChatId/{chatId} with no messages should return empty page")
  void whenGetMessagesByChatIdWithNoMessages_thenReturnEmptyPage() throws Exception {
    // Given
    Long chatId = 999L;
    Page<MessageEntity> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    when(messageRepository.findByChatId(eq(chatId), any(PageRequest.class))).thenReturn(emptyPage);

    // When & Then
    mockMvc.perform(get("/messages/byChatId/{chatId}", chatId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Test
  @DisplayName("GET /messages/byId/{id} with existing ID should return message")
  void whenGetMessageByIdWithExistingId_thenReturnMessage() throws Exception {
    // Given
    Long messageId = 1L;
    MessageEntity message = new MessageEntity(messageId)
        .withSenderId(100L)
        .withChatId(200L)
        .withDate(1640995200)
        .withContent("Test message");

    when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

    // When & Then
    mockMvc.perform(get("/messages/byId/{id}", messageId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(messageId))
        .andExpect(jsonPath("$.senderId").value(100))
        .andExpect(jsonPath("$.chatId").value(200))
        .andExpect(jsonPath("$.date").value(1640995200))
        .andExpect(jsonPath("$.content").value("Test message"));
  }

  @Test
  @DisplayName("GET /messages/byId/{id} with non-existing ID should return 404")
  void whenGetMessageByIdWithNonExistingId_thenReturn404() throws Exception {
    // Given
    Long messageId = 999L;
    when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(get("/messages/byId/{id}", messageId))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /messages/byChatIdAndSenderId/{id} should return messages where chatId equals senderId")
  void whenGetMessagesByChatIdAndSenderId_thenReturnMessagesWhereChatIdEqualsSenderId() throws Exception {
    // Given
    Long id = 123L;
    MessageEntity message1 = new MessageEntity(1L)
        .withSenderId(id)
        .withChatId(id)
        .withDate(1640995200)
        .withContent("System message 1");
    MessageEntity message2 = new MessageEntity(2L)
        .withSenderId(id)
        .withChatId(id)
        .withDate(1640995300)
        .withContent("System message 2");

    Page<MessageEntity> messagePage = new PageImpl<>(List.of(message1, message2), PageRequest.of(0, 10), 2);
    when(messageRepository.findByChatIdAndSenderId(eq(id), eq(id), any(PageRequest.class))).thenReturn(messagePage);

    // When & Then
    mockMvc.perform(get("/messages/byChatIdAndSenderId/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].senderId").value(id))
        .andExpect(jsonPath("$.content[0].chatId").value(id))
        .andExpect(jsonPath("$.content[1].senderId").value(id))
        .andExpect(jsonPath("$.content[1].chatId").value(id))
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  @Test
  @DisplayName("GET /messages/byChatIdAndSenderId/{id} with pagination should return paginated messages")
  void whenGetMessagesByChatIdAndSenderIdWithPagination_thenReturnPaginatedMessages() throws Exception {
    // Given
    Long id = 123L;
    MessageEntity message = new MessageEntity(1L)
        .withSenderId(id)
        .withChatId(id)
        .withDate(1640995200)
        .withContent("System message");

    Page<MessageEntity> messagePage = new PageImpl<>(List.of(message), PageRequest.of(0, 5), 8);
    when(messageRepository.findByChatIdAndSenderId(eq(id), eq(id), any(PageRequest.class))).thenReturn(messagePage);

    // When & Then
    mockMvc.perform(get("/messages/byChatIdAndSenderId/{id}", id)
            .param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.size").value(5))
        .andExpect(jsonPath("$.totalElements").value(8))
        .andExpect(jsonPath("$.totalPages").value(2));
  }

  @Test
  @DisplayName("GET /messages/byChatIdAndSenderId/{id} with no matching messages should return empty page")
  void whenGetMessagesByChatIdAndSenderIdWithNoMessages_thenReturnEmptyPage() throws Exception {
    // Given
    Long id = 999L;
    Page<MessageEntity> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    when(messageRepository.findByChatIdAndSenderId(eq(id), eq(id), any(PageRequest.class))).thenReturn(emptyPage);

    // When & Then
    mockMvc.perform(get("/messages/byChatIdAndSenderId/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Test
  @DisplayName("GET /messages/all with empty result should return empty page")
  void whenGetAllMessagesWithEmptyResult_thenReturnEmptyPage() throws Exception {
    // Given
    Page<MessageEntity> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    when(messageRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

    // When & Then
    mockMvc.perform(get("/messages/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.totalElements").value(0))
        .andExpect(jsonPath("$.totalPages").value(0));
  }

  @Test
  @DisplayName("GET /messages/byId/{id} with message having null content should handle gracefully")
  void whenGetMessageByIdWithNullContent_thenHandleGracefully() throws Exception {
    // Given
    Long messageId = 1L;
    MessageEntity message = new MessageEntity(messageId)
        .withSenderId(100L)
        .withChatId(200L)
        .withDate(1640995200)
        .withContent(null);

    when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

    // When & Then
    mockMvc.perform(get("/messages/byId/{id}", messageId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(messageId))
        .andExpect(jsonPath("$.content").doesNotExist());
  }

  @Test
  @DisplayName("GET /messages/all with zero size should normalize to size 1")
  void whenGetAllMessagesWithZeroSize_thenNormalizeToSizeOne() throws Exception {
    // Given
    Page<MessageEntity> messagePage = new PageImpl<>(List.of(), PageRequest.of(0, 1), 0);
    when(messageRepository.findAll(any(PageRequest.class))).thenReturn(messagePage);

    // When & Then
    mockMvc.perform(get("/messages/all")
            .param("size", "0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(1));
  }

  @Test
  @DisplayName("GET /messages/byChatId/{chatId} with zero size should normalize to size 1")
  void whenGetMessagesByChatIdWithZeroSize_thenNormalizeToSizeOne() throws Exception {
    // Given
    Long chatId = 123L;
    Page<MessageEntity> messagePage = new PageImpl<>(List.of(), PageRequest.of(0, 1), 0);
    when(messageRepository.findByChatId(eq(chatId), any(PageRequest.class))).thenReturn(messagePage);

    // When & Then
    mockMvc.perform(get("/messages/byChatId/{chatId}", chatId)
            .param("size", "0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(1));
  }

  @Test
  @DisplayName("GET /messages/byChatIdAndSenderId/{id} with zero size should normalize to size 1")
  void whenGetMessagesByChatIdAndSenderIdWithZeroSize_thenNormalizeToSizeOne() throws Exception {
    // Given
    Long id = 123L;
    Page<MessageEntity> messagePage = new PageImpl<>(List.of(), PageRequest.of(0, 1), 0);
    when(messageRepository.findByChatIdAndSenderId(eq(id), eq(id), any(PageRequest.class))).thenReturn(messagePage);

    // When & Then
    mockMvc.perform(get("/messages/byChatIdAndSenderId/{id}", id)
            .param("size", "0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(1));
  }
}