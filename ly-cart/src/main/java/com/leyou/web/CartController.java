package com.leyou.web;

import com.leyou.pojo.Cart;
import com.leyou.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;
    @PostMapping
    public ResponseEntity<Void> addcart(@RequestBody Cart cart){
        cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @GetMapping("list")
    public ResponseEntity<List<Cart>> queryCartList(){

        return ResponseEntity.ok(cartService.queryCartList());

    }
    @PutMapping
    public ResponseEntity<Void> updateCartNum(@RequestParam("id") Long id , @RequestParam("num") Integer num){
        cartService.updateCartNum(id,num);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }
    @DeleteMapping("{spuId}")
    public ResponseEntity<Void> updateCartNum(@PathVariable("spuId") Long spuId){
        cartService.deleteCart(spuId);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

}
