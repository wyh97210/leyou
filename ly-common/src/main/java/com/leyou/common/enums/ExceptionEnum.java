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
    GOODS_UPDATE_ERROR(500,"更新商品失败");

    private int code;
    private String msg;


}
