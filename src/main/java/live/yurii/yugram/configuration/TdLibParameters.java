package live.yurii.yugram.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix = "app.telegram.client")
public class TdLibParameters {

  private Integer apiId;
  private String apiHash;
  private String applicationVersion;
  private String databaseDirectory;
  private String deviceModel;
  private Boolean enableStorageOptimizer;
  private String logFile;
  private Integer logFileMaxSize;
  private Integer logVerbosityLevel;
  private String systemLanguageCode;
  private Boolean useMessageDatabase;
  private Boolean useSecretChats;
  private String phoneNumber;
  private String password;
}
