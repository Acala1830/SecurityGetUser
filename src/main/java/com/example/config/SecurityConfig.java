package com.example.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * セキュリティ設定クラス.
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    @Qualifier("SuccessHandler")
    AuthenticationSuccessHandler successHandler;

    @Autowired
    @Qualifier("UserDetailsServiceImpl")
    private UserDetailsService userDetailsService;

    //パスワードエンコーダーのBean定義
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // データソース
    @Autowired
    private DataSource dataSource;

    @Autowired
    @Qualifier("CustomAuthenticationProvider")
    private AuthenticationProvider authenticationProvider;

    // ユーザーIDとパスワードを取得するSQL文
    private static final String USER_SQL = "SELECT"
            + "    user_id,"
            + "    password,"
            + "    enabled"
            + " FROM"
            + "    m_user"
            + " WHERE"
            + "    user_id = ?";

    // ユーザーのロールを取得するSQL文
    private static final String ROLE_SQL = "SELECT"
            + "    m_user.user_id,"
            + "    role.role_name"
            + " FROM"
            + "    m_user INNER JOIN t_user_role AS user_role"
            + " ON"
            + "    m_user.user_id = user_role.user_id"
            + "    INNER JOIN m_role AS role"
            + " ON"
            + "    user_role.role_id = role.role_id"
            + " WHERE"
            + "    m_user.user_id = ?";

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 直リンクの禁止＆ログイン不要ページの設定
        http
            .authorizeRequests()
                .antMatchers("/login").permitAll() //ログインページは直リンクOK
                .anyRequest().authenticated(); //それ以外は直リンク禁止

        //ログイン処理の実装
        http
            .formLogin()
                // .loginProcessingUrl("/login") //ログイン処理のパス
                .loginPage("/login"); //ログインページの指定
                // .failureUrl("/login?error") //ログイン失敗時の遷移先
                // .usernameParameter("userId") //ログインページのユーザーID
                // .passwordParameter("password") //ログインページのパスワード
                // .defaultSuccessUrl("/home", true) //ログイン成功後の遷移先
                // .successHandler(successHandler);

        // ログインフィルターの設定
        CustomAuthenticationFilter filter = new CustomAuthenticationFilter();
        filter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/login", "POST"));
        filter.setAuthenticationManager(authenticationManagerBean());
        filter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler("/login?error"));
        filter.setAuthenticationSuccessHandler(successHandler);
        http.addFilterBefore(filter, CustomAuthenticationFilter.class);

        // ログアウト処理
        http
            .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login");

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

     // ログイン処理時のユーザー情報を、DBから取得する
//      auth.jdbcAuthentication()
//              .dataSource(dataSource)
//              .usersByUsernameQuery(USER_SQL) //ユーザーの取得
//              .authoritiesByUsernameQuery(ROLE_SQL) //ロールの取得
//              .passwordEncoder(passwordEncoder()); //パスワードの復号

//        auth
//            .userDetailsService(userDetailsService)
//            .passwordEncoder(passwordEncoder());

        // 独自認証
        auth.authenticationProvider(authenticationProvider);

    }
}
