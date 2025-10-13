package live.yurii.yugram.chats;

import live.yurii.yugram.chats.dto.ChatDto;
import live.yurii.yugram.chats.dto.ChatPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/chats")
@CrossOrigin(origins = "*")
public class ChatController {

  private final ChatRepository chatRepository;
  private static final int DEFAULT_PAGE_SIZE = 20;

  @GetMapping("/all")
  public ResponseEntity<ChatPageResponse> getAllChats(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    log.debug("Getting all chats with page: {}, size: {}", page, size);

    // Validate and normalize pagination parameters
    int normalizedPage = Math.max(0, page);
    int normalizedSize = Math.max(1, Math.min(size, DEFAULT_PAGE_SIZE));
    Pageable pageable = PageRequest.of(normalizedPage, normalizedSize);

    Page<ChatEntity> chatPage = chatRepository.findAll(pageable);

    ChatPageResponse response = ChatPageResponse.builder()
        .content(chatPage.getContent().stream().map(ChatDto::fromEntity).toList())
        .page(chatPage.getNumber())
        .size(chatPage.getSize())
        .totalElements(chatPage.getTotalElements())
        .totalPages(chatPage.getTotalPages())
        .first(chatPage.isFirst())
        .last(chatPage.isLast())
        .build();

    return ResponseEntity.ok(response);
  }

  @GetMapping("/withMessages")
  public ResponseEntity<ChatPageResponse> getChatsWithMessages(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    log.debug("Getting chats with messages - page: {}, size: {}", page, size);

    // Validate and normalize pagination parameters
    int normalizedPage = Math.max(0, page);
    int normalizedSize = Math.max(1, Math.min(size, DEFAULT_PAGE_SIZE));
    Pageable pageable = PageRequest.of(normalizedPage, normalizedSize);

    Page<ChatEntity> chatPage = chatRepository.findChatsWithMessages(pageable);

    ChatPageResponse response = ChatPageResponse.builder()
        .content(chatPage.getContent().stream().map(ChatDto::fromEntity).toList())
        .page(chatPage.getNumber())
        .size(chatPage.getSize())
        .totalElements(chatPage.getTotalElements())
        .totalPages(chatPage.getTotalPages())
        .first(chatPage.isFirst())
        .last(chatPage.isLast())
        .build();

    return ResponseEntity.ok(response);
  }

  @GetMapping("/byId/{id}")
  public ResponseEntity<ChatDto> getChatById(@PathVariable Long id) {
    log.debug("Getting chat by id: {}", id);

    return chatRepository.findById(id)
        .map(chat -> ResponseEntity.ok(ChatDto.fromEntity(chat)))
        .orElse(ResponseEntity.notFound().build());
  }
}