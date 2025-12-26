package com.shopperspoint.auditing;

import com.shopperspoint.dto.UserPrinciple;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.of("SYSTEM");
        }

        Object principle = authentication.getPrincipal();

        if (principle instanceof UserPrinciple userPrinciple) {
            return Optional.of(userPrinciple.getId().toString());
        }

        return Optional.of("UNKNOWN");
    }
}
