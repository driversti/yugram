package live.yurii.yugram.authorization;

import live.yurii.yugram.configuration.TdLibParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthorizationEventHandler {

  private final Client client;
  private final TdLibParameters parameters;

  @EventListener
  public void onAuthorizationStateChange(UpdateAuthorizationStateEvent event) {
    log.debug("Authorization state event received: {}", event);
    if (event.getState().getConstructor() != TdApi.UpdateAuthorizationState.CONSTRUCTOR) {
      return;
    }
    TdApi.AuthorizationState newState = event.getState().authorizationState;
    log.debug("Authorization state has changed: {}", newState);
    switch(newState.getConstructor()) {
      case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> sendTdLibParameters();
      case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> sendPhoneNumber();
      case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> sendCode();
      case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> sendPassword();
      case TdApi.AuthorizationStateReady.CONSTRUCTOR -> log.info("Authorization state is ready");
      case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR -> log.info("Logging out...");
      case TdApi.AuthorizationStateClosing.CONSTRUCTOR -> log.info("Closing state...");
      case TdApi.AuthorizationStateClosed.CONSTRUCTOR -> log.info("Closed");
      default -> log.warn("Unsupported authorization state: {}", newState);
    }
  }

  @EventListener(LoginRequestEvent.class)
  public void sendTdLibParameters() {
    TdApi.SetTdlibParameters request = new TdApi.SetTdlibParameters();
    request.databaseDirectory = parameters.getDatabaseDirectory();
    request.filesDirectory = "files";
    request.useMessageDatabase = parameters.getUseMessageDatabase();
    request.useSecretChats = parameters.getUseSecretChats();
    request.useChatInfoDatabase = true;
    request.apiId = parameters.getApiId();
    request.apiHash = parameters.getApiHash();
    request.systemLanguageCode = parameters.getSystemLanguageCode();
    request.deviceModel = parameters.getDeviceModel();
    request.applicationVersion = parameters.getApplicationVersion();

    log.info("Sending TDLib parameters: {}", request);
    client.send(request,
        new AuthorizationRequestHandler(),
        e -> log.error("Failed to send TDLib parameters", e));
  }

  private void sendPhoneNumber() {
    var function = new TdApi.SetAuthenticationPhoneNumber(parameters.getPhoneNumber(), null);
    client.send(function, new AuthorizationRequestHandler());
    log.debug("Sent phone number: {}", parameters.getPhoneNumber());
  }

  @EventListener
  public void verifyOtp(OtpCodeReceivedEvent event) {
    client.send(new TdApi.CheckAuthenticationCode(event.getCode()), new AuthorizationRequestHandler());
    log.info("OTP code sent to Telegram");
  }

  private void sendPassword() {
    client.send(new TdApi.CheckAuthenticationPassword(parameters.getPassword()), new AuthorizationRequestHandler());
    log.debug("Sent password: ***************");
  }

  private void sendCode() {
    log.info("Code authentication is supported in 'verifyOtp' method");
  }

  class AuthorizationRequestHandler implements Client.ResultHandler {

    @Override
    public void onResult(TdApi.Object object) {
      log.info("Received response from TDLib: {}", object);
      switch (object.getConstructor()) {
        case TdApi.Ok.CONSTRUCTOR -> log.info("TDLib parameters set successfully");
        case TdApi.Error.CONSTRUCTOR -> log.error("Received an error: {}. Credentials: {}", object, parameters);
        default -> log.warn("Received wrong response from TDLib: {}", object);
      }
    }
  }
}
