package live.yurii.yugram.chats;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    @Query("SELECT DISTINCT c FROM ChatEntity c INNER JOIN MessageEntity m ON c.id = m.chatId ORDER BY c.id")
    Page<ChatEntity> findChatsWithMessages(Pageable pageable);
}
