package com.example.domain.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.domain.repository.LoginUserRepository;

import lombok.extern.slf4j.Slf4j;

@Component("UserDetailsServiceImpl")
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private LoginUserRepository repository;

    @Autowired
    private PasswordEncoder encoder;

    /** ログイン失敗の上限回数. */
    private static final int LOGIN_MISS_LIMIT = 3;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        //リポジトリー(DAO)からUserDetailsを取得
        UserDetails user = repository.selectOne(username);

        return user;
    }

    /**
     * 独自認証用のメソッド.
     * ユーザーIDとテナントIDでユーザーを取得する.
     */
    public UserDetails loadUserByUsernameAndTenantId(String username, String tenantId)
            throws UsernameNotFoundException {

        //リポジトリー(DAO)からUserDetailsを取得
        UserDetails user = repository.selectOne(username, tenantId);

        return user;
    }

    /**
     * パスワードを更新する.
     */
    public void updatePasswordDate(String userId, String password) throws ParseException {

        // パスワード暗号化
        String encryptPass = encoder.encode(password);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date date = sdf.parse("2099/12/31");

        // リポジトリーからパスワード更新
        repository.updatePassword(userId, encryptPass, date);
    }

    /**
     * 失敗回数と有効/無効フラグを更新する.
     */
    public void updateUnlock(String userId, int loginMissTime) {

        // 有効・無効フラグ(有効)
        boolean unlock = true;

        if(loginMissTime >= LOGIN_MISS_LIMIT) {
            unlock = false;
            log.info(userId + "をロックします");
        }

        // リポジトリーからパスワード更新
        repository.updateUnlock(userId, loginMissTime, unlock);
    }
}
