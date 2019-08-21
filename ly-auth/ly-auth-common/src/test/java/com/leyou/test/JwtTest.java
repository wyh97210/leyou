package com.leyou.test;

import com.leyou.pojo.UserInfo;
import com.leyou.order.config.JwtUtils;
import com.leyou.order.config.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "D:\\wyh\\ideademo\\rsa\\rsa.pub";

    private static final String priKeyPath = "D:\\wyh\\ideademo\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU2MzY4MDgzNH0.aSK3stC-hZo2gtCFhLrNZ8aBuArWEKNuSTYJEh9U6zaAjSSrKnIDb2zjdPmm24JheoFSntDtlsOb2nHoKZPuLUEwL9fYa_OCCQH8AL4Mg8D4vup0ntw947w50_r8WCLB7A2mHa5PtGXTjII_rPPrxiB1-4qfpMZ8oAM3sBuuRa0";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}