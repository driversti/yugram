package live.yurii.yugram.messages;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MessageEntity {

  @Id
  @Column(name = "id")
  private long id;

  @Column(name = "sender_id")
  private long senderId; // can be user or chat

  @Column(name = "chat_id")
  private long chatId;

  @Column(name = "date")
  private int date;

  @Column(name = "content")
  private String content; // interesting only for text messages (also as a description for photos, videos, etc.)

  public MessageEntity(long id) {
    this.id = id;
  }

  public MessageEntity withSenderId(long senderId) {
    this.senderId = senderId;
    return this;
  }

  public MessageEntity withChatId(long chatId) {
    this.chatId = chatId;
    return this;
  }

  public MessageEntity withDate(int date) {
    this.date = date;
    return this;
  }

  public MessageEntity withContent(String content) {
    this.content = content;
    return this;
  }
}
