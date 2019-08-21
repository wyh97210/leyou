package com.leyou.order.service;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.Dto.AddressDTO;

import com.leyou.order.Dto.OrderDTO;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodClient;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.interceprtor.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.PayHelper;
import com.leyou.pojo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private IdWorker idWorker;
    @Autowired
    private GoodClient goodClient;

    @Autowired
    private PayHelper payHelper;
    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @Transactional
    public Long createOrder(OrderDTO orderDTO) {
        //1新增订单
        Order order = new Order();
        //1.1 订单编号 基本信息
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDTO.getPaymentType());
        //1.2 用户信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerRate(false);
        order.setBuyerNick(user.getUsername());
        //1.3 收货人地址
        AddressDTO addressDTO = AddressClient.findById(1L);
        order.setReceiver(addressDTO.getName());
        order.setReceiverAddress(addressDTO.getAddress());
        order.setReceiverCity(addressDTO.getCity());
        order.setReceiverDistrict(addressDTO.getDistrict());
        order.setReceiverState(addressDTO.getState());
        order.setReceiverMobile(addressDTO.getPhone());
        order.setReceiverZip(addressDTO.getZipCode());
        //1.4 金额
        Map<Long, Integer> numMap = orderDTO.getCarts().stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        Set<Long> ids = numMap.keySet();
        List<Sku> skus = goodClient.querySkuByIds(new ArrayList<>(ids));

        //准备orderDetail集合

        List<OrderDetail> details=new ArrayList<>();
        Long totalPay=0L;
        for (Sku sku : skus) {
            //计算金额
            totalPay+=sku.getPrice()*numMap.get(sku.getId());
            //封装orderDetail
            OrderDetail orderDetail = new OrderDetail();
            //orderDetail.setId(sku.getId());
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(),","));
            orderDetail.setNum(numMap.get(sku.getId()));
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setPrice(sku.getPrice());
            orderDetail.setOrderId(orderId);
            orderDetail.setSkuId(sku.getId());
            orderDetail.setTitle(sku.getTitle());

            details.add(orderDetail);
        }

        order.setTotalPay(totalPay);
        order.setActualPay(totalPay+order.getPostFee()-0);

        int count=orderMapper.insertSelective(order);
        if(count!=1){
            throw new LyException(ExceptionEnum.CREATE_OEDER_ERROR);
        }
        //2新增订单详情
        count=orderDetailMapper.insertList(details);
        if(count!=details.size()){
            throw new LyException(ExceptionEnum.CREATE_OEDERDETAIL_ERROR);
        }
        //3新增订单状态
        OrderStatus orderStatus = new OrderStatus();

        orderStatus.setOrderId(orderId);
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setStatus(OrderStatusEnum.UN_PAY.value());
        count= orderStatusMapper.insertSelective(orderStatus);
        if(count!=1){
            throw new LyException(ExceptionEnum.CREATE_OEDER_ERROR);
        }
        //4 减库存
        List<CartDTO> carts =orderDTO.getCarts();
        goodClient.decreseStock(carts);
        return orderId;
    }

    /**
     * 根据订单id查询订单
     * @param id
     * @return
     */
    public Order queryOrderById(Long id) {
        Order order = orderMapper.selectByPrimaryKey(id);
        if(order==null){
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(id);
        List<OrderDetail> details = orderDetailMapper.select(orderDetail);
        if(CollectionUtils.isEmpty(details)){
            throw new LyException(ExceptionEnum.ORDERDETAILS_NOT_FOUND);
        }
        order.setOrderDetails(details);

        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(id);
        if(orderStatus==null){
            throw new LyException(ExceptionEnum.ORDERSTATUS_NOT_FOUND);
        }
        order.setOrderStatus(orderStatus);

        return order;
    }

    /**
     * 创建微信支付二维码url
     * @param orderId
     * @return
     */
    public String createPayUrl(Long orderId) {
        // 查询订单获取订单金额
        Order order = queryOrderById(orderId);
        // 判断订单状态
        Integer status = order.getOrderStatus().getStatus();
        if (status != OrderStatusEnum.UN_PAY.getCode()) {
            throw new LyException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        //Long actualPay = order.getActualPay();
        Long actualPay = 1L;
        OrderDetail detail = order.getOrderDetails().get(0);
        String desc = detail.getTitle();
        log.info("[], 订单dizhi:{}",payHelper.createPayUrl(orderId,actualPay,desc));
        return payHelper.createPayUrl(orderId, actualPay, desc);
    }

    public void handleNotify(Map<String, String> result) {
        // 数据校验
        payHelper.isSuccess(result);
        // 校验签名
        payHelper.isValidSign(result);
        String totalFeeStr = result.get("total_fee");
        String tradeNoStr = result.get("out_trade_no");
        if (StringUtils.isBlank(tradeNoStr) || StringUtils.isBlank(totalFeeStr)) {
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        Long totalFee = Long.valueOf(totalFeeStr);
        Long orderId = Long.valueOf(tradeNoStr);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        // FIXME 这里应该是不等于实际金额
        if (totalFee != 1L) {
            // 金额不符
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        // 修改订单状态
        OrderStatus status = new OrderStatus();
        status.setStatus(OrderStatusEnum.PAYED.getCode());
        status.setOrderId(orderId);
        status.setPaymentTime(new Date());
        int count = orderStatusMapper.updateByPrimaryKeySelective(status);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
        }
        log.info("[订单回调, 订单支付成功!], 订单编号:{}", orderId);
    }

    public PayState queryOrderState(Long orderId) {
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        Integer status = orderStatus.getStatus();
        if (status != OrderStatusEnum.UN_PAY.getCode()) {
            // 如果已支付, 真的是已支付
            return PayState.SUCCESS;
        }
        // 如果未支付, 但其实不一定是未支付, 必须去微信查询支付状态
        return payHelper.queryPayState(orderId);
    }
}
