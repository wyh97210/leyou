package com.leyou.order.web;

import com.leyou.order.Dto.OrderDTO;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("order")
public class OrdetController {

    @Autowired
    private OrderService orderService;
    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDTO orderDTO){
        return ResponseEntity.ok(orderService.createOrder(orderDTO));

    }
    @GetMapping("{id}")
    public ResponseEntity<Order> queryOrderById(@PathVariable Long id){
        return ResponseEntity.ok(orderService.queryOrderById(id));
    }
    @GetMapping("url/{id}")
    public ResponseEntity<String> createPayUrl(@PathVariable("id") Long orderId) {
        return ResponseEntity.ok(orderService.createPayUrl(orderId));
    }

    @GetMapping("state/{id}")
    public ResponseEntity<Integer> queryOrderState(@PathVariable("id") Long orderId) {
        return ResponseEntity.ok(orderService.queryOrderState(orderId).getValue());
    }
}
