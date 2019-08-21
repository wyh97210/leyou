package com.leyou.mq;

import com.leyou.common.utils.JsonUtils;
import com.leyou.config.SmsProperties;
import com.leyou.order.config.SmsUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Slf4j
@Component
@EnableConfigurationProperties(SmsProperties.class)
public class SmsListener {
    /**
     * 发送短信验证码
     */
    @Autowired
    private SmsUtils smsUtils;

    @Autowired
    private SmsProperties prop;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "sms.verify.code.queue", durable = "true"),
            exchange = @Exchange(value = "ly.sms.exchange",
                                 type = ExchangeTypes.TOPIC),
            key = "sms.verify.code"))
    public void listenSms(Map<String, String> msg) throws Exception {
        if (CollectionUtils.isEmpty(msg)) {
            // 放弃处理
            return;
        }
        String phone = msg.remove("phone");
        //String code = msg.get("code");

        if (StringUtils.isBlank(phone)) {
            // 放弃处理
            return;
        }
        log.info("短信服务555555");
        // 发送消息

       smsUtils.sendSms(phone, prop.getSignName(), prop.getVerifyCodeTemplate(),JsonUtils.serialize(msg));
        //发送短信日志
        log.info("短信服务,发送号码为");

    }
}