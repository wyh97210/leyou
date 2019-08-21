package com.leyou.config;

import com.leyou.order.config.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@ConfigurationProperties(prefix = "ly.jwt")
@Data
public class JwtProperties {


    private String pubKeyPath;// 公钥

    private String cookieName;
    private PublicKey publicKey;

    /**
     * @PostContruct：在构造方法执行之后执行该方法
     */
    @PostConstruct
    public void init() throws Exception {

            // 获取公钥和私钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);


    }
    
    // getter setter ...
}