package live.yurii.yugram.messages.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MessageSkipProperties}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MessageSkipProperties Tests")
class MessageSkipPropertiesTest {

  @Test
  @DisplayName("Should handle empty YAML list")
  void shouldHandleEmptyYamlList() {
    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIds(List.of());

    // When
    boolean shouldSkip = properties.shouldSkipChatId(123L);
    List<Long> effectiveIds = properties.getEffectiveChatIds();

    // Then
    assertThat(shouldSkip).isFalse();
    assertThat(effectiveIds).isEmpty();
    assertThat(properties.hasSkipChatIds()).isFalse();
    assertThat(properties.getSkipChatIdsCount()).isZero();
    assertThat(properties.isUsingEnvironmentVariable()).isFalse();
    assertThat(properties.getConfigurationSource()).isEqualTo("YAML configuration");
  }

  @Test
  @DisplayName("Should handle YAML list with chat IDs")
  void shouldHandleYamlListWithChatIds() {
    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIds(List.of(123456789L, -1001125352796L, 555555555L));

    // When
    boolean shouldSkipExisting = properties.shouldSkipChatId(-1001125352796L);
    boolean shouldSkipNonExisting = properties.shouldSkipChatId(999L);
    List<Long> effectiveIds = properties.getEffectiveChatIds();

    // Then
    assertThat(shouldSkipExisting).isTrue();
    assertThat(shouldSkipNonExisting).isFalse();
    assertThat(effectiveIds).containsExactly(123456789L, -1001125352796L, 555555555L);
    assertThat(properties.hasSkipChatIds()).isTrue();
    assertThat(properties.getSkipChatIdsCount()).isEqualTo(3);
    assertThat(properties.isUsingEnvironmentVariable()).isFalse();
    assertThat(properties.getConfigurationSource()).isEqualTo("YAML configuration");
  }

  @Test
  @DisplayName("Should prioritize environment variable over YAML list")
  void shouldPrioritizeEnvironmentVariableOverYamlList() {
    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIds(List.of(111111111L, 222222222L, 333333333L)); // YAML list
    properties.setChatIdsEnv("123456789,987654321,-1001125352796"); // Environment variable

    // When
    boolean shouldSkipYamlId = properties.shouldSkipChatId(111111111L);
    boolean shouldSkipEnvId = properties.shouldSkipChatId(987654321L);
    List<Long> effectiveIds = properties.getEffectiveChatIds();

    // Then
    assertThat(shouldSkipYamlId).isFalse(); // YAML ID should be ignored
    assertThat(shouldSkipEnvId).isTrue();   // Environment variable ID should be used
    assertThat(effectiveIds).containsExactly(123456789L, 987654321L, -1001125352796L);
    assertThat(properties.hasSkipChatIds()).isTrue();
    assertThat(properties.getSkipChatIdsCount()).isEqualTo(3);
    assertThat(properties.isUsingEnvironmentVariable()).isTrue();
    assertThat(properties.getConfigurationSource()).isEqualTo("Environment variable SKIP_CHAT_IDS");
  }

  @Test
  @DisplayName("Should handle empty environment variable")
  void shouldHandleEmptyEnvironmentVariable() {
    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIds(List.of(123456789L, -1001125352796L, 555555555L));
    properties.setChatIdsEnv("");

    // When
    boolean shouldSkip = properties.shouldSkipChatId(-1001125352796L);
    List<Long> effectiveIds = properties.getEffectiveChatIds();

    // Then
    assertThat(shouldSkip).isTrue(); // Should use YAML list when env var is empty
    assertThat(effectiveIds).containsExactly(123456789L, -1001125352796L, 555555555L);
    assertThat(properties.isUsingEnvironmentVariable()).isFalse();
  }

  @Test
  @DisplayName("Should handle environment variable with whitespace")
  void shouldHandleEnvironmentVariableWithWhitespace() {
    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIdsEnv(" 123456789 , -1001125352796 , 555555555  ");

    // When
    List<Long> effectiveIds = properties.getEffectiveChatIds();

    // Then
    assertThat(effectiveIds).containsExactly(123456789L, -1001125352796L, 555555555L);
    assertThat(properties.hasSkipChatIds()).isTrue();
    assertThat(properties.getSkipChatIdsCount()).isEqualTo(3);
  }

  @Test
  @DisplayName("Should handle environment variable with empty entries")
  void shouldHandleEnvironmentVariableWithEmptyEntries() {
    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIdsEnv("123456789,, -1001125352796 ,, 555555555,");

    // When
    List<Long> effectiveIds = properties.getEffectiveChatIds();

    // Then
    assertThat(effectiveIds).containsExactly(123456789L, -1001125352796L, 555555555L);
    assertThat(properties.getSkipChatIdsCount()).isEqualTo(3);
  }

  @Test
  @DisplayName("Should handle invalid environment variable gracefully")
  void shouldHandleInvalidEnvironmentVariableGracefully() {
    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIdsEnv("123456789,invalid,-1001125352796");

    // When
    List<Long> effectiveIds = properties.getEffectiveChatIds();

    // Then
    assertThat(effectiveIds).isEmpty(); // Should return empty list on parsing error
    assertThat(properties.hasSkipChatIds()).isFalse();
    assertThat(properties.getSkipChatIdsCount()).isZero();
  }

  @Test
  @DisplayName("Should handle negative chat IDs")
  void shouldHandleNegativeChatIds() {
    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIds(List.of(-123456789L, -1001125352796L, 789L));

    // When
    boolean shouldSkipNegative = properties.shouldSkipChatId(-1001125352796L);
    boolean shouldSkipPositive = properties.shouldSkipChatId(789L);
    boolean shouldSkipNonExisting = properties.shouldSkipChatId(999L);

    // Then
    assertThat(shouldSkipNegative).isTrue();
    assertThat(shouldSkipPositive).isTrue();
    assertThat(shouldSkipNonExisting).isFalse();
    assertThat(properties.getEffectiveChatIds()).containsExactly(-123456789L, -1001125352796L, 789L);
  }

  @Test
  @DisplayName("Should handle zero chat ID")
  void shouldHandleZeroChatId() {
    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIds(List.of(0L, 123456789L, -1001125352796L));

    // When
    boolean shouldSkipZero = properties.shouldSkipChatId(0L);
    boolean shouldSkipNonZero = properties.shouldSkipChatId(123456789L);

    // Then
    assertThat(shouldSkipZero).isTrue();
    assertThat(shouldSkipNonZero).isTrue();
    assertThat(properties.getEffectiveChatIds()).containsExactly(0L, 123456789L, -1001125352796L);
  }

  @Test
  @DisplayName("Should handle very large chat IDs")
  void shouldHandleVeryLargeChatIds() {
    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIds(List.of(Long.MAX_VALUE, Long.MIN_VALUE, 123L));

    // When
    boolean shouldSkipMax = properties.shouldSkipChatId(Long.MAX_VALUE);
    boolean shouldSkipMin = properties.shouldSkipChatId(Long.MIN_VALUE);
    boolean shouldSkipNormal = properties.shouldSkipChatId(123L);

    // Then
    assertThat(shouldSkipMax).isTrue();
    assertThat(shouldSkipMin).isTrue();
    assertThat(shouldSkipNormal).isTrue();
    assertThat(properties.getEffectiveChatIds()).containsExactly(Long.MAX_VALUE, Long.MIN_VALUE, 123L);
  }

  @Test
  @DisplayName("Should generate correct configuration summary")
  void shouldGenerateCorrectConfigurationSummary() {
    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIds(List.of(123456789L, -1001125352796L, 555555555L));

    // When
    String summary = properties.getConfigurationSummary();

    // Then
    assertThat(summary).contains("skipChatIdsCount=3");
    assertThat(summary).contains("specificChatIds=[123456789, -1001125352796, 555555555]");
  }

  @Test
  @DisplayName("Should generate correct configuration summary for empty list")
  void shouldGenerateCorrectConfigurationSummaryForEmptyList() {
    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIds(List.of());

    // When
    String summary = properties.getConfigurationSummary();

    // Then
    assertThat(summary).contains("skipChatIdsCount=0");
    assertThat(summary).contains("specificChatIds=[]");
  }

  @Test
  @DisplayName("Should handle single chat ID in YAML list")
  void shouldHandleSingleChatIdInYamlList() {
    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIds(List.of(123456789L));

    // When
    boolean shouldSkipExisting = properties.shouldSkipChatId(123456789L);
    boolean shouldSkipNonExisting = properties.shouldSkipChatId(987654321L);

    // Then
    assertThat(shouldSkipExisting).isTrue();
    assertThat(shouldSkipNonExisting).isFalse();
    assertThat(properties.getSkipChatIdsCount()).isEqualTo(1);
    assertThat(properties.hasSkipChatIds()).isTrue();
  }

  @Test
  @DisplayName("Should handle single chat ID in environment variable")
  void shouldHandleSingleChatIdInEnvironmentVariable() {
    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIdsEnv("-1001125352796");

    // When
    boolean shouldSkipExisting = properties.shouldSkipChatId(-1001125352796L);
    boolean shouldSkipNonExisting = properties.shouldSkipChatId(123456789L);

    // Then
    assertThat(shouldSkipExisting).isTrue();
    assertThat(shouldSkipNonExisting).isFalse();
    assertThat(properties.getSkipChatIdsCount()).isEqualTo(1);
    assertThat(properties.isUsingEnvironmentVariable()).isTrue();
  }

  @Test
  @DisplayName("Should handle duplicate chat IDs in YAML list")
  void shouldHandleDuplicateChatIdsInYamlList() {
    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIds(List.of(123456789L, -1001125352796L, 123456789L, 555555555L));

    // When
    List<Long> effectiveIds = properties.getEffectiveChatIds();
    int count = properties.getSkipChatIdsCount();

    // Then
    assertThat(effectiveIds).containsExactly(123456789L, -1001125352796L, 123456789L, 555555555L); // Duplicates are preserved
    assertThat(count).isEqualTo(4);
  }

  @Test
  @DisplayName("Should handle duplicate chat IDs in environment variable")
  void shouldHandleDuplicateChatIdsInEnvironmentVariable() {
    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIdsEnv("123456789,-1001125352796,123456789");

    // When
    List<Long> effectiveIds = properties.getEffectiveChatIds();

    // Then
    assertThat(effectiveIds).containsExactly(123456789L, -1001125352796L, 123456789L); // Duplicates are preserved
  }

  @Test
  @DisplayName("Should handle mix of private chats, groups, and channels")
  void shouldHandleMixOfPrivateChatsGroupsAndChannels() {
    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIds(List.of(
        123456789L,    // Private chat
        -1001125352796L, // Channel (negative)
        -2002234567L,   // Group/Supergroup (negative)
        456789012L     // Another private chat
    ));

    // When
    boolean shouldSkipPrivate = properties.shouldSkipChatId(123456789L);
    boolean shouldSkipChannel = properties.shouldSkipChatId(-1001125352796L);
    boolean shouldSkipGroup = properties.shouldSkipChatId(-2002234567L);
    boolean shouldSkipPrivate2 = properties.shouldSkipChatId(456789012L);
    boolean shouldSkipOther = properties.shouldSkipChatId(999999999L);

    // Then
    assertThat(shouldSkipPrivate).isTrue();
    assertThat(shouldSkipChannel).isTrue();
    assertThat(shouldSkipGroup).isTrue();
    assertThat(shouldSkipPrivate2).isTrue();
    assertThat(shouldSkipOther).isFalse();
    assertThat(properties.getSkipChatIdsCount()).isEqualTo(4);
  }

  @Test
  @DisplayName("Should handle only private chats in skip list")
  void shouldHandleOnlyPrivateChatsInSkipList() {
    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIds(List.of(123456789L, 987654321L, 555555555L));

    // When
    boolean shouldSkipPrivate1 = properties.shouldSkipChatId(123456789L);
    boolean shouldSkipPrivate2 = properties.shouldSkipChatId(987654321L);
    boolean shouldSkipChannel = properties.shouldSkipChatId(-1001125352796L);
    boolean shouldSkipGroup = properties.shouldSkipChatId(-2002234567L);

    // Then
    assertThat(shouldSkipPrivate1).isTrue();
    assertThat(shouldSkipPrivate2).isTrue();
    assertThat(shouldSkipChannel).isFalse();
    assertThat(shouldSkipGroup).isFalse();
    assertThat(properties.getSkipChatIdsCount()).isEqualTo(3);
  }

  @Test
  @DisplayName("Should handle only channels in skip list")
  void shouldHandleOnlyChannelsInSkipList() {
    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIds(List.of(-1001125352796L, -2002234567L, -3003345678L));

    // When
    boolean shouldSkipChannel1 = properties.shouldSkipChatId(-1001125352796L);
    boolean shouldSkipChannel2 = properties.shouldSkipChatId(-2002234567L);
    boolean shouldSkipChannel3 = properties.shouldSkipChatId(-3003345678L);
    boolean shouldSkipPrivate = properties.shouldSkipChatId(123456789L);

    // Then
    assertThat(shouldSkipChannel1).isTrue();
    assertThat(shouldSkipChannel2).isTrue();
    assertThat(shouldSkipChannel3).isTrue();
    assertThat(shouldSkipPrivate).isFalse();
    assertThat(properties.getSkipChatIdsCount()).isEqualTo(3);
  }

  @DisplayName("Should skip all messages from chats in skip list regardless of message content or sender")
  @Test
  void shouldSkipAllMessagesFromChatsInSkipListRegardlessOfContentOrSender() {
    // This test ensures that the skip logic is based on chat ID only, not message properties

    // Given
    MessageSkipProperties properties = new MessageSkipProperties();
    properties.setChatIds(List.of(999L, 888L));

    // Test various scenarios
    boolean shouldSkipChat1 = properties.shouldSkipChatId(999L);
    boolean shouldSkipChat2 = properties.shouldSkipChatId(888L);
    boolean shouldSkipOther = properties.shouldSkipChatId(777L);

    // Then
    assertThat(shouldSkipChat1).isTrue();
    assertThat(shouldSkipChat2).isTrue();
    assertThat(shouldSkipOther).isFalse();
  }
}