package com.archiservice.common;

import com.archiservice.common.security.CustomUser;
import com.archiservice.user.domain.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WithMockCustomUserFactory implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {

        User mockUser = createMockUser(annotation);
        CustomUser customUser = new CustomUser(mockUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customUser,
                customUser.getPassword(),
                customUser.getAuthorities()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        return context;
    }

    private User createMockUser(WithMockCustomUser annotation) {

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(annotation.id());
        when(user.getEmail()).thenReturn(annotation.email());
        when(user.getPassword()).thenReturn(annotation.password());
        when(user.getTagCode()).thenReturn(annotation.tagCode());
        when(user.getAgeCode()).thenReturn(annotation.ageCode());

        return user;
    }
}
