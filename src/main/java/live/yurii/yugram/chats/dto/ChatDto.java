package live.yurii.yugram.chats.dto;

import live.yurii.yugram.chats.ChatEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatDto {
  private Long id;
  private String type;
  private String title;

  public static ChatDto fromEntity(ChatEntity entity) {
    return ChatDto.builder()
        .id(entity.getId())
        .type(entity.getType() != null ? entity.getType().name() : null)
        .title(entity.getTitle())
        .build();
  }
}