package com.ptit.story_speaker.services;

import com.ptit.story_speaker.domain.dto.request.AdminLoginRequest;
import com.ptit.story_speaker.domain.dto.request.LogoutRequest;
import com.ptit.story_speaker.domain.dto.request.RefreshTokenRequest;
import com.ptit.story_speaker.domain.dto.response.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse loginWithGoogle(String idTokenString);
    AuthenticationResponse loginAdmin(AdminLoginRequest request);
    AuthenticationResponse refreshToken(RefreshTokenRequest request);
    void logout(LogoutRequest request);
}
