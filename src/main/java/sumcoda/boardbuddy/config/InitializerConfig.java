package sumcoda.boardbuddy.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sumcoda.boardbuddy.service.BadgeImageService;
import sumcoda.boardbuddy.service.MemberService;

import java.util.TimeZone;


@Configuration
@RequiredArgsConstructor
public class InitializerConfig {

    private final MemberService memberService;

    private final BadgeImageService badgeImageService;

//    // 성능 개선용 코드
//    private final RedisTemplate<String, String> chatMessageRedisZSetTemplate;
//    private final RedisTemplate<String, byte[]> chatMessageRedisTemplate;

//    private final TestDataSetInitializeService testDataSetInitializeService;

    @Bean
    public ApplicationRunner initializer() {
        return args -> {

//            // 성능 개선용 코드
//            Set<String> keys = chatMessageRedisTemplate.keys("chat:*");
//            if (!keys.isEmpty()) {
//                chatMessageRedisZSetTemplate.delete(keys);
//                chatMessageRedisTemplate.delete(keys);
//            }
//
//            testDataSetInitializeService.initializeTestDataSet();

            // 프로덕션 코드
            // ddl-auto none 으로 수정후 아래 로직 비활성화
//            memberService.createAdminAccount();
//            memberService.createInitTestAccounts();
//            badgeImageService.assignBadgesToInitTestMembers(YearMonth.now().minusMonths(1));
        };
    }

    @PostConstruct
    public void setTimeZone(){
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}

