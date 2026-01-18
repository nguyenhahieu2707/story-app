package com.ptit.story_speaker.security;

import com.ptit.story_speaker.domain.entity.UserEntity; // Thêm import này
import com.ptit.story_speaker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // 1. Khai báo tường minh UserEntity để trình biên dịch hiểu rõ kiểu dữ liệu
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với ID: " + userId));

        // --- DEBUG: KIỂM TRA CLASS THỰC TẾ ---
        log.info("========================================");
        log.info("Đang load User ID: {}", userId);
        log.info("Class của đối tượng: {}", userEntity.getClass().getName());
        log.info("Có phải là instance của UserDetails không? -> {}", (userEntity instanceof UserDetails));
        log.info("Các Interface mà class này thực thi: {}", Arrays.toString(userEntity.getClass().getInterfaces()));
        log.info("========================================");
        // -------------------------------------

        log.info("User ID: {}", userId);
        log.info("Role trong entity: {}", userEntity.getRole());
        log.info("Authorities trả về: {}", userEntity.getAuthorities());

        // 2. Trả về (Java sẽ tự động upcasting từ UserEntity -> UserDetails)
        return userEntity;
    }
}
