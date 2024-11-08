package com.shop.config;

import com.shop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final MemberService memberService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.formLogin(form -> {
            form
                    .loginPage("/members/login") // 로그인 페이지
                    .defaultSuccessUrl("/")  // 로그인 성공시 기본 경로
                    .usernameParameter("email") // 로그인시 인증 키 값
                    .failureUrl("/members/login/error"); // 로그인 실패 시 갈 경로
        })
                        .logout(logout -> {
                            logout.logoutRequestMatcher(new AntPathRequestMatcher("/members/logout")) // 로그아웃 처리용 경로
                                    .logoutSuccessUrl("/"); // 로그아웃 성공 시 갈 경로
                        });

        http.authorizeHttpRequests(authorizeRequests -> {
           authorizeRequests.requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                   .requestMatchers("/", "/members/**", "/item/**", "/images/**").permitAll()
                   // requestMatchers Http 요청 매체 적용
                   // .permitAll() 모든 요청을 인가(인증된 사용자 권한에 상관 없음)
                   .requestMatchers("/admin/**").hasRole("ADMIN")
                   // admin 하위 메서드는 ADMIN 룰 적용
                   .anyRequest().authenticated();
        });

        http.exceptionHandling(exceptionHandling -> {
            exceptionHandling
                    .authenticationEntryPoint(new CustomAuthenticationEntryPoint());
        }) ;


        return http.build();
    }

    @Bean // 패스워드를 DB에 저장할 때 암호화 처리함
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
