package live.yurii.yugram.chats;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {
}
