package live.yurii.yugram.messages.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration properties for message skipping behavior.
 * Allows configuration of specific chat IDs for which messages should not be saved.
 * Supports both YAML list format and comma-separated environment variables.
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "app.messages.skip")
public class MessageSkipProperties {

  /**
   * List of chat IDs for which messages should be skipped when saving.
   * Can be configured as a YAML list directly or via SKIP_CHAT_IDS environment variable
   * as a comma-separated string.
   * <p>
   * YAML example:
   * chat-ids:
   * - 123456789
   * - 987654321
   * - -1001125352796
   * <p>
   * Environment variable example:
   * SKIP_CHAT_IDS=123456789,987654321,-1001125352796
   */
  private List<Long> chatIds = new ArrayList<>();

  /**
   * Additional field to handle environment variable override
   * This will be populated from SKIP_CHAT_IDS environment variable
   */
  private String chatIdsEnv = "";

  /**
   * Get the effective list of chat IDs to skip.
   * Combines YAML list and environment variable values.
   * Environment variable takes precedence if set.
   *
   * @return List of chat IDs to skip
   */
  public List<Long> getEffectiveChatIds() {
    // If environment variable is set, use it
    if (StringUtils.hasText(chatIdsEnv)) {
      return parseChatIdsFromString(chatIdsEnv);
    }

    // Otherwise use the YAML list
    return chatIds;
  }

  /**
   * Parse comma-separated chat IDs string into a list of Long values.
   *
   * @param chatIdsString Comma-separated chat IDs
   * @return List of chat IDs
   */
  private List<Long> parseChatIdsFromString(String chatIdsString) {
    if (!StringUtils.hasText(chatIdsString)) {
      return new ArrayList<>();
    }

    try {
      return Arrays.stream(chatIdsString.split(","))
          .map(String::trim)
          .filter(StringUtils::hasText)
          .map(Long::parseLong)
          .toList();
    } catch (NumberFormatException e) {
      // Log error and return empty list if parsing fails
      log.warn("Failed to parse chat IDs from environment variable: {}", chatIdsString);
      return new ArrayList<>();
    }
  }

  /**
   * Check if a message from a specific chat ID should be skipped based on the configuration.
   *
   * @param chatId The chat ID to check
   * @return true if messages from this chat ID should be skipped
   */
  public boolean shouldSkipChatId(Long chatId) {
    return getEffectiveChatIds().contains(chatId);
  }

  /**
   * Get the count of chat IDs in the skip list.
   *
   * @return the number of chat IDs to skip
   */
  public int getSkipChatIdsCount() {
    return getEffectiveChatIds().size();
  }

  /**
   * Check if any chat IDs are configured to be skipped.
   *
   * @return true if any chat IDs are configured to be skipped
   */
  public boolean hasSkipChatIds() {
    return !getEffectiveChatIds().isEmpty();
  }

  /**
   * Get a summary of current skip configuration for logging.
   *
   * @return Configuration summary string
   */
  public String getConfigurationSummary() {
    return String.format(
        "MessageSkipConfig{skipChatIdsCount=%d, specificChatIds=%s}",
        getSkipChatIdsCount(), getEffectiveChatIds()
    );
  }

  /**
   * Check if environment variable override is being used.
   *
   * @return true if SKIP_CHAT_IDS environment variable is set
   */
  public boolean isUsingEnvironmentVariable() {
    return StringUtils.hasText(chatIdsEnv);
  }

  /**
   * Get the source of the configuration (YAML or environment variable).
   *
   * @return String indicating the configuration source
   */
  public String getConfigurationSource() {
    if (isUsingEnvironmentVariable()) {
      return "Environment variable SKIP_CHAT_IDS";
    } else {
      return "YAML configuration";
    }
  }
}