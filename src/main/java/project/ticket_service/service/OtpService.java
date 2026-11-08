package project.ticket_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final SecureRandom random = new SecureRandom();

    public String generateAndSaveOtp(String email) {
        String otp = String.format("%06d", random.nextInt(1000000));

        String key = "otp:" + email;
        redisTemplate.opsForValue().set(key, otp, OTP_TTL);

        return otp;
    }

    public boolean validateOtp(String email, String code) {
        String key = "otp:" + email;
        String cachedOtp = redisTemplate.opsForValue().get(key);

        if (cachedOtp != null && cachedOtp.equals(code)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}