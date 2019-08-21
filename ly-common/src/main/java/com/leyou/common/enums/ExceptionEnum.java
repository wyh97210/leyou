package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {
    CATEGORY_NOT_ROND(404,"商品分类没查到"),
    SPEC_GROUP_NOT_ROND(404,"商品规格组没查到"),
    BRAND_NOT_FOUND(404,"商品品牌没有"),
    BRAND_SAVE_ERROR(500,"新增品牌失败"),
    UPLOAD_FILE_ERROR(500,"文件上传失败"),
    INVALID_FILE_TYPE(400,"无效文件类型"),
    SPEC_PARAM_NOT_ROND(404,"商品参数没查到" ),
    GOODS_SAVE_ERROR(500,"新增商品失败"),
    GOODS_NOT_FOUND(404,"商品没查到"),
    SPUDETAIL_NOT_FOUND(404,"商品信息没查到" ),
    SKU_NOT_FOUND(404,"商品信息没查到" ),
    INVALID_USERNAME_PASSWORD(400,"无效的用户名和密码"),
    INVALID_USER_DATA_TYPE(400,"用户类型无效"),
    INVALID_RIGISTER_CODE(400,"无效验证码"),
    CREATE_TOKEN_ERROR(500,"生成用户凭证失败"),
    GOODS_UPDATE_ERROR(500,"更新商品失败"),
    UN_AUTHORIZED(403,"未授权" ), CART_NOT_FOUND(404,"购物车为空" ),
    CREATE_OEDER_ERROR(500, "创建订单失败"),
    CREATE_OEDERDETAIL_ERROR(500, "创建订单详情失败" ),
    STOCK_NOT_ENOUGH(500,"库存不足" ),
    ORDER_NOT_FOUND(500,"订单不存在" ),
    ORDERDETAILS_NOT_FOUND(500,"订单详情不存在" ),
    ORDERSTATUS_NOT_FOUND(500, "订单状态不存在"),
    WX_PAY_ORDER_FAIL(500,"下单失败"),
    ORDER_STATUS_ERROR(400,"订单状态异常"),
    INVALID_SIGN_ERROR(400,"无效的签名"),
    INVALID_ORDER_PARAM(400,"订单参数异常"),
    UPDATE_ORDER_STATUS_ERROR(500,"更新订单状态失败"),
            ;

    private int code;
    private String msg;


}
