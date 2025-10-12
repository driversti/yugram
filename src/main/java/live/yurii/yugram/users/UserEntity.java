package live.yurii.yugram.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.drinkless.tdlib.TdApi;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class UserEntity {

  @Id
  @Column(name = "id")
  private long id;

  @Column(name = "username")
  private String username;
  @Column(name = "first_name")
  private String firstName;
  @Column(name = "last_name")
  private String lastName;
  @Column(name = "phone_number")
  private String phoneNumber;
  @Column(name = "is_contact")
  private Boolean isContact;
  @Column(name = "is_mutual_contact")
  private Boolean isMutualContact;
  @Column(name = "is_close_friend")
  private Boolean isCloseFriend;
  @Column(name = "is_premium")
  private Boolean isPremium;
  @Column(name = "is_support")
  private Boolean isSupport;
  @Column(name = "language_code")
  private String languageCode;
  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private UserType type;

  public UserEntity(long id) {
    this.id = id;
  }

  public UserEntity withUsername(String username) {
    this.username = username;
    return this;
  }

  public UserEntity withFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public UserEntity withLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public UserEntity withPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
    return this;
  }

  public UserEntity withIsContact(boolean isContact) {
    this.isContact = isContact;
    return this;
  }

  public UserEntity withIsMutualContact(boolean isMutualContact) {
    this.isMutualContact = isMutualContact;
    return this;
  }

  public UserEntity withIsCloseFriend(boolean isCloseFriend) {
    this.isCloseFriend = isCloseFriend;
    return this;
  }

  public UserEntity withIsPremium(boolean isPremium) {
    this.isPremium = isPremium;
    return this;
  }

  public UserEntity withIsSupport(boolean isSupport) {
    this.isSupport = isSupport;
    return this;
  }

  public UserEntity withLanguageCode(String languageCode) {
    this.languageCode = languageCode;
    return this;
  }

  public UserEntity withType(UserType type) {
    this.type = type;
    return this;
  }

  @Getter
  public enum UserType {

    BOT(-1952199642),
    REGULAR(-598644325),
    DELETED(-1807729372),
    UNKNOWN(-724541123);
    private final int constructor;

    UserType(int constructor) {
      this.constructor = constructor;
    }

    public static UserType fromConstructor(int constructor) {
      return switch (constructor) {
        case TdApi.UserTypeBot.CONSTRUCTOR -> BOT;
        case TdApi.UserTypeRegular.CONSTRUCTOR -> REGULAR;
        case TdApi.UserTypeDeleted.CONSTRUCTOR -> DELETED;
        default -> UNKNOWN;
      };
    }
  }

}
