package live.yurii.yugram.users;

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
 * Unit tests for {@link UserController}.
 */
@WebMvcTest(UserController.class)
@DisplayName("UserController Tests")
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserRepository userRepository;

  @Test
  @DisplayName("GET /users/all should return paginated users with default values")
  void whenGetAllUsers_thenReturnPaginatedUsersWithDefaults() throws Exception {
    // Given
    UserEntity user1 = new UserEntity(1L)
        .withUsername("john_doe")
        .withFirstName("John")
        .withLastName("Doe")
        .withPhoneNumber("+1234567890")
        .withIsContact(true)
        .withIsMutualContact(false)
        .withIsCloseFriend(false)
        .withIsPremium(true)
        .withIsSupport(false)
        .withLanguageCode("en")
        .withType(UserEntity.UserType.REGULAR);

    UserEntity user2 = new UserEntity(2L)
        .withUsername("jane_smith")
        .withFirstName("Jane")
        .withLastName("Smith")
        .withPhoneNumber("+0987654321")
        .withIsContact(false)
        .withIsMutualContact(true)
        .withIsCloseFriend(true)
        .withIsPremium(false)
        .withIsSupport(false)
        .withLanguageCode("en")
        .withType(UserEntity.UserType.REGULAR);

    Page<UserEntity> userPage = new PageImpl<>(List.of(user1, user2), PageRequest.of(0, 20), 2);
    when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

    // When & Then
    mockMvc.perform(get("/users/all"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].username").value("john_doe"))
        .andExpect(jsonPath("$.content[0].firstName").value("John"))
        .andExpect(jsonPath("$.content[0].lastName").value("Doe"))
        .andExpect(jsonPath("$.content[0].phoneNumber").value("+1234567890"))
        .andExpect(jsonPath("$.content[0].isContact").value(true))
        .andExpect(jsonPath("$.content[0].isMutualContact").value(false))
        .andExpect(jsonPath("$.content[0].isCloseFriend").value(false))
        .andExpect(jsonPath("$.content[0].isPremium").value(true))
        .andExpect(jsonPath("$.content[0].isSupport").value(false))
        .andExpect(jsonPath("$.content[0].languageCode").value("en"))
        .andExpect(jsonPath("$.content[0].type").value("REGULAR"))
        .andExpect(jsonPath("$.content[1].id").value(2))
        .andExpect(jsonPath("$.content[1].username").value("jane_smith"))
        .andExpect(jsonPath("$.content[1].firstName").value("Jane"))
        .andExpect(jsonPath("$.content[1].lastName").value("Smith"))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(20))
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.first").value(true))
        .andExpect(jsonPath("$.last").value(true));
  }

  @Test
  @DisplayName("GET /users/all with custom pagination should return paginated users")
  void whenGetAllUsersWithCustomPagination_thenReturnPaginatedUsers() throws Exception {
    // Given
    UserEntity user = new UserEntity(1L)
        .withUsername("test_user")
        .withFirstName("Test")
        .withLastName("User")
        .withPhoneNumber("+1111111111")
        .withType(UserEntity.UserType.REGULAR);

    Page<UserEntity> userPage = new PageImpl<>(List.of(user), PageRequest.of(1, 10), 35);
    when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

    // When & Then
    mockMvc.perform(get("/users/all")
            .param("page", "1")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.page").value(1))
        .andExpect(jsonPath("$.size").value(10))
        .andExpect(jsonPath("$.totalElements").value(35))
        .andExpect(jsonPath("$.totalPages").value(4))
        .andExpect(jsonPath("$.first").value(false))
        .andExpect(jsonPath("$.last").value(false));
  }

  @Test
  @DisplayName("GET /users/all with large size should limit to default size")
  void whenGetAllUsersWithLargeSize_thenLimitToDefaultSize() throws Exception {
    // Given
    Page<UserEntity> userPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
    when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

    // When & Then
    mockMvc.perform(get("/users/all")
            .param("size", "100"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(20));
  }

  @Test
  @DisplayName("GET /users/byId/{id} with existing ID should return user")
  void whenGetUserByIdWithExistingId_thenReturnUser() throws Exception {
    // Given
    Long userId = 1L;
    UserEntity user = new UserEntity(userId)
        .withUsername("john_doe")
        .withFirstName("John")
        .withLastName("Doe")
        .withPhoneNumber("+1234567890")
        .withType(UserEntity.UserType.REGULAR);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    // When & Then
    mockMvc.perform(get("/users/byId/{id}", userId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(userId))
        .andExpect(jsonPath("$.username").value("john_doe"))
        .andExpect(jsonPath("$.firstName").value("John"))
        .andExpect(jsonPath("$.lastName").value("Doe"))
        .andExpect(jsonPath("$.phoneNumber").value("+1234567890"))
        .andExpect(jsonPath("$.type").value("REGULAR"));
  }

  @Test
  @DisplayName("GET /users/byId/{id} with non-existing ID should return 404")
  void whenGetUserByIdWithNonExistingId_thenReturn404() throws Exception {
    // Given
    Long userId = 999L;
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(get("/users/byId/{id}", userId))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /users/byUsername/{username} with existing username should return user")
  void whenGetUserByUsernameWithExistingUsername_thenReturnUser() throws Exception {
    // Given
    String username = "john_doe";
    UserEntity user = new UserEntity(1L)
        .withUsername(username)
        .withFirstName("John")
        .withLastName("Doe")
        .withPhoneNumber("+1234567890")
        .withType(UserEntity.UserType.REGULAR);

    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

    // When & Then
    mockMvc.perform(get("/users/byUsername/{username}", username))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.username").value(username))
        .andExpect(jsonPath("$.firstName").value("John"))
        .andExpect(jsonPath("$.lastName").value("Doe"))
        .andExpect(jsonPath("$.type").value("REGULAR"));
  }

  @Test
  @DisplayName("GET /users/byUsername/{username} with non-existing username should return 404")
  void whenGetUserByUsernameWithNonExistingUsername_thenReturn404() throws Exception {
    // Given
    String username = "nonexistent_user";
    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(get("/users/byUsername/{username}", username))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /users/byPhoneNumber/{phoneNumber} with existing phone number should return user")
  void whenGetUserByPhoneNumberWithExistingPhoneNumber_thenReturnUser() throws Exception {
    // Given
    String phoneNumber = "+1234567890";
    UserEntity user = new UserEntity(1L)
        .withUsername("john_doe")
        .withFirstName("John")
        .withLastName("Doe")
        .withPhoneNumber(phoneNumber)
        .withType(UserEntity.UserType.REGULAR);

    when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(user));

    // When & Then
    mockMvc.perform(get("/users/byPhoneNumber/{phoneNumber}", phoneNumber))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.phoneNumber").value(phoneNumber))
        .andExpect(jsonPath("$.username").value("john_doe"))
        .andExpect(jsonPath("$.type").value("REGULAR"));
  }

  @Test
  @DisplayName("GET /users/byPhoneNumber/{phoneNumber} with non-existing phone number should return 404")
  void whenGetUserByPhoneNumberWithNonExistingPhoneNumber_thenReturn404() throws Exception {
    // Given
    String phoneNumber = "+9999999999";
    when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(get("/users/byPhoneNumber/{phoneNumber}", phoneNumber))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /users/all with empty result should return empty page")
  void whenGetAllUsersWithEmptyResult_thenReturnEmptyPage() throws Exception {
    // Given
    Page<UserEntity> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
    when(userRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

    // When & Then
    mockMvc.perform(get("/users/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.totalElements").value(0))
        .andExpect(jsonPath("$.totalPages").value(0))
        .andExpect(jsonPath("$.first").value(true))
        .andExpect(jsonPath("$.last").value(true));
  }

  @Test
  @DisplayName("GET /users/all with bot user should handle bot type correctly")
  void whenGetAllUsersWithBotUser_thenHandleBotTypeCorrectly() throws Exception {
    // Given
    UserEntity botUser = new UserEntity(1L)
        .withUsername("test_bot")
        .withFirstName("Test Bot")
        .withType(UserEntity.UserType.BOT);

    Page<UserEntity> userPage = new PageImpl<>(List.of(botUser), PageRequest.of(0, 20), 1);
    when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

    // When & Then
    mockMvc.perform(get("/users/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].type").value("BOT"))
        .andExpect(jsonPath("$.content[0].username").value("test_bot"))
        .andExpect(jsonPath("$.content[0].firstName").value("Test Bot"));
  }

  @Test
  @DisplayName("GET /users/all with deleted user should handle deleted type correctly")
  void whenGetAllUsersWithDeletedUser_thenHandleDeletedTypeCorrectly() throws Exception {
    // Given
    UserEntity deletedUser = new UserEntity(1L)
        .withUsername("deleted_user")
        .withFirstName("Deleted")
        .withType(UserEntity.UserType.DELETED);

    Page<UserEntity> userPage = new PageImpl<>(List.of(deletedUser), PageRequest.of(0, 20), 1);
    when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

    // When & Then
    mockMvc.perform(get("/users/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].type").value("DELETED"))
        .andExpect(jsonPath("$.content[0].username").value("deleted_user"));
  }

  @Test
  @DisplayName("GET /users/byId/{id} with user having null fields should handle gracefully")
  void whenGetUserByIdWithNullFields_thenHandleGracefully() throws Exception {
    // Given
    Long userId = 1L;
    UserEntity user = new UserEntity(userId)
        .withUsername(null)
        .withFirstName(null)
        .withLastName(null)
        .withPhoneNumber(null)
        .withLanguageCode(null)
        .withType(null);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    // When & Then
    mockMvc.perform(get("/users/byId/{id}", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId))
        .andExpect(jsonPath("$.username").doesNotExist())
        .andExpect(jsonPath("$.firstName").doesNotExist())
        .andExpect(jsonPath("$.lastName").doesNotExist())
        .andExpect(jsonPath("$.phoneNumber").doesNotExist())
        .andExpect(jsonPath("$.languageCode").doesNotExist())
        .andExpect(jsonPath("$.type").doesNotExist());
  }

  @Test
  @DisplayName("GET /users/all with negative page should handle gracefully")
  void whenGetAllUsersWithNegativePage_thenHandleGracefully() throws Exception {
    // Given
    Page<UserEntity> userPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
    when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

    // When & Then
    mockMvc.perform(get("/users/all")
            .param("page", "-1"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET /users/all with zero size should normalize to size 1")
  void whenGetAllUsersWithZeroSize_thenNormalizeToSizeOne() throws Exception {
    // Given
    Page<UserEntity> userPage = new PageImpl<>(List.of(), PageRequest.of(0, 1), 0);
    when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

    // When & Then
    mockMvc.perform(get("/users/all")
            .param("size", "0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(1));
  }
}