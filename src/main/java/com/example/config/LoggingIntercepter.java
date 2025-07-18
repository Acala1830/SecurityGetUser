package com.example.config;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.example.domain.model.AppUserDetails;

import lombok.extern.slf4j.Slf4j;

@Component("LoggingIntercepter")
@Slf4j
public class LoggingIntercepter implements HandlerInterceptor {

    /** USER_IDのキー名 */
    private static final String USER_ID = "USER_ID";

    /** SESSION-IDのキー名 */
    private static final String SESSION_ID = "SESSION_ID";

    @Autowired
    private HttpSession session;

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        // テナントIDの取得
        SecurityContext context = (SecurityContext)session.getAttribute("SPRING_SECURITY_CONTEXT");
        AppUserDetails appUser = null;
        String tenantId = null;
        if(context != null) {
            appUser = (AppUserDetails)context.getAuthentication().getPrincipal();
            tenantId = appUser.getTenantId();
        }

        // ユーザー情報の取得
        Principal user = request.getUserPrincipal();

        // ユーザーIDの設定
        String userId = null;
        if(user != null) {
//            userId = user.getName();
            userId = user.getName() + "(" + tenantId + ")";
        }

        // ユーザーIDをMDCにセット
        if (userId != null && "".equals(userId) == false) {
            MDC.put(USER_ID, userId);
        } else {
            MDC.put(USER_ID, "");
        }

        // セッション情報の取得
        HttpSession session = request.getSession(false);
        if (session != null) {
            MDC.put(SESSION_ID, session.getId());
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView) {

        // キーの削除
        MDC.remove(SESSION_ID);
        MDC.remove(USER_ID);
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {
        // 何もしない
    }
}
