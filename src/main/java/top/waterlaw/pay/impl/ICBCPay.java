package top.waterlaw.pay.impl;

import top.waterlaw.pay.Pay;
import top.waterlaw.pay.Stratygy;
import java.math.BigDecimal;
import java.util.Map;

@Pay(1)
public class ICBCPay implements Stratygy {
    // Spring 框架需注入 Bean
    private Map channelMaper;
    private Map goodsMaper;


    public BigDecimal calRecharge(Integer channelId, Integer goodsId) {
        //- 调用数据库
        // getChannel(channelId) getGoodDisCount(goodsId)
        return null;
    }
}
