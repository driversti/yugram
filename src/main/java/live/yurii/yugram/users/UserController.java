package live.yurii.yugram.users;

import live.yurii.yugram.users.dto.UserDto;
import live.yurii.yugram.users.dto.UserPageResponse;
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
@RequestMapping("/users")
public class UserController {

  private final UserRepository userRepository;
  private static final int DEFAULT_PAGE_SIZE = 20;

  @GetMapping("/all")
  public ResponseEntity<UserPageResponse> getAllUsers(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    log.debug("Getting all users with page: {}, size: {}", page, size);

    // Ensure size doesn't exceed default maximum
    int pageSize = Math.min(size, DEFAULT_PAGE_SIZE);
    Pageable pageable = PageRequest.of(page, pageSize);

    Page<UserEntity> userPage = userRepository.findAll(pageable);

    UserPageResponse response = UserPageResponse.builder()
        .content(userPage.getContent().stream().map(UserDto::fromEntity).toList())
        .page(userPage.getNumber())
        .size(userPage.getSize())
        .totalElements(userPage.getTotalElements())
        .totalPages(userPage.getTotalPages())
        .first(userPage.isFirst())
        .last(userPage.isLast())
        .build();

    return ResponseEntity.ok(response);
  }

  @GetMapping("/byId/{id}")
  public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
    log.debug("Getting user by id: {}", id);

    return userRepository.findById(id)
        .map(user -> ResponseEntity.ok(UserDto.fromEntity(user)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/byUsername/{username}")
  public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
    log.debug("Getting user by username: {}", username);

    return userRepository.findByUsername(username)
        .map(user -> ResponseEntity.ok(UserDto.fromEntity(user)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/byPhoneNumber/{phoneNumber}")
  public ResponseEntity<UserDto> getUserByPhoneNumber(@PathVariable String phoneNumber) {
    log.debug("Getting user by phone number: {}", phoneNumber);

    return userRepository.findByPhoneNumber(phoneNumber)
        .map(user -> ResponseEntity.ok(UserDto.fromEntity(user)))
        .orElse(ResponseEntity.notFound().build());
  }
}