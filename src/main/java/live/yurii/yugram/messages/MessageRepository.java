package live.yurii.yugram.messages;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
  Page<MessageEntity> findByChatId(Long chatId, Pageable pageable);
  Page<MessageEntity> findByChatIdAndSenderId(Long chatId, Long senderId, Pageable pageable);
}
