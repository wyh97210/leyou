package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.userMapper;

import com.leyou.user.pojo.User;

import com.leyou.user.utils.CodecUtils;

import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private userMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final  String key_prefix="user:verify:phone:";

    /**
     * 校验数据
     * @param data
     * @param type
     * @return
     */
    public Boolean checkData(String data, Integer type) {
        User record = new User();
        //判断数据类型
        switch (type){
            case 1:
                record.setUsername(data);
                break;
            case 2:
                record.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_USER_DATA_TYPE);
        }
        return  userMapper.selectCount(record)==0;
    }

    /**
     * 发送短信
     * @param phone
     */
    public void sendCode(String phone) {
        //生成key
        String key=key_prefix + phone;
        Map<String, String> msg = new HashMap<>();
        //生成code
        String code= NumberUtils.generateCode(6);
        msg.put("phone",phone);
        msg.put("code",code);
        amqpTemplate.convertAndSend("ly.sms.exchange","sms.verify.code",msg);
        stringRedisTemplate.opsForValue().set(key,code,5, TimeUnit.MINUTES);
        System.out.println("保存到redis");
    }

    /**
     * 注册用户
     * @param user
     * @param code
     */
    public void register( User user, String code) {
        //校验验证码
        //取出验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(key_prefix + user.getPhone());
        if (!StringUtils.equals(code,cacheCode)) {
            throw new LyException(ExceptionEnum.INVALID_RIGISTER_CODE);
        }
        //生成halt
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        //密码加密
        String password = CodecUtils.md5Hex(user.getPassword(), salt);
        user.setPassword(password);

        //保存
        user.setCreated(new Date());
        userMapper.insert(user);
    }

    /**
     * 查询用户
     * @param username
     * @param password
     * @return
     */
    public User queryUserByUsernameAndPassword(String username, String password) {
        User record = new User();
        record.setUsername(username);
        User user = userMapper.selectOne(record);
        if (user==null) {
            throw  new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        if (!StringUtils.equals(user.getPassword(), CodecUtils.md5Hex(password,user.getSalt()))) {
            throw  new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        return user;
    }
}
