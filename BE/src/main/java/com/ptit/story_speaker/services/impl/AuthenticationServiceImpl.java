package com.ptit.story_speaker.services.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.ptit.story_speaker.common.exceptions.AppException;
import com.ptit.story_speaker.common.exceptions.ErrorCode;
import com.ptit.story_speaker.domain.dto.request.AdminLoginRequest;
import com.ptit.story_speaker.domain.dto.request.LogoutRequest;
import com.ptit.story_speaker.domain.dto.request.RefreshTokenRequest;
import com.ptit.story_speaker.domain.dto.response.AuthenticationResponse;
import com.ptit.story_speaker.domain.entity.RefreshTokenEntity;
import com.ptit.story_speaker.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import com.ptit.story_speaker.domain.entity.UserEntity;
import com.ptit.story_speaker.domain.model.enums.LoginType;
import com.ptit.story_speaker.domain.model.enums.Role;
import com.ptit.story_speaker.repository.UserRepository;
import com.ptit.story_speaker.security.JwtTokenProvider;
import com.ptit.story_speaker.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${google.client-id}")
    private String googleClientId;

    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return idToken.getPayload();
            }
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_GOOGLE_TOKEN);
        }
        return null;
    }

    private String createAndSaveRefreshToken(UserEntity user) {

        String newToken = jwtTokenProvider.createRefreshToken(user.getId());
        Instant newExpiryDate = Instant.now().plusMillis(jwtTokenProvider.getRefreshExpirationMs());

        RefreshTokenEntity tokenEntity = refreshTokenRepository.findByUser(user)
                .orElseGet(() -> RefreshTokenEntity.builder()
                        .user(user)
                        .build());

        tokenEntity.setToken(newToken);
        tokenEntity.setExpiryDate(newExpiryDate);

        refreshTokenRepository.save(tokenEntity);

        return newToken;
    }

    @Override
    public AuthenticationResponse loginWithGoogle(String idTokenString) {

        GoogleIdToken.Payload payload = verifyGoogleToken(idTokenString);
        if (payload == null) {
            throw new AppException(ErrorCode.INVALID_GOOGLE_TOKEN);
        }

        String socialUserId = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        LoginType loginType = LoginType.GOOGLE;

        UserEntity userEntity = userRepository.findBySocialUserIdAndLoginType(socialUserId, loginType)
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity();
                    newUser.setSocialUserId(socialUserId);
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setLoginType(loginType);
                    newUser.setRole(Role.USER);
                    return userRepository.save(newUser);
                });

        String accessToken = jwtTokenProvider.createToken(userEntity.getId());
        String refreshToken = createAndSaveRefreshToken(userEntity);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(userEntity.getId())
                .build();
    }

    @Override
    public AuthenticationResponse loginAdmin(AdminLoginRequest request) {

        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.INCORRECT_USERNAME_PASSWORD));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INCORRECT_USERNAME_PASSWORD);
        }

        if (user.getRole() != Role.ADMIN) {
            throw new AppException(ErrorCode.NOT_ADMIN_ACCOUNT);
        }

        String accessToken = jwtTokenProvider.createToken(user.getId());
        String refreshToken = createAndSaveRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        RefreshTokenEntity tokenInDb = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (tokenInDb.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(tokenInDb);
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        if (!jwtTokenProvider.validateToken(requestRefreshToken)) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        UserEntity user = tokenInDb.getUser();

        String newAccessToken = jwtTokenProvider.createToken(user.getId());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        tokenInDb.setToken(newRefreshToken);
        tokenInDb.setExpiryDate(Instant.now().plusMillis(jwtTokenProvider.getRefreshExpirationMs()));

        refreshTokenRepository.save(tokenInDb);

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        String refreshToken = request.getRefreshToken();
        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken)
                    .ifPresent(refreshTokenRepository::delete);
        }
    }
}
