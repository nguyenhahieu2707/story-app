package com.ptit.story_speaker.controllers;

import com.ptit.story_speaker.domain.dto.request.AdminLoginRequest;
import com.ptit.story_speaker.domain.dto.request.LoginRequest;
import com.ptit.story_speaker.domain.dto.request.LogoutRequest;
import com.ptit.story_speaker.domain.dto.request.RefreshTokenRequest;
import com.ptit.story_speaker.domain.dto.response.AuthenticationResponse;
import com.ptit.story_speaker.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/google")
    public ResponseEntity<AuthenticationResponse> loginWithGoogle(@RequestBody LoginRequest request) {
            return ResponseEntity.ok(authenticationService.loginWithGoogle(request.getToken()));
    }

    @PostMapping("/admin/login")
    public ResponseEntity<AuthenticationResponse> loginAdmin(@RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(authenticationService.loginAdmin(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LogoutRequest request) {
        authenticationService.logout(request);
        return ResponseEntity.ok("Đăng xuất thành công");
    }
}
