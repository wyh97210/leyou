package com.leyou.order.interceprtor;

import com.leyou.common.utils.CookieUtils;

import com.leyou.pojo.UserInfo;
import com.leyou.order.config.JwtProperties;

import com.leyou.order.config.JwtUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<UserInfo> tl=new ThreadLocal<>();
    private JwtProperties prop;
    public UserInterceptor(JwtProperties prop) {
        this.prop=prop;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        UserInfo user = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
        tl.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
     //用完数据清空
        tl.remove();
    }
    public static UserInfo getUser(){
        return tl.get();
    }
}
