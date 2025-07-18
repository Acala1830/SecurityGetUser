package com.example.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.domain.model.AppUserDetails;
import com.example.domain.service.UserDetailsServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Component("CustomAuthenticationProvider")
@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserDetailsServiceImpl service;

    @Autowired
    private MessageSource messageSource;

    /** メッセージのキー(認証失敗) */
    private static final String BAD_CREDENTIALS = "AbstractUserDetailsAuthenticationProvider.badCredentials";

    /**
     * 認証処理を行うメソッド.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        // ユーザー取得
        AppUserDetails user = (AppUserDetails)authentication.getPrincipal();

        // パスワード取得
        String password = (String)authentication.getCredentials();

        // DBからユーザー取得
        user = (AppUserDetails) service.loadUserByUsernameAndTenantId(user.getUserId(), user.getTenantId());

        // パスワードチェック
        checkPassword(password, user.getPassword());

        // 各種チェック
        UserDetailsChecker checker = new AccountStatusUserDetailsChecker();
        checker.check(user);

        // トークンを再生成
        return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
    }

    /**
     * 認証対象の制限.
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * パスワードチェック.
     */
    private void checkPassword(String rawPassword, String encodedPassword) {

        PasswordEncoder encoder = new BCryptPasswordEncoder();

        // パスワードが一致しているかどうか
        if(encoder.matches(rawPassword, encodedPassword) == false) {
            // エラーメッセージ取得
            String message = messageSource.getMessage(BAD_CREDENTIALS,
                    null,
                    Locale.getDefault());
            // 例外を投げる
            throw new BadCredentialsException(message);
        }
    }
}
