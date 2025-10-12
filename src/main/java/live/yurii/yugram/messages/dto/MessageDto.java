package live.yurii.yugram.messages.dto;

import live.yurii.yugram.messages.MessageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
  private Long id;
  private Long senderId;
  private Long chatId;
  private Integer date;
  private String content;

  public static MessageDto fromEntity(MessageEntity entity) {
    return MessageDto.builder()
        .id(entity.getId())
        .senderId(entity.getSenderId())
        .chatId(entity.getChatId())
        .date(entity.getDate())
        .content(entity.getContent())
        .build();
  }
}