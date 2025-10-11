package live.yurii.yugram.configuration;

import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;

@Slf4j
public class LogMessageHandler implements Client.LogMessageHandler {

  @Override
  public void onLogMessage(int verbosityLevel, String message) {
    switch (verbosityLevel) {
      case 0, 1 -> log.error("TDLib: {}", message);
      case 2 -> log.warn("TDLib: {}", message);
      case 3 -> log.info("TDLib: {}", message);
      default -> log.debug("TDLib: {}", message);
    }
  }
}
