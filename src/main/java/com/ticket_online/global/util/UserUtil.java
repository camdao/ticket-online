package com.ticket_online.global.util;

import com.ticket_online.domain.user.dao.UserRepository;
import com.ticket_online.domain.user.domain.User;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserUtil {

    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;

    public User getCurrentUser() {
        return userRepository
                .findById(securityUtil.getCurrentMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public User getUserByUserId(Long memberId) {
        return userRepository
                .findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
