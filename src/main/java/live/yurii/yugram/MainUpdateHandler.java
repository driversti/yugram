package live.yurii.yugram;

import live.yurii.yugram.authorization.UpdateAuthorizationStateEvent;
import live.yurii.yugram.user.UpdateUserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class MainUpdateHandler implements Client.ResultHandler {

  private final ApplicationEventPublisher publisher;
  private final Queue<TdApi.Object> pendingUpdates = new ConcurrentLinkedQueue<>();
  private boolean isApplicationReady = false;

  @Override
  public void onResult(TdApi.Object object) {
    if (!isApplicationReady) {
      // Store updates that arrive before initialization in a queue
      log.info("Initialization not completed, queueing update");
      queueUpdate(object);
      return;
    }

    processUpdate(object);
  }

  private synchronized void queueUpdate(TdApi.Object object) {
    pendingUpdates.add(object);
  }

  private void processUpdate(TdApi.Object object) {
    switch (object.getConstructor()) {
      // authorization
      case TdApi.UpdateAuthorizationState.CONSTRUCTOR ->
        publisher.publishEvent(new UpdateAuthorizationStateEvent(this, (TdApi.UpdateAuthorizationState) object));

      // users
      case TdApi.UpdateUser.CONSTRUCTOR -> publisher.publishEvent(new UpdateUserEvent(this, (TdApi.UpdateUser) object));
      case TdApi.UpdateUserStatus.CONSTRUCTOR -> skip("UpdateUserStatus");

      case TdApi.UpdateNewMessage.CONSTRUCTOR -> skip("UpdateNewMessage");
      case TdApi.UpdateChatLastMessage.CONSTRUCTOR -> skip("UpdateChatLastMessage");
      case TdApi.UpdateUnreadMessageCount.CONSTRUCTOR -> skip("UpdateUnreadMessageCount");
      case TdApi.UpdateSupergroup.CONSTRUCTOR -> skip("UpdateSupergroup");
      case TdApi.UpdateSupergroupFullInfo.CONSTRUCTOR -> skip("UpdateSupergroupFullInfo");
      case TdApi.UpdateOption.CONSTRUCTOR -> skip("UpdateOption");
      case TdApi.UpdateNewChat.CONSTRUCTOR -> skip("UpdateNewChat");
      case TdApi.UpdateChatReadInbox.CONSTRUCTOR -> skip("UpdateChatReadInbox");
      case TdApi.UpdateChatAddedToList.CONSTRUCTOR -> skip("UpdateChatAddedToList");
      case TdApi.UpdateDeleteMessages.CONSTRUCTOR -> skip("UpdateDeleteMessages");
      case TdApi.UpdateMessageInteractionInfo.CONSTRUCTOR -> skip("UpdateMessageInteractionInfo");
      case TdApi.UpdateConnectionState.CONSTRUCTOR -> skip("UpdateConnectionState");
      case TdApi.UpdateHavePendingNotifications.CONSTRUCTOR -> skip("UpdateHavePendingNotifications");
      case TdApi.UpdateMessageContent.CONSTRUCTOR -> skip("UpdateMessageContent");
      case TdApi.UpdateMessageEdited.CONSTRUCTOR -> skip("UpdateMessageEdited");
      case TdApi.UpdateChatAction.CONSTRUCTOR -> skip("UpdateChatAction");
      case TdApi.UpdateAttachmentMenuBots.CONSTRUCTOR -> skip("UpdateAttachmentMenuBots");
      case TdApi.UpdateDefaultBackground.CONSTRUCTOR -> skip("UpdateDefaultBackground");
      case TdApi.UpdateFileDownloads.CONSTRUCTOR -> skip("UpdateFileDownloads");
      case TdApi.UpdateDiceEmojis.CONSTRUCTOR -> skip("UpdateDiceEmojis");
      case TdApi.UpdateActiveEmojiReactions.CONSTRUCTOR -> skip("UpdateActiveEmojiReactions");
      case TdApi.UpdateAvailableMessageEffects.CONSTRUCTOR -> skip("UpdateAvailableMessageEffects");
      case TdApi.UpdateDefaultPaidReactionType.CONSTRUCTOR -> skip("UpdateDefaultPaidReactionType");
      case TdApi.UpdateChatThemes.CONSTRUCTOR -> skip("UpdateChatThemes");
      case TdApi.UpdateReactionNotificationSettings.CONSTRUCTOR -> skip("UpdateReactionNotificationSettings");
      case TdApi.UpdateChatFolders.CONSTRUCTOR -> skip("UpdateChatFolders");
      case TdApi.UpdateUnreadChatCount.CONSTRUCTOR -> skip("UpdateUnreadChatCount");
      case TdApi.UpdateStoryStealthMode.CONSTRUCTOR -> skip("UpdateStoryStealthMode");
      case TdApi.UpdateChatAvailableReactions.CONSTRUCTOR -> skip("UpdateChatAvailableReactions");
      case TdApi.UpdateChatIsTranslatable.CONSTRUCTOR -> skip("UpdateChatIsTranslatable");
      case TdApi.UpdateChatMessageSender.CONSTRUCTOR -> skip("UpdateChatMessageSender");
      case TdApi.UpdateMessageIsPinned.CONSTRUCTOR -> skip("UpdateMessageIsPinned");
      case TdApi.UpdateChatNotificationSettings.CONSTRUCTOR -> skip("UpdateChatNotificationSettings");
      case TdApi.UpdateChatVideoChat.CONSTRUCTOR -> skip("UpdateChatVideoChat");
      case TdApi.UpdateGroupCall.CONSTRUCTOR -> skip("UpdateGroupCall");
      
      default -> log.debug("Not implemented update: {}", object.getClass().getName());
    }
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReadyEvent() {
    log.debug("ApplicationReadyEvent");
    if (!isApplicationReady) {
      isApplicationReady = true;
      log.debug("Initialization completed. Processing queued updates...");
      while (!pendingUpdates.isEmpty()) {
        TdApi.Object update = pendingUpdates.poll();
        if (update != null) {
          processUpdate(update);
        }
      }
    }
  }

  private void skip(String updateName) {
  }
}
