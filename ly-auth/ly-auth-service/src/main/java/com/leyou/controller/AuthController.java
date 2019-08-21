package com.leyou.controller;


import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.config.JwtProperties;
import com.leyou.pojo.UserInfo;
import com.leyou.service.AuthService;
import com.leyou.order.config.JwtUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {
    @Autowired
    public AuthService authService;
    @Autowired
    private JwtProperties properties;
@Value("${leyou.jwt.cookieName}")
private String cookieName;
//    @Value("${leyou.jwt.cookieName}")
//    private String cookieName;
    /**
     * 登录授权
     * @param username
     * @param password
     * @return
     */
    @PostMapping("login")
    public ResponseEntity<Void> login(@RequestParam("username") String username, @RequestParam("password") String password,
                                      HttpServletRequest request, HttpServletResponse response){
        String token = authService.login(username, password);
        //  将token写入到cookie

        // 将token写入cookie,并指定httpOnly为true，防止通过JS获取和修改
        CookieUtils.newBuilder(response).httpOnly().request(request).build(cookieName,token);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN")String token, HttpServletRequest request, HttpServletResponse response){
        try {
            if(StringUtils.isBlank(token)){
           throw new LyException(ExceptionEnum.UN_AUTHORIZED);
             }
            //解析token
            UserInfo info = JwtUtils.getInfoFromToken(token, properties.getPublicKey());
            //刷新token
            String newtoken = JwtUtils.generateToken(info, properties.getPrivateKey(), properties.getExpire());
            //将token写入cookie,并指定httpOnly为true，防止通过JS获取和修改
            CookieUtils.newBuilder(response).httpOnly().request(request).build(cookieName,token);
            return ResponseEntity.ok(info);

        } catch (Exception e) {
            throw new LyException(ExceptionEnum.UN_AUTHORIZED);
        }

    }
}
