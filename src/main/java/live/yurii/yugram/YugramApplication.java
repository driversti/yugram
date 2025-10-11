package live.yurii.yugram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class YugramApplication {

  static void main(String[] args) {
    SpringApplication.run(YugramApplication.class, args);
  }

}
