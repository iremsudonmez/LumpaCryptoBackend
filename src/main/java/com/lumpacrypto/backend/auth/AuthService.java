package com.lumpacrypto.backend.auth;

import com.lumpacrypto.backend.common.error.ApiException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthService {

    private static final Duration SESSION_TTL = Duration.ofHours(1);

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final StringRedisTemplate redis;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository,
                       WalletRepository walletRepository,
                       StringRedisTemplate redis) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.redis = redis;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest req) {
        if (userRepository.existsByEmail(req.email()))
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_TAKEN", "Email already registered");
        if (userRepository.existsByUsername(req.username()))
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_TAKEN", "Username already taken");

        User user = userRepository.save(
                new User(req.email(), req.username(), encoder.encode(req.password())));

        // randomized starting balance -> $10,000 to $100,000
        BigDecimal balance = BigDecimal.valueOf(
                        ThreadLocalRandom.current().nextDouble(10_000, 100_000))
                .setScale(2, RoundingMode.HALF_UP);
        Wallet wallet = walletRepository.save(new Wallet(user.getId(), balance));

        return createSession(user, wallet);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Wrong email or password"));

        if (!encoder.matches(req.password(), user.getPasswordHash()))
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Wrong email or password");

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Wallet missing"));

        return createSession(user, wallet);
    }

    public void logout(String token) {
        redis.delete("session:" + token);
    }

    // opaque random token stored in redis with ttl -> real server-side invalidation
    private AuthDtos.AuthResponse createSession(User user, Wallet wallet) {
        String token = UUID.randomUUID().toString();
        redis.opsForValue().set("session:" + token, user.getId().toString(), SESSION_TTL);
        return new AuthDtos.AuthResponse(
                token, user.getId(), user.getEmail(), user.getUsername(),
                wallet.getFiatBalance(), Instant.now().plus(SESSION_TTL));
    }
}