package live.yurii.yugram.messages;

import live.yurii.yugram.messages.dto.MessageDto;
import live.yurii.yugram.messages.dto.MessagePageResponse;
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
@RequestMapping("/messages")
public class MessageController {

  private final MessageRepository messageRepository;
  private static final int DEFAULT_PAGE_SIZE = 10;

  @GetMapping("/all")
  public ResponseEntity<MessagePageResponse> getAllMessages(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    log.debug("Getting all messages with page: {}, size: {}", page, size);

    // Ensure size doesn't exceed default maximum
    int pageSize = Math.min(size, DEFAULT_PAGE_SIZE);
    Pageable pageable = PageRequest.of(page, pageSize);

    Page<MessageEntity> messagePage = messageRepository.findAll(pageable);

    MessagePageResponse response = MessagePageResponse.builder()
        .content(messagePage.getContent().stream().map(MessageDto::fromEntity).toList())
        .page(messagePage.getNumber())
        .size(messagePage.getSize())
        .totalElements(messagePage.getTotalElements())
        .totalPages(messagePage.getTotalPages())
        .first(messagePage.isFirst())
        .last(messagePage.isLast())
        .build();

    return ResponseEntity.ok(response);
  }

  @GetMapping("/byChatId/{chatId}")
  public ResponseEntity<MessagePageResponse> getMessagesByChatId(
      @PathVariable Long chatId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    log.debug("Getting messages for chatId: {} with page: {}, size: {}", chatId, page, size);

    // Ensure size doesn't exceed default maximum
    int pageSize = Math.min(size, DEFAULT_PAGE_SIZE);
    Pageable pageable = PageRequest.of(page, pageSize);

    Page<MessageEntity> messagePage = messageRepository.findByChatId(chatId, pageable);

    MessagePageResponse response = MessagePageResponse.builder()
        .content(messagePage.getContent().stream().map(MessageDto::fromEntity).toList())
        .page(messagePage.getNumber())
        .size(messagePage.getSize())
        .totalElements(messagePage.getTotalElements())
        .totalPages(messagePage.getTotalPages())
        .first(messagePage.isFirst())
        .last(messagePage.isLast())
        .build();

    return ResponseEntity.ok(response);
  }

  @GetMapping("/byId/{id}")
  public ResponseEntity<MessageDto> getMessageById(@PathVariable Long id) {
    log.debug("Getting message by id: {}", id);

    return messageRepository.findById(id)
        .map(message -> ResponseEntity.ok(MessageDto.fromEntity(message)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/byChatIdAndSenderId/{id}")
  public ResponseEntity<MessagePageResponse> getMessagesByChatIdAndSenderId(
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    log.debug("Getting messages where chatId and senderId are both: {} with page: {}, size: {}", id, page, size);

    // Ensure size doesn't exceed default maximum
    int pageSize = Math.min(size, DEFAULT_PAGE_SIZE);
    Pageable pageable = PageRequest.of(page, pageSize);

    Page<MessageEntity> messagePage = messageRepository.findByChatIdAndSenderId(id, id, pageable);

    MessagePageResponse response = MessagePageResponse.builder()
        .content(messagePage.getContent().stream().map(MessageDto::fromEntity).toList())
        .page(messagePage.getNumber())
        .size(messagePage.getSize())
        .totalElements(messagePage.getTotalElements())
        .totalPages(messagePage.getTotalPages())
        .first(messagePage.isFirst())
        .last(messagePage.isLast())
        .build();

    return ResponseEntity.ok(response);
  }
}