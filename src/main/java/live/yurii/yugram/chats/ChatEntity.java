package live.yurii.yugram.chats;

import jakarta.persistence.*;
import lombok.*;
import org.drinkless.tdlib.*;

@Entity
@Table(name = "chats")
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatEntity {

  @Id
  @Column(name = "id")
  private long id;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private ChatType type;

  @Column(name = "title")
  private String title;

  public ChatEntity(long id) {
    this.id = id;
  }

  public ChatEntity withType(ChatType type) {
    this.type = type;
    return this;
  }

  public ChatEntity withTitle(String title) {
    this.title = title;
    return this;
  }

  @Getter
  public enum ChatType {
    PRIVATE(1579049844),
    BASIC_GROUP(973884508),
    SUPERGROUP(-1472570774),
    SECRET(862366513);

    private final int constructor;

    ChatType(int constructor) {
      this.constructor = constructor;
    }

    public static ChatType fromConstructor(int constructor) {
      return switch (constructor) {
        case TdApi.ChatTypePrivate.CONSTRUCTOR -> PRIVATE;
        case TdApi.ChatTypeBasicGroup.CONSTRUCTOR -> BASIC_GROUP;
        case TdApi.ChatTypeSupergroup.CONSTRUCTOR -> SUPERGROUP;
        case TdApi.ChatTypeSecret.CONSTRUCTOR -> SECRET;
        default -> throw new IllegalArgumentException("Unknown constructor: " + constructor);
      };
    }
  }
}
