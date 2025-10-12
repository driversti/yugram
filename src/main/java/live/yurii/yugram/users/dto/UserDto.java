package live.yurii.yugram.users.dto;

import live.yurii.yugram.users.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
  private Long id;
  private String username;
  private String firstName;
  private String lastName;
  private String phoneNumber;
  private Boolean isContact;
  private Boolean isMutualContact;
  private Boolean isCloseFriend;
  private Boolean isPremium;
  private Boolean isSupport;
  private String languageCode;
  private String type;

  public static UserDto fromEntity(UserEntity entity) {
    return UserDto.builder()
        .id(entity.getId())
        .username(entity.getUsername())
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .phoneNumber(entity.getPhoneNumber())
        .isContact(entity.getIsContact())
        .isMutualContact(entity.getIsMutualContact())
        .isCloseFriend(entity.getIsCloseFriend())
        .isPremium(entity.getIsPremium())
        .isSupport(entity.getIsSupport())
        .languageCode(entity.getLanguageCode())
        .type(entity.getType() != null ? entity.getType().name() : null)
        .build();
  }
}