package com.leyou.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.order.interceprtor.UserInterceptor;
import com.leyou.pojo.Cart;
import com.leyou.pojo.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service

public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX="cart:uid:";
    public void addCart(Cart cart) {
        //获取当前用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key=KEY_PREFIX+user.getId();
        //hashKey
        String hashKey = cart.getSkuId().toString();
        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);
        //判断当前商品是否存在
        if(operation.hasKey(hashKey)) {
            //是  修改数量
            String json = operation.get(hashKey).toString();
            Cart cacheCart = JsonUtils.parse(json, Cart.class);
            cacheCart.setNum(cacheCart.getNum()+cart.getNum());
            operation.put(hashKey,JsonUtils.serialize(cacheCart));
        }else {
            operation.put(hashKey,JsonUtils.serialize(cart));
        }



    }

    public List<Cart> queryCartList(){
        UserInfo user = UserInterceptor.getUser();
        //key
        String key=KEY_PREFIX+user.getId();

        if(!redisTemplate.hasKey(key)){
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        //获取登录用户的全部商品
        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);
        List<Cart> carts = operation.values().stream().map(o -> JsonUtils.parse(o.toString(), Cart.class)).collect(Collectors.toList());

        return carts;
    }


    public void updateCartNum(Long id, Integer num) {
        //获取当前用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key=KEY_PREFIX+user.getId();
        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);


        Cart cart = JsonUtils.parse(operation.get(id.toString()).toString(), Cart.class);
        cart.setNum(num);
        operation.put(id.toString(),JsonUtils.serialize(cart));

    }

    public void deleteCart(Long spuId) {
        //获取当前用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key=KEY_PREFIX+user.getId();
        redisTemplate.opsForHash().delete(key,spuId.toString());

    }
}
