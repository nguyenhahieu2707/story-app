package com.ptit.story_speaker.common.configurations;

import com.ptit.story_speaker.common.exceptions.CustomAccessDeniedHandler;
import com.ptit.story_speaker.common.exceptions.CustomAuthenticationEntryPoint;

// Import các class bảo mật (Service và Filter)
import com.ptit.story_speaker.security.CustomUserDetailsService;
import com.ptit.story_speaker.security.JwtAuthenticationFilter;
import com.ptit.story_speaker.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 1. Kích hoạt @PreAuthorize cho Controller
@RequiredArgsConstructor
public class SecurityConfig {

    // Inject các thành phần cần thiết
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public static String[] PUBLIC_URLS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/auth/**"
    };

    /**
     * 2. Tự tạo Bean cho JwtAuthenticationFilter tại đây.
     * Lý do: Để kiểm soát thứ tự chạy của Filter chính xác trong chuỗi bảo mật.
     * (Nhớ xóa @Component ở file JwtAuthenticationFilter.java nếu có)
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider, customUserDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 3. Bean AuthenticationManager cần thiết để Spring Security hoạt động
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF vì chúng ta dùng Token, không dùng Session/Cookies
                .csrf(csrf -> csrf.disable())

                .cors(Customizer.withDefaults())

                // Chuyển sang chế độ Stateless (Không lưu Session phía Server)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Cấu hình quyền truy cập các đường dẫn
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .anyRequest().authenticated() // Tất cả các request còn lại phải có token hợp lệ
                )

                // 4. Cấu hình xử lý lỗi (401, 403) trả về JSON đẹp thay vì HTML mặc định
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // 5. Thêm bộ lọc JWT vào trước bộ lọc xác thực Username/Password mặc định
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}