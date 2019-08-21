package com.leyou.service;

import com.leyou.client.UserClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.config.JwtProperties;
import com.leyou.pojo.UserInfo;
import com.leyou.user.pojo.User;
import com.leyou.order.config.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {
    @Autowired
    private UserClient userClient;
    @Autowired
    private JwtProperties prop;
    public String login(String username, String password)  {
        try { User user = userClient.queryUserByUsernameAndPassword(username, password);
        if (user==null) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
            String token = JwtUtils.generateToken(new UserInfo(user.getId(), username), prop.getPrivateKey(), prop.getExpire());
            return token;
        } catch (Exception e) {
            log.error("授权中心生成token失败 ，用户名{}",username,e);
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

    }
}
