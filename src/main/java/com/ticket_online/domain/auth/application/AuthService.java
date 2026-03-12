package com.ticket_online.domain.auth.application;

import com.ticket_online.domain.auth.dto.request.UsernamePasswordRequest;
import com.ticket_online.domain.auth.dto.response.TokenPairResponse;
import com.ticket_online.domain.user.dao.UserRepository;
import com.ticket_online.domain.user.domain.User;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository memberRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    public TokenPairResponse loginMember(UsernamePasswordRequest request) {
        final User member =
                memberRepository
                        .findByUsername(request.username())
                        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        validatePasswordMatches(member, request.password());

        return getLoginResponse(member);
    }

    private TokenPairResponse getLoginResponse(User member) {
        String accessToken = jwtTokenService.createAccessToken(member.getId(), member.getRole());
        String refreshToken = jwtTokenService.createRefreshToken(member.getId());

        return TokenPairResponse.from(accessToken, refreshToken);
    }

    private void validatePasswordMatches(User member, String password) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCHES);
        }
    }

    public void registerMember(UsernamePasswordRequest request) {
        if (memberRepository.findByUsername(request.username()).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        String encodedPassword = passwordEncoder.encode(request.password());

        User member = User.createUser(request.username(), encodedPassword);

        memberRepository.save(member);
    }
}
