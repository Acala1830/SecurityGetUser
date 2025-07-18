package com.example.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.domain.model.AppUserDetails;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {

        // ユーザー生成
        AppUserDetails user = new AppUserDetails();
        user.setTenantId(request.getParameter("tenantId"));
        user.setUserId(request.getParameter("userId"));

        // パスワード取得
        String password = obtainPassword(request);

        // トークンの作成
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(user, password);

        // リクエストの詳細をセットしておく
        setDetails(request, authRequest);

        // 認証の実施
        return this.getAuthenticationManager()
                .authenticate(authRequest);
    }
}
