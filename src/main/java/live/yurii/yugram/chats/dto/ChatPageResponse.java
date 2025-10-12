package live.yurii.yugram.chats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatPageResponse {
  private List<ChatDto> content;
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
  private boolean first;
  private boolean last;
}