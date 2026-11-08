package project.ticket_service.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ticket_service.dto.AuthDtos.*;
import project.ticket_service.model.User;
import project.ticket_service.repository.UserRepository;
import project.ticket_service.security.JwtService;
import project.ticket_service.security.UserDetailsServiceImpl;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final StringRedisTemplate redisTemplate;
    private final OtpService otpService;
    private final EmailProducer emailProducer;

    private static final String USER_SESSION_PREFIX = "user:session:";

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        User user = User.builder()
                .email(request.email())
                .hashedPassword(passwordEncoder.encode(request.password()))
                .isVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        return new UserResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getCreatedAt());
    }

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        Claims claims = jwtService.extractClaim(refreshToken, c -> c);
        String jti = claims.getId();
        long expirationMs = claims.getExpiration().getTime() - System.currentTimeMillis();

        if (jti == null) {
            throw new IllegalStateException("Generated refresh token does not contain JTI");
        }

        String redisKey = USER_SESSION_PREFIX + user.getId();
        redisTemplate.opsForValue().set(
                redisKey,
                jti,
                Duration.ofMillis(expirationMs > 0 ? expirationMs : 604800000L)
        );

        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        String email = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Claims claims = jwtService.extractClaim(refreshToken, c -> c);
        String tokenJti = claims.getId();

        String redisKey = USER_SESSION_PREFIX + user.getId();
        String activeJti = redisTemplate.opsForValue().get(redisKey);

        if (activeJti == null || !activeJti.equals(tokenJti)) {
            throw new SecurityException("Session expired or token reuse detected! Please login again.");
        }

        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        Claims newClaims = jwtService.extractClaim(newRefreshToken, c -> c);
        String newJti = newClaims.getId();
        long newExpirationMs = newClaims.getExpiration().getTime() - System.currentTimeMillis();

        if (newJti != null) {
            redisTemplate.opsForValue().set(
                    redisKey,
                    newJti,
                    Duration.ofMillis(newExpirationMs > 0 ? newExpirationMs : 604800000L)
            );
        }

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    public void sendVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (user.getIsVerified()) {
            throw new IllegalStateException("Email is already verified");
        }

        String otpCode = otpService.generateAndSaveOtp(email);

        emailProducer.sendEmailTask(email, otpCode);
    }

    public void verifyEmail(String email, String otpCode) {
        boolean isValid = otpService.validateOtp(email, otpCode);
        if (!isValid) {
            throw new IllegalArgumentException("Invalid or expired OTP code");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        user.setIsVerified(true);
        userRepository.save(user);
    }
}