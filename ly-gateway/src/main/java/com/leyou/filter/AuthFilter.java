package com.leyou.filter;

import com.leyou.common.utils.CookieUtils;
import com.leyou.config.FilterProperties;
import com.leyou.config.JwtProperties;
import com.leyou.order.config.JwtUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class AuthFilter extends ZuulFilter {
    @Autowired
    private JwtProperties prop;

    @Autowired
  private   FilterProperties filterProperties;
    @Override
    public String filterType() {
        //过滤类型 前置
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER-1;//过滤器顺序
    }

    @Override
    public boolean shouldFilter() {//是否过滤、
        //获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = ctx.getRequest();
        //获取请求路径
        String path = request.getRequestURI();
System.out.println(path);
        //判断是否放行


        return !isallowPath(path);
    }

    private boolean isallowPath(String path) {
        for (String alowpath : filterProperties.getAllowPaths()) {
            if(path.startsWith(alowpath)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Object run() throws ZuulException {
        //获取request
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        //获取token
        String cookie = CookieUtils.getCookieValue(request, prop.getCookieName());
        //解析token
        try {
            JwtUtils.getInfoFromToken(cookie, prop.getPublicKey());
            //TODO 检验权限
        } catch (Exception e) {
            //未登录.拦截
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(403);
        }
        return null;
    }
}
