package live.yurii.yugram.configuration;

import live.yurii.yugram.MainUpdateHandler;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class TelegramConfiguration {

  @Bean
  public Client client(@Autowired TdLibParameters parameters, @Autowired MainUpdateHandler updateHandler) {
    Client.setLogMessageHandler(0, new LogMessageHandler());

    try {
      Client.execute(new TdApi.SetLogVerbosityLevel(0));
      Client.execute(new TdApi.SetLogStream(new TdApi.LogStreamFile(parameters.getLogFile(), parameters.getLogFileMaxSize(), false)));
    } catch (Client.ExecutionException error) {
      throw new RuntimeException("Write access to the current directory is required", error);
    }

    return Client.create(
        updateHandler,
        e -> log.error("Failed to send TDLib parameters", e),
        e -> log.error("Failed to execute TDLib request", e)
    );
  }
}
