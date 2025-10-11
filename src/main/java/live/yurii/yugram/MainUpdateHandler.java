package live.yurii.yugram;

import live.yurii.yugram.authorization.UpdateAuthorizationStateEvent;
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
      case TdApi.UpdateAuthorizationState.CONSTRUCTOR ->
        publisher.publishEvent(new UpdateAuthorizationStateEvent(this, (TdApi.UpdateAuthorizationState) object));

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
