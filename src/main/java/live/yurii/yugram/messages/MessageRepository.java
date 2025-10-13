package live.yurii.yugram.messages;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
  Page<MessageEntity> findByChatId(Long chatId, Pageable pageable);
  Page<MessageEntity> findByChatIdAndSenderId(Long chatId, Long senderId, Pageable pageable);

  @Query("SELECT DISTINCT m.chatId FROM MessageEntity m")
  List<Long> findDistinctChatIds();
}
