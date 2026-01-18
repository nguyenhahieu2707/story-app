package com.ptit.story_speaker.common.configurations;

import com.ptit.story_speaker.domain.entity.UserEntity;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return Optional.of("anonymousUser"); // or Optional.empty() if you prefer null
        }

        UserEntity userPrincipal = (UserEntity) authentication.getPrincipal();
        return Optional.of(userPrincipal.getId());
    }
}
