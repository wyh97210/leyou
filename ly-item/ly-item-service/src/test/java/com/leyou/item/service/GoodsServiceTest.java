package com.leyou.item.service;

import com.github.andrewoma.dexx.collection.List;
import com.leyou.common.dto.CartDTO;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsServiceTest {
    @Autowired
    private GoodsService goodsService;
    @org.junit.Test
    public void decreseStock()  {
        java.util.List<CartDTO> cartDTOS = Arrays.asList(new CartDTO(2600242L, 5), new CartDTO(2600248L, 5));
        goodsService.decreseStock(cartDTOS);
    }

}