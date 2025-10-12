package live.yurii.yugram.chats;

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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link ChatController}.
 */
@WebMvcTest(ChatController.class)
@DisplayName("ChatController Tests")
class ChatControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ChatRepository chatRepository;

  @Test
  @DisplayName("GET /chats/all should return paginated chats with default values")
  void whenGetAllChats_thenReturnPaginatedChatsWithDefaults() throws Exception {
    // Given
    ChatEntity chat1 = new ChatEntity(1L)
        .withType(ChatEntity.ChatType.PRIVATE)
        .withTitle("John Doe");
    ChatEntity chat2 = new ChatEntity(2L)
        .withType(ChatEntity.ChatType.SUPERGROUP)
        .withTitle("Test Group");

    Page<ChatEntity> chatPage = new PageImpl<>(List.of(chat1, chat2), PageRequest.of(0, 20), 2);
    when(chatRepository.findAll(any(PageRequest.class))).thenReturn(chatPage);

    // When & Then
    mockMvc.perform(get("/chats/all"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].type").value("PRIVATE"))
        .andExpect(jsonPath("$.content[0].title").value("John Doe"))
        .andExpect(jsonPath("$.content[1].id").value(2))
        .andExpect(jsonPath("$.content[1].type").value("SUPERGROUP"))
        .andExpect(jsonPath("$.content[1].title").value("Test Group"))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(20))
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.first").value(true))
        .andExpect(jsonPath("$.last").value(true));
  }

  @Test
  @DisplayName("GET /chats/all with custom pagination should return paginated chats")
  void whenGetAllChatsWithCustomPagination_thenReturnPaginatedChats() throws Exception {
    // Given
    ChatEntity chat = new ChatEntity(1L)
        .withType(ChatEntity.ChatType.BASIC_GROUP)
        .withTitle("Basic Group");

    Page<ChatEntity> chatPage = new PageImpl<>(List.of(chat), PageRequest.of(1, 5), 15);
    when(chatRepository.findAll(any(PageRequest.class))).thenReturn(chatPage);

    // When & Then
    mockMvc.perform(get("/chats/all")
            .param("page", "1")
            .param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.page").value(1))
        .andExpect(jsonPath("$.size").value(5))
        .andExpect(jsonPath("$.totalElements").value(15))
        .andExpect(jsonPath("$.totalPages").value(3))
        .andExpect(jsonPath("$.first").value(false))
        .andExpect(jsonPath("$.last").value(false));
  }

  @Test
  @DisplayName("GET /chats/all with large size should limit to default size")
  void whenGetAllChatsWithLargeSize_thenLimitToDefaultSize() throws Exception {
    // Given
    Page<ChatEntity> chatPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
    when(chatRepository.findAll(any(PageRequest.class))).thenReturn(chatPage);

    // When & Then
    mockMvc.perform(get("/chats/all")
            .param("size", "100"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(20));
  }

  @Test
  @DisplayName("GET /chats/byId/{id} with existing ID should return chat")
  void whenGetChatByIdWithExistingId_thenReturnChat() throws Exception {
    // Given
    Long chatId = 1L;
    ChatEntity chat = new ChatEntity(chatId)
        .withType(ChatEntity.ChatType.PRIVATE)
        .withTitle("Test Chat");

    when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

    // When & Then
    mockMvc.perform(get("/chats/byId/{id}", chatId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(chatId))
        .andExpect(jsonPath("$.type").value("PRIVATE"))
        .andExpect(jsonPath("$.title").value("Test Chat"));
  }

  @Test
  @DisplayName("GET /chats/byId/{id} with non-existing ID should return 404")
  void whenGetChatByIdWithNonExistingId_thenReturn404() throws Exception {
    // Given
    Long chatId = 999L;
    when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(get("/chats/byId/{id}", chatId))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /chats/all with empty result should return empty page")
  void whenGetAllChatsWithEmptyResult_thenReturnEmptyPage() throws Exception {
    // Given
    Page<ChatEntity> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
    when(chatRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

    // When & Then
    mockMvc.perform(get("/chats/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.totalElements").value(0))
        .andExpect(jsonPath("$.totalPages").value(0))
        .andExpect(jsonPath("$.first").value(true))
        .andExpect(jsonPath("$.last").value(true));
  }

  @Test
  @DisplayName("GET /chats/all with negative page should handle gracefully")
  void whenGetAllChatsWithNegativePage_thenHandleGracefully() throws Exception {
    // Given
    Page<ChatEntity> chatPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
    when(chatRepository.findAll(any(PageRequest.class))).thenReturn(chatPage);

    // When & Then
    mockMvc.perform(get("/chats/all")
            .param("page", "-1"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET /chats/all with zero size should normalize to size 1")
  void whenGetAllChatsWithZeroSize_thenNormalizeToSizeOne() throws Exception {
    // Given
    Page<ChatEntity> chatPage = new PageImpl<>(List.of(), PageRequest.of(0, 1), 0);
    when(chatRepository.findAll(any(PageRequest.class))).thenReturn(chatPage);

    // When & Then
    mockMvc.perform(get("/chats/all")
            .param("size", "0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(1));
  }
}