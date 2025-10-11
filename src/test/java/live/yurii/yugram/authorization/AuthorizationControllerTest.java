package live.yurii.yugram.authorization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link AuthorizationController}.
 */
@WebMvcTest(AuthorizationController.class)
@DisplayName("AuthorizationController Tests")
class AuthorizationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AuthorizationEventHandler handler;

  @Test
  @DisplayName("POST /auth/login should return 202 Accepted and publish LoginRequestEvent")
  void whenLogin_thenReturnsAcceptedAndPublishesEvent() throws Exception {
    // When
    mockMvc.perform(post("/auth/login"))
        // Then
        .andExpect(status().isAccepted())
        .andExpect(content().string("Login request accepted. POST OTP code to '/auth/otp'"));

    // Verify event publication
    verify(handler).sendTdLibParameters();
  }

  @Test
  @DisplayName("POST /auth/otp should return 202 Accepted and publish OtpCodeReceivedEvent with code")
  void whenVerifyOtp_thenReturnsAcceptedAndPublishesEventWithCode() throws Exception {
    // Given
    String otpCode = "123456";
    ArgumentCaptor<OtpCodeReceivedEvent> eventCaptor = ArgumentCaptor.forClass(OtpCodeReceivedEvent.class);

    // When
    mockMvc.perform(post("/auth/otp")
            .contentType(MediaType.TEXT_PLAIN)
            .content(otpCode))
        // Then
        .andExpect(status().isAccepted())
        .andExpect(content().string("OTP code accepted"));

    // Verify event publication and its content
    verify(handler).verifyOtp(eventCaptor.capture());
    OtpCodeReceivedEvent publishedEvent = eventCaptor.getValue();
    assertThat(publishedEvent.getCode()).isEqualTo(otpCode);
  }

}
