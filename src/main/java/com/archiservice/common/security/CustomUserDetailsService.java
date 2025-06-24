package com.archiservice.common.security;

import com.archiservice.common.jwt.RefreshTokenService;
import com.archiservice.user.domain.User;
import com.archiservice.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        if (isRefreshTokenRequest()) {
            if (!refreshTokenService.hasRefreshToken(user.getUserId())) {
                throw new UsernameNotFoundException("Refresh Token이 만료되었습니다. 다시 로그인해주세요.");
            }
        }

        return new CustomUser(user);
    }
    
    public CustomUser loadUserByUserId(Long userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        if (isRefreshTokenRequest()) {
            if (!refreshTokenService.hasRefreshToken(userId)) {
                throw new UsernameNotFoundException("Refresh Token이 만료되었습니다. 다시 로그인해주세요.");
            }
        }

        return new CustomUser(user);
    }

    private boolean isRefreshTokenRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            String requestURI = request.getRequestURI();

            return "/api/auth/refresh".equals(requestURI);
        }
        return false;
    }
}
