package live.yurii.yugram.users;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserHandler {

  private final UserRepository userRepository;

  @Transactional
  @EventListener
  public void handle(UserEvent event) {
    TdApi.User tgUser = event.getUpdateUser().user;
    userRepository.findById(tgUser.id).ifPresentOrElse(
        entity -> userRepository.save(updateEntity(entity, tgUser)),
        () -> userRepository.save(createEntity(tgUser)));
  }

  private UserEntity updateEntity(UserEntity userEntity, TdApi.User tgUser) {
    String userName = getUserName(tgUser.usernames);
    if (userName != null && !userName.equals(userEntity.getUsername())) {
      userEntity.setUsername(userName);
    }
    if (tgUser.firstName != null && !tgUser.firstName.equals(userEntity.getFirstName())) {
      userEntity.setFirstName(tgUser.firstName);
    }
    if (tgUser.lastName != null && !tgUser.lastName.equals(userEntity.getLastName())) {
      userEntity.setLastName(tgUser.lastName);
    }

    if (tgUser.phoneNumber != null && !tgUser.phoneNumber.equals(userEntity.getLastName())) {
      userEntity.setPhoneNumber(tgUser.phoneNumber);
    }

    userEntity.setIsContact(tgUser.isContact);
    userEntity.setIsMutualContact(tgUser.isMutualContact);
    userEntity.setIsCloseFriend(tgUser.isCloseFriend);
    userEntity.setIsPremium(tgUser.isPremium);
    userEntity.setIsSupport(tgUser.isSupport);

    if (tgUser.languageCode != null && !tgUser.languageCode.equals(userEntity.getLanguageCode())) {
      userEntity.setLanguageCode(tgUser.languageCode);
    }

    UserEntity.UserType newType = UserEntity.UserType.fromConstructor(tgUser.type.getConstructor());
    if (!newType.equals(userEntity.getType())) {
      userEntity.setType(newType);
    }

    return userEntity;
  }

  private UserEntity createEntity(TdApi.User user) {
    UserEntity userEntity = new UserEntity(user.id);
    return userEntity
        .withUsername(getUserName(user.usernames))
        .withFirstName(user.firstName)
        .withLastName(user.lastName)
        .withPhoneNumber(user.phoneNumber)
        .withIsContact(user.isContact)
        .withIsMutualContact(user.isMutualContact)
        .withIsCloseFriend(user.isCloseFriend)
        .withIsPremium(user.isPremium)
        .withIsSupport(user.isSupport)
        .withLanguageCode(user.languageCode)
        .withType(UserEntity.UserType.fromConstructor(user.type.getConstructor()));
  }

  private String getUserName(TdApi.Usernames usernames) {
    if (usernames == null) {
      return null;
    }
    if (usernames.activeUsernames != null && usernames.activeUsernames.length > 0) {
      return usernames.activeUsernames[0];
    }
    if (usernames.disabledUsernames != null && usernames.disabledUsernames.length > 0) {
      return usernames.disabledUsernames[0];
    }
    return usernames.editableUsername;
  }
}
